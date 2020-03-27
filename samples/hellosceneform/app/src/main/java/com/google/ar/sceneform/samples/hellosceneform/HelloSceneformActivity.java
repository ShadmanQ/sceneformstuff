/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager;
import com.google.ar.sceneform.samples.hellosceneform.AssetNodes.AugmentedAssetNode;
import com.google.ar.sceneform.samples.hellosceneform.AssetNodes.ExperienceSplashImageNode;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.samples.hellosceneform.utils.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
  private static final String TAG = HelloSceneformActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;
  protected Bitmap AugmentedImageBitmap;
  protected AugmentedImageDatabase augmentedImageDatabase;
  protected ExperienceSplashImageNode splashNode;
  protected Collection<AugmentedAssetNode> AssetNodes;
  public static com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager AnalyticsManager = new AnalyticsManager();
  protected String current_uuid;
  private ArFragment arFragment;
  private ModelRenderable andyRenderable;
  private ViewRenderable testRenderable;
  public static API api = new API();
  String metaData = "";
  public static LinkedList<AugmentedAssetNode> audioAndVideoNodes = new LinkedList<>();
  public static boolean tracking = false;

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (CustomARFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    Log.d("TAG", "Custom Fragment created");

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

/*
    String response = api.fetchARExperience("d4f74b5e-1ddf-11ea-9a0b-1fd7f8b74283");
    Log.d("API_RESPONSE_ONCREATE",response);
*/

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (andyRenderable == null) {
            return;
          }

            Thread thread = new Thread(()->{
                    String response = api.fetchARExperienceByExpID("d4f74b5e-1ddf-11ea-9a0b-1fd7f8b74283");
                    Log.d("API Response",response);
                    try {
                      JSONObject reader = new JSONObject(response);
                      String marker_uuid = reader.getString("marker_uuid");
                      DownloadMarker(marker_uuid);
                      runOnUiThread(()-> Toast.makeText(this, marker_uuid, Toast.LENGTH_SHORT).show());
                    }catch (JSONException e){
                      runOnUiThread(()-> Toast.makeText(this, "Could not retrieve", Toast.LENGTH_SHORT).show());
                    }
        }
          );
            thread.start();

          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // Create the transformable andy and add it to the anchor.
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());

          andy.setParent(anchorNode);

          andy.setRenderable(andyRenderable);
          andy.select();
        });
  }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }

  private void DownloadMarker(String marker_uuid){
      API apiCall = new API();
      AssetNodes = new ArrayList<>();
      if (augmentedImageDatabase == null)
          augmentedImageDatabase = new AugmentedImageDatabase(Objects.requireNonNull(arFragment.getArSceneView().getSession()));

      Thread thread = new Thread(()->{
          runOnUiThread(()-> Toast.makeText(this, "starting DownloadMarker", Toast.LENGTH_SHORT).show());
          String marker_url = apiCall.fetchTargetMarkerURL(marker_uuid);
          Log.d(TAG,marker_url);

          Bitmap bitmap = apiCall.DownloadImageToBitmap(marker_url);

          if (bitmap.getHeight()!=0){
              runOnUiThread(()-> Toast.makeText(this, "there is a bitmap present of height " +bitmap.getHeight(), Toast.LENGTH_SHORT).show());
          }

          AugmentedImageBitmap = bitmap;
          current_uuid = marker_uuid;

          metaData = apiCall.fetchARExperience(marker_uuid);

          runOnUiThread(() -> splashNode = new ExperienceSplashImageNode(this, AugmentedImageBitmap.getWidth(), AugmentedImageBitmap.getHeight()));
          String ImageName = "Marker";
          try {
              JSONObject reader = new JSONObject(metaData);
              ImageName = reader.getString("name"); //name of the marker

              //set name of the marker
              if (ImageName.length() > 20)
                  ImageName = ImageName.substring(0, 19) + "...";
              //userInterface.getPosterName().setText(ImageName);
          } catch (JSONException ignored) {
          }

          Session session = arFragment.getArSceneView().getSession();// reconfiguring ARCore session to include the new image

          assert session != null;
          Config config = session.getConfig();

          Log.d(TAG, "DownloadMarker: " + AugmentedImageBitmap.getWidth());
          float markerWidth = bitmap.getWidth() * 0.001f;
          augmentedImageDatabase.addImage(ImageName, AugmentedImageBitmap, markerWidth);
          Log.d(TAG, "DownloadMarker: MARKER WIDTH: " + bitmap.getWidth() + "MARKER HEIGHT: " + bitmap.getHeight());
         // config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
          config.setAugmentedImageDatabase(augmentedImageDatabase);
          session.configure(config); //Pushing the new configuration into the current ARCore settings
          arFragment.getArSceneView().setupSession(session);
          runOnUiThread(()-> Toast.makeText(this, Integer.toString(augmentedImageDatabase.getNumImages()), Toast.LENGTH_SHORT).show());
          SetUpExperience();
      });
      thread.start();
  }
public void SetUpExperience(){
      Thread getDataThread = new Thread(()->{
          Log.e(TAG, "SetupExperience: " + current_uuid);
          Log.d(TAG, metaData); //meta data contains JSON of all the data associated with each component of the experience
//            JSONArray arContent;
          AnalyticsManager.setExperienceUUID(current_uuid);
          try {
              JSONArray assetInfo = new JSONObject(metaData).getJSONArray("asset_transform_info");
              if (assetInfo.length()==0){
                  runOnUiThread(()-> Toast.makeText(this, "No asset info", Toast.LENGTH_SHORT).show());
              }
              for (int i = 0; i < assetInfo.length(); i++) {
                  JSONObject assetInfoObject = assetInfo.getJSONObject(i);
                  String uuid = assetInfoObject.getString("uuid");
                  String type = assetInfoObject.getString("type");

                  try {
                      if ((type.equals("message") || type.equals("link"))) {
                          if (assetInfoObject.getString("text").isEmpty())
                              continue;
                      }

                      if (!(type.equals("image") || type.equals("text"))) {
                          AnalyticsManager.addEngagementAnalytic(uuid);
                      }
                  } catch (JSONException e){

                  }


                  runOnUiThread(() -> {
                      AugmentedAssetNode node = new AugmentedAssetNode(this, assetInfoObject);
                      AssetNodes.add(node);
                      arFragment.getArSceneView().getScene().addChild(node);
                  });
              }
          }catch (JSONException e){
              e.printStackTrace();
          }

      });
      getDataThread.start();
  }
}

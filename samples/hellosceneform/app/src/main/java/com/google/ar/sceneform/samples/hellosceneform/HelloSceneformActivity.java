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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
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
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager;
import com.google.ar.sceneform.samples.hellosceneform.AssetNodes.AugmentedAssetNode;
import com.google.ar.sceneform.samples.hellosceneform.AssetNodes.ExperienceSplashImageNode;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.samples.hellosceneform.utils.API;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
  protected boolean setupStarted = false;
protected boolean experienceLoaded = false;
protected boolean splashSetup = false;
public boolean shouldWatermark = false;

  @RequiresApi(api = VERSION_CODES.P)
  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    Thread startThread = new Thread(()->{
        String test_string = api.fetchARExperienceByExpID("d4f74b5e-1ddf-11ea-9a0b-1fd7f8b74283");
        Log.d("THREAD",test_string);
        try {
            JSONObject reader = new JSONObject(test_string);
            String marker_uuid = reader.getString("marker_uuid");
            Log.d("START_THREAD",marker_uuid);
            String response = api.fetchARExperience(marker_uuid);
            Thread inner_thread = new Thread(()->{
                String marker_url = api.fetchTargetMarkerURL(marker_uuid);
                Bitmap bitmap = api.DownloadImageToBitmap(marker_uuid);
                try {
                    JSONArray assetInfo = new JSONObject(metaData).getJSONArray("asset_transform_info");
                    if (assetInfo.length()==0){
                        runOnUiThread(()-> Toast.makeText(this, "No asset info", Toast.LENGTH_SHORT).show());
                    }
                    for (int i = 0; i < assetInfo.length(); i++) {
                        JSONObject assetInfoObject = assetInfo.getJSONObject(i);
                        String uuid = assetInfoObject.getString("uuid");
                        String type = assetInfoObject.getString("type");
                        String source = assetInfoObject.getString("source");
                        Log.d("type",type);

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
                            AugmentedAssetNode node = new AugmentedAssetNode(this, assetInfoObject);
                            AssetNodes.add(node);
                            if (AssetNodes.size() > 0){
                                Log.d("START_THREAD","SIZE IS NON-ZERO");
                            }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Log.d("AssetNodes:",Integer.toString(AssetNodes.size()));
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


    });

    startThread.start();

    setContentView(R.layout.activity_ux);
    arFragment = (CustomARFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    Log.d("TAG", "Custom Fragment created");

    //runOnUiThread(()-> Toast.makeText(this, "Now Downloading Marker", Toast.LENGTH_SHORT).show());
   // DownloadMarker("d4f74b5e-1ddf-11ea-9a0b-1fd7f8b74283");

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


    ImageView view = new ImageView(this);
    view.setImageDrawable(getResources().getDrawable(R.drawable.simp));

    AtomicReference<ViewRenderable> viewrender = null;

    ViewRenderable.builder()
            .setView(this,view)
            .build()
    .thenAccept(renderable -> testRenderable = renderable)
            .exceptionally(
                    throwable -> {
                        Toast.makeText(this, "haha no", Toast.LENGTH_SHORT).show();
                        return null;
                    }
            );

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
                      //DownloadMarker(marker_uuid);
                 //     runOnUiThread(()-> Toast.makeText(this, marker_uuid, Toast.LENGTH_SHORT).show());
                      Thread fetchThread = new Thread(()-> {
                          DownloadMarker(marker_uuid);

                      });
                      fetchThread.start();

                    }catch (JSONException e){
                      runOnUiThread(()-> Toast.makeText(this, "Could not retrieve", Toast.LENGTH_SHORT).show());
                    }
        }
          );
            thread.start();
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode((anchor));
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            TransformableNode simp = new TransformableNode(arFragment.getTransformationSystem());
            simp.setParent(anchorNode);
            simp.setRenderable(testRenderable);
            simp.select();



//          // Create the Anchor.
//          Anchor anchor = hitResult.createAnchor();
//          AnchorNode anchorNode = new AnchorNode(anchor);
//          anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//          // Create the transformable andy and add it to the anchor.
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//
          andy.setParent(anchorNode);
//
          andy.setRenderable(andyRenderable);
//          andy.select();
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

    public void createAlertDialog(String title, String message, String btnText, String postAction) {
        AlertDialog myAlert;
        myAlert = new AlertDialog.Builder(this).create();
        myAlert.setCancelable(false);
        myAlert.setTitle(title);
        myAlert.setMessage(message);
        myAlert.setButton(AlertDialog.BUTTON_NEGATIVE, btnText,
                (dialog, which) -> {
                    switch (postAction) {
                        case "QUIT":
                            finishAffinity();
                            break;
                        case "DISMISS":
                            myAlert.dismiss();
                            break;
                        case "blueRectScan()":
                    //        runOnUiThread(() -> userInterface.setBlueRectIntro());
                            break;
                        case "update":
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kreatar.postrealityviewer")));
                            myAlert.dismiss();
                            break;
                    }

                });
        Objects.requireNonNull(myAlert.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//        myAlert.show();
        myAlert.show();
    }

  @RequiresApi(api = VERSION_CODES.P)
  private void DownloadMarker(String marker_uuid){
      API apiCall = new API();
      AssetNodes = new ArrayList<>();
      if (augmentedImageDatabase == null)
          augmentedImageDatabase = new AugmentedImageDatabase(Objects.requireNonNull(arFragment.getArSceneView().getSession()));

      Thread thread = new Thread(()->{
          String marker_url = apiCall.fetchTargetMarkerURL(marker_uuid);
          Log.d(TAG,marker_url);

          Bitmap bitmap = apiCall.DownloadImageToBitmap(marker_url);
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
          try {
              JSONArray arContent = new JSONObject(metaData).getJSONArray("asset_transform_info");//Reading the metaData JSON
              int numberOfARAssets = arContent.length();
              runOnUiThread(()-> Toast.makeText(this, "There are " + numberOfARAssets + " AR assets", Toast.LENGTH_SHORT).show());
              if (numberOfARAssets == 0) {
                  runOnUiThread(() -> {
                      createAlertDialog("No Components Found", "An experience has been found, but there are no components", "Dismiss", "blueRectScan()");
                    //  scanningThread.stopScanAnimation();
                      //ResetState();
//                        takePhoto();
                  });
                  return;
              }
          } catch (Exception e) {
              runOnUiThread(() -> {
                  Log.e(TAG, "Error: " + e);
                  //createAlertDialog("No Components Found", "An experience has been found, but there are no components", "Dismiss", "blueRectScan()");
                  //runOnUiThread(() -> scanningThread.stopScanAnimation());
                  //ResetState();
//                    takePhoto();
              });
              return;
          }
          String finalImageName = ImageName;

       //   runOnUiThread(()-> Toast.makeText(this, finalImageName, Toast.LENGTH_SHORT).show());
//
//          runOnUiThread(()->{
//              Toast toast = new Toast(this);
//              ImageView view = new ImageView(this);
//              view.setImageBitmap(AugmentedImageBitmap);
//              toast.setView(view);
//              toast.show();
//                  }
//          );


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
          SetupExperience();
         // arFragment.getArSceneView().getScene().addOnUpdateListener(this::SetupExperience);
      });
      thread.start();
  }

    @RequiresApi(api = VERSION_CODES.P)
    public void SetupExperience(){
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
                  String source = assetInfoObject.getString("source");
                  Log.d("SOURCE",source);

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
                  });

                  Log.d("AssetNodes:",Integer.toString(AssetNodes.size()));
              }
          }catch (JSONException e){
              e.printStackTrace();
          }

      });
      getDataThread.start();
  }
}

package com.google.ar.sceneform.samples.hellosceneform.AssetNodes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.samples.hellosceneform.HelloSceneformActivity;
import com.google.ar.sceneform.samples.hellosceneform.utils.API;
import com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager;
import com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager;
import com.google.ar.sceneform.samples.hellosceneform.R;
import java.util.concurrent.CompletableFuture;

public class ExperienceSplashImageNode extends AnchorNode {
    private static final String TAG = "ExperienceSplashImageNode";
//    private int ViewId;

    private int xPos, yPos, zPos;
    private float xScl, yScl, zScl;
    private float width, height;
    private float aspectRatio;

    //for GA...get the manager analytic and the UUID of the object
    private com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager AnalyticsManager = HelloSceneformActivity.AnalyticsManager;
    private String audioUUID,videoUUID, linkUUID, messageUUID;


    private boolean Video;
    private ImageView imageView;
    private ImageView imageView2;

    private MediaPlayer mediaPlayer;
    private CompletableFuture<ViewRenderable> BackgroundAssetNode;
    private  CompletableFuture<ViewRenderable> TitleTextAssetNode;
    private  CompletableFuture<ViewRenderable> SpinnerAssetNode;



    private AugmentedImage currentImage;

    public Thread thread;
    private Node BackgroundPositionNode;
    private Node TitleTextPositionNode;
    private AnchorNode anchorNode;
    private Context activityContext;

    private String text;
    private String finalText;

    private float markerHeight;
    private float markerWidth;

    private float componentScale;

    @TargetApi(Build.VERSION_CODES.P)
    public ExperienceSplashImageNode(Context context, float markerWidth, float markerHeight) {
        Activity activity = (Activity) context;
        activityContext = context;

        API apiCall = new API();
        width = 1;
        height = 1;
        componentScale = 1;
        Video = false;

        xPos = 1;
        yPos = 1;
        zPos = 1;
        xScl = 1;
        yScl = 1;
        zScl = 1;



//        width = 0;
//        height = 0;


        aspectRatio = markerHeight / markerWidth;
        this.markerHeight = markerHeight;
        this.markerWidth = markerWidth;
//        width = 0.76f;
//        height = 0.76f * aspectRatio;

        Log.d(TAG, "ExperienceSplashImageNode: " + aspectRatio);
        CreateImageAsset(context);
    }

    private void CreateImageAsset(Context context) {
//        Activity activity = (Activity) context;
        Activity activity = (Activity) activityContext;
        activity.runOnUiThread(() -> {
            imageView = new ImageView(context);
//
//            if (bitmap == null)
//                imageView.setImageResource(resID);
//            else
//                imageView.setImageBitmap(bitmap);

            imageView.setImageResource(R.drawable.experience_blue_backdrop);

            imageView.requestLayout();
            imageView.setAdjustViewBounds(true);

//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (width * 45), (int) (height * 45 * aspectRatio));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(layoutParams);
//            imageView.setAlpha(1f);
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageView.getLayoutParams().height = (int) (aspectRatio * imageView.getLayoutParams().height);

            Log.d(TAG, "CreateImageAsset: " + imageView.getLayoutParams().width);
            Log.d(TAG, "CreateImageAsset: " + imageView.getLayoutParams().height);

//            RelativeLayout view = activity.findViewById(R.id.experienceSplash);

            imageView.requestLayout();
            BackgroundAssetNode = null;
//            Log.d(TAG, "run: " + ViewId);
//            if ((width == 0) && (height == 0)) {
            BackgroundAssetNode = ViewRenderable.builder()
                    .setView(context, imageView)
                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                    .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                    .build();
//            } else {
//                BackgroundAssetNode = ViewRenderable.builder()
//                        .setView(context, imageView)
//                        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
//                        .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
//                        .setSizer(new FixedWidthViewSizer(width))
//                        .setSizer(new FixedHeightViewSizer(height))
//                        .build();
//            }
//            Log.d(TAG, AssetNode.toString());

            imageView2 = new ImageView(context);
//
//            if (bitmap == null)
//                imageView.setImageResource(resID);
//            else
//                imageView.setImageBitmap(bitmap);

            imageView2.setImageResource(R.drawable.experience_splash_logo);

            imageView2.requestLayout();
            imageView2.setAdjustViewBounds(true);

            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(layoutParams2);

            imageView.setAlpha(1f);
            imageView2.setAlpha(1f);
            TitleTextAssetNode = null;
//            Log.d(TAG, "run: " + ViewId);
            TitleTextAssetNode = ViewRenderable.builder()
                    .setView(context, imageView2)
                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                    .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                    .build();
        });
    }

    private void _doReplace() {
        if (currentImage == null)
            return;

        if (currentImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            RemoveAnchor(activityContext);
            setImage(currentImage);
        } else {
            RemoveAnchor(activityContext);
            HelloSceneformActivity.tracking = false;
        }
    }

    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        currentImage = image;

        if (BackgroundAssetNode == null)
            return;

        if (!BackgroundAssetNode.isDone()) {
            CompletableFuture.allOf(BackgroundAssetNode)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        if (TitleTextAssetNode == null)
            return;

        if (!TitleTextAssetNode.isDone()) {
            CompletableFuture.allOf(TitleTextAssetNode)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        Anchor anchor = image.createAnchor((image.getCenterPose()));

//        if (image.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
//            try {
//                anchor = image.createAnchor(image.getCenterPose());
//            } catch (Exception e) {
//                RemoveAnchor();
//                MainActivity.tracking = false;
//                return;
//            }
//        } else {
//            RemoveAnchor();
//            MainActivity.tracking = false;
//            return;
//        }

        setAnchor(anchor);

        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(this);
        xScl = currentImage.getExtentX();
        zScl = currentImage.getExtentZ();

//        Log.d(TAG, "setImage: " + xScl + " z: " + zScl);
        BackgroundPositionNode = new Node();
        BackgroundPositionNode.setParent(anchorNode);
        BackgroundPositionNode.setLocalPosition(new Vector3(0, 0, 0));
        BackgroundPositionNode.setLocalRotation(new Quaternion(new Vector3(90, 0, 180)));
        BackgroundPositionNode.setLocalScale(new Vector3(0.74f * markerWidth * 0.001f, 0.74f * markerHeight * 0.001f, 0.74f));
        BackgroundPositionNode.setRenderable(BackgroundAssetNode.getNow(null));

        TitleTextPositionNode = new Node();
        TitleTextPositionNode.setParent(anchorNode);
        TitleTextPositionNode.setLocalPosition(new Vector3(0, 0.1f, 0));
        TitleTextPositionNode.setLocalRotation(new Quaternion(new Vector3(90, 0, 180)));
        TitleTextPositionNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        TitleTextPositionNode.setRenderable(TitleTextAssetNode.getNow(null));

//        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
//        animator.addUpdateListener(animation -> {
//            imageView.setAlpha((float) animation.getAnimatedValue());
//            imageView2.setAlpha((float) animation.getAnimatedValue());
//        });
//        animator.setDuration(2000);
//        animator.start();
//        Thread thread = new Thread(() -> {
//            float alpha = 1f;
//            while (alpha > 0){
//                alpha = alpha - 0.025f;
//                imageView.setAlpha(alpha);
//                imageView2.setAlpha(alpha);
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });
//        thread.start();
    }

    public void RemoveAnchor(Context context) {
        Activity activity = (Activity) context;

        activity.runOnUiThread(() -> {
            if (anchorNode != null && anchorNode.getAnchor() != null) {
                anchorNode.getAnchor().detach();
                anchorNode.setAnchor(null);
                anchorNode.setParent(null);
                anchorNode = null;
            }

            if (BackgroundPositionNode != null) {
                BackgroundPositionNode.setParent(null);
                BackgroundPositionNode.setRenderable(null);
                BackgroundPositionNode = null;
            }

            if (TitleTextPositionNode != null) {
                TitleTextPositionNode.setParent(null);
                TitleTextPositionNode.setRenderable(null);
                TitleTextPositionNode = null;
            }
        });
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        //TODO: Other Cleanup
    }
}

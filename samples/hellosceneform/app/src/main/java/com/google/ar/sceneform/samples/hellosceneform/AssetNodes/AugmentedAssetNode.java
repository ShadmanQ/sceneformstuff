package com.google.ar.sceneform.samples.hellosceneform.AssetNodes;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.FixedHeightViewSizer;
import com.google.ar.sceneform.rendering.FixedWidthViewSizer;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.samples.hellosceneform.HelloSceneformActivity;
import com.google.ar.sceneform.samples.hellosceneform.utils.API;
import com.google.ar.sceneform.samples.hellosceneform.R;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.google.ar.sceneform.samples.hellosceneform.utils.Constants.getInstance;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.copySign;
import static java.lang.Math.log;
import static java.lang.Math.toDegrees;
import static java.lang.Thread.sleep;


public class AugmentedAssetNode extends AnchorNode {
    private static final String TAG = "AugmentedAssetNode";
//    private int ViewId;

    private float xPos, yPos, zPos;
    private float xScl, yScl, zScl;
//    private float[] qRot = new float[4];

    private static class qRot {
        float x;
        float y;
        float z;
        float w;
    }

    private static class eRot {
        double x;
        double y;
        double z;
    }

    private eRot rot = new eRot();
    private qRot q = new qRot();
    private float width, height;


    //for GA...get the manager analytic and the UUID of the object
    private com.google.ar.sceneform.samples.hellosceneform.utils.AnalyticsManager AnalyticsManager = HelloSceneformActivity.AnalyticsManager;
    private String audioUUID, videoUUID, linkUUID, messageUUID;


    private boolean Video;
    private boolean gif;
    private boolean autoPlay;

    private Drawable drawable;
    private ImageView imageView;
    private ImageView playButton;
    private MediaPlayer mediaPlayer;
    private ExternalTexture externalTexture;
    @Nullable
    private ModelRenderable VideoNode;
    private CompletableFuture<ViewRenderable> AssetNode;

    private CompletableFuture<ViewRenderable> playButtonNode;


    //for background
    private boolean wasRunning;
    private int pauseTime;
//    private ModelRenderable VideoRenderable;


    private AugmentedImage currentImage;

    public Thread thread;
    private ValueAnimator audioGlow;
    private Node playButtonPositionNode = null;
    private Node positionNode;
    private AnchorNode anchorNode;
    private Context activityContext;

    private String text;
    private String finalText;

    private float componentScale;


    @TargetApi(Build.VERSION_CODES.P)
    public AugmentedAssetNode(Context context, JSONObject assetInfo) {
        Activity activity = (Activity) context;
        activityContext = context;

        API apiCall = new API();
        width = 0;
        height = 0;
        componentScale = 1;

        try {
            xPos = (float) assetInfo.getJSONObject("position").getDouble("x");
            yPos = (float) assetInfo.getJSONObject("position").getDouble("y");
            zPos = (float) assetInfo.getJSONObject("position").getDouble("z");
            xScl = (float) assetInfo.getJSONObject("scale").getDouble("x");
            yScl = (float) assetInfo.getJSONObject("scale").getDouble("y");
            zScl = (float) assetInfo.getJSONObject("scale").getDouble("z");

            q.x = (float) assetInfo.getJSONObject("rotation").getDouble("x");
            q.y = (float) assetInfo.getJSONObject("rotation").getDouble("y");
            q.z = (float) assetInfo.getJSONObject("rotation").getDouble("z");
            q.w = (float) assetInfo.getJSONObject("rotation").getDouble("w");

            rot = ConvertToEuler(q);
        } catch (JSONException e) {
            Log.e(TAG, "AugmentedAssetNode: ", e);
        }

        try{
            width = (float) assetInfo.getJSONObject("geometry").getDouble("width") * 0.001f;
            height = (float) assetInfo.getJSONObject("geometry").getDouble("height") * 0.001f;
        } catch (JSONException e){
        width = 0;
        height = 0;
        }

        Video = false;
        gif = false;
        autoPlay = false;

        try {
            switch (assetInfo.getString("type")) {
                case "image":

                    componentScale = 1.2f;

                    CreateImageAsset(context, R.drawable.image_icon, null);
//                    if(assetInfo.getBoolean("isGif")){
//                    } else {
                        Log.d(TAG, "AugmentedAssetNode: FALSE");
                        thread = new Thread(() -> {
                            String uuid = null;
                            try {
                                uuid = assetInfo.getString("uuid");
                                Log.d("UUID_AAN",uuid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String url = apiCall.fetchARContentAssetURL(uuid);
                            Bitmap bitmap = apiCall.DownloadImageToBitmap(url);
//                            activity.runOnUiThread(()->{
//                                Toast toast = new Toast(activityContext);
//                                ImageView view = new ImageView(activityContext);
//                                view.setImageBitmap(bitmap);
//                                toast.setView(view);
//                                toast.show();
//                            });
//                        CreateNode(context, R.drawable.image_icon, bitmap);

                            CreateImageAsset(context, 0, bitmap);

                            try {
                                sleep(20);
                                activity.runOnUiThread(this::_doReplace);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                        thread.start();
//                    }
                    break;
                case "video":
                    externalTexture = new ExternalTexture();
                    CreateImageAsset(context, R.drawable.video_icon, null);
                    if (assetInfo.getBoolean("autoPlay"))
                        autoPlay = true;

                    thread = new Thread(() -> {
                        componentScale = 5;
                        try {
                            String uuid = assetInfo.getString("uuid");

                            //for GA
                            videoUUID = uuid;

                            String url = apiCall.fetchARContentAssetURL(uuid);
                            mediaPlayer = MediaPlayer.create(context, Uri.parse(url));
//                            mediaPlayer.setDataSource(url);
                            Log.d(TAG, "AugmentedAssetNode: " + url);
                            mediaPlayer.setSurface(externalTexture.getSurface());


                            if (assetInfo.getBoolean("loop")) {
                                mediaPlayer.setLooping(true);
                            } else {
                                mediaPlayer.setLooping(false);
                                mediaPlayer.setOnCompletionListener(arg0 -> AnalyticsManager.pauseRecordingEngagement(videoUUID));
                            }

                            float videoWidth = mediaPlayer.getVideoWidth() * 0.001f;
                            float videoHeight = mediaPlayer.getVideoHeight() * 0.001f;
                            if(videoWidth > videoHeight){
                                componentScale = videoHeight * componentScale;
                            }
                            else {
                                componentScale = videoWidth * componentScale;
                            }

                            //add this once the videos is ready... will be used for background
                            HelloSceneformActivity.audioAndVideoNodes.add(this);
                            Log.d("AugmentedAssetNode","An element has been added to the list");
                            wasRunning = false;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        activity.runOnUiThread(() -> {
                            playButton = new ImageView(context);
                            playButton.setImageResource(R.drawable.play_button);

                            playButton.requestLayout();
                            playButton.setAdjustViewBounds(true);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER_VERTICAL;
                            playButton.setLayoutParams(layoutParams);

                            playButton.setOnClickListener(v -> ToggleMedia());

                            playButtonNode = ViewRenderable.builder()
                                    .setView(context, playButton)
                                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                                    .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                                    .build();

                            ModelRenderable.builder()
                                    .setSource(context, R.raw.chroma_key_video)
                                    .build()
                                    .thenAccept(
                                            renderable -> {
                                                VideoNode = renderable;
                                                renderable.getMaterial().setExternalTexture("videoTexture", externalTexture);
                                            })
                                    .exceptionally(
                                            throwable -> null
                                    );
                            try {
                                sleep(20);
                                Video = true;
                                _doReplace();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    });
                    thread.start();
                    break;
                case "audio":
                    //for background
                    HelloSceneformActivity.audioAndVideoNodes.add(this);
                    wasRunning = false;
                    CreateImageAsset(context, R.drawable.audio_icon, null);

                    thread = new Thread(() -> {
                        String uuid = null;
                        try {
                            uuid = assetInfo.getString("uuid");
                            //for GA
                            audioUUID = uuid;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String url = apiCall.fetchARContentAssetURL(uuid);

                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try {
                            mediaPlayer.setDataSource(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mediaPlayer.prepare(); // might take long! (for buffering, etc)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //add to here only once it is prepared...will vbe used for background
                        HelloSceneformActivity.audioAndVideoNodes.add(this);
                        wasRunning = false;

                        // @Override
                        try {
                            if (assetInfo.getBoolean("loop")) {
                                mediaPlayer.setLooping(true);
                            } else {
                                mediaPlayer.setLooping(false);
                                mediaPlayer.setOnCompletionListener(arg0 -> AnalyticsManager.pauseRecordingEngagement(videoUUID));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        audioGlow = ValueAnimator.ofFloat(1f, 0.9f);

                        audioGlow.addUpdateListener(animation -> imageView.setAlpha((float) animation.getAnimatedValue()));

                        audioGlow.setDuration(1000);
                        audioGlow.setRepeatCount(-1);
                        audioGlow.setRepeatMode(ValueAnimator.REVERSE);

                        activity.runOnUiThread(() -> {

                            playButton = new ImageView(context);
                            playButton.setImageResource(R.drawable.play_button);

                            playButton.requestLayout();
                            playButton.setAdjustViewBounds(true);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.CENTER_VERTICAL;
                            playButton.setLayoutParams(layoutParams);

                            playButtonNode = ViewRenderable.builder()
                                    .setView(context, playButton)
                                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                                    .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                                    .build();

                            playButton.setOnClickListener(v -> ToggleMedia());
                            imageView.setOnClickListener(v -> ToggleMedia());
                        });
                    });
                    thread.start();


                    break;
                case "text":

                    text = assetInfo.getString("source");
                    String textColor = assetInfo.getString("fontColor");
//                    int aColor = assetInfo.getJSONObject("asset").getJSONObject("color").getInt("a") * 255;
//                    int bColor = assetInfo.getJSONObject("asset").getJSONObject("color").getInt("b");
//                    int gColor = assetInfo.getJSONObject("asset").getJSONObject("color").getInt("g");
//                    int rColor = assetInfo.getJSONObject("asset").getJSONObject("color").getInt("r");
                    String alignment = assetInfo.getString("align");
//                    Log.d(TAG, "AugmentedAssetNode: COLOR: " + aColor + "," + rColor + "," + gColor + "," + bColor);

                    componentScale = 2;

                    activity.runOnUiThread(() -> {
//                        TextView textView = new TextView(context);

//                        textView.setText(text);
//                        textView.setTextSize(40);
//                        textView.setTextColor(Color.argb(aColor, rColor, gColor, bColor));
//                        Typeface typeface = activity.getResources().getFont(R.font.lato_regular);
//                        textView.setTypeface(typeface);
//                        textView.setGravity(Gravity.CENTER);
//                        textView.setMaxEms(10000);


//                        textView.setWidth(10000);
//                        textView.setHeight(10000);
//                        textView.setLineHeight(1500);

//                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
////                        layoutParams.gravity = Gravity.CENTER;
//                        textView.setLayoutParams(layoutParams);
////                        textView.setSingleLine(true);
//                        final TextView textView = activity.findViewById(R.id.assetText);
//                        textView.setText(text);
//                        textView.setTextColor(Color.argb(aColor, rColor, gColor, bColor));

                        RelativeLayout relativeLayout = new RelativeLayout(context);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        relativeLayout.setLayoutParams(params);

                        TextView textView = new TextView(context);
                        textView.setText(text);
                        textView.setTextSize(15);
                        textView.setTextColor(Color.parseColor(textColor));
                        Typeface typeface = activity.getResources().getFont(R.font.lato_regular);
                        textView.setTypeface(typeface);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        textView.setShadowLayer(1.5f, -1, 1, Color.LTGRAY);
//                        layoutParams.gravity = Gravity.CENTER;
                        textView.setLayoutParams(layoutParams);

                        if (alignment.equals("left"))
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        else if (alignment.equals("right"))
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        else
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

//                        RelativeLayout textLayout = activity.findViewById(R.id.text_layout);
//                        textLayout.addView(textView);
                        relativeLayout.addView(textView);


                        AssetNode = ViewRenderable.builder()
                                .setView(context, relativeLayout)
                                .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                                .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                                .build();
                    });
                    break;
                case "link":
                    CreateImageAsset(context, R.drawable.link_icon, null);

                    activity.runOnUiThread(() -> {
                        try {
                            linkUUID = assetInfo.getString("uuid");
//                            if (assetInfo.getJSONObject("asset").has("text")) {
                            text = assetInfo.getString("hyperlink");
//                            } else {
//                                text = assetInfo.getJSONObject("asset").getString("source");
//                            }

                            if (text != null && !text.equals("") && !text.contains("http"))
                                text = "https://".concat(text);
                            finalText = text;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (finalText != null && !finalText.equals("")) {
                            imageView.setOnClickListener(v -> {
                                Uri uri = Uri.parse(finalText); // missing 'http://' will cause crashed
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                AnalyticsManager.addClick(linkUUID);
                                activity.startActivity(intent);
                            });
                        }

                    });

//                    if (!assetInfo.getJSONObject("asset").getString("source").contains("blob"))
//                        return;

                    thread = new Thread(() -> {
                        String uuid = null;
                        try {
                            uuid = assetInfo.getString("uuid");

                            //for GA
                            linkUUID = uuid;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String url = apiCall.fetchARContentAssetURL(uuid);
                        Bitmap bitmap = apiCall.DownloadImageToBitmap(url);

//                        CreateNode(context, R.drawable.image_icon, bitmap);

                        CreateImageAsset(context, 0, bitmap);

                        activity.runOnUiThread(() -> {
                            if (finalText != null && !finalText.equals("")) {
                                imageView.setOnClickListener(v -> {
                                    Uri uri = Uri.parse(finalText); // missing 'http://' will cause crashed
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    activity.startActivity(intent);

//                                for GA..adds one to clicks on link
//                                    AnalyticsManager.addClick(linkUUID);
                                });
                            }
                            try {
                                sleep(20);
                                _doReplace();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    });
                    thread.start();
                    break;
                case "message":
                    CreateImageAsset(context, R.drawable.message_icon, null);

                    activity.runOnUiThread(() -> {
                        try {
//                            if (assetInfo.getJSONObject("asset").has("text")) {
                            text = assetInfo.getString("hyperlink");

//                            } else {
//                                text = assetInfo.getJSONObject("asset").getString("source");
//                            }

                            if (text != null && !text.equals("") && !text.contains("mailto"))
                                text = "mailto:".concat(text);

                            finalText = text;
                        } catch (JSONException e) {
                            text = null;
                            e.printStackTrace();
                        }

                        if (finalText != null && !finalText.equals("")) {
                            imageView.setOnClickListener(v -> {
                                Uri uri = Uri.parse(finalText); // missing 'http://' will cause crashed
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                AnalyticsManager.addClick(messageUUID);
                                activity.startActivity(intent);
                            });
                        }
                    });

//                    if (!assetInfo.getJSONObject("asset").getString("source").contains("blob"))
//                        return;

                    thread = new Thread(() -> {

                        String uuid = null;
                        try {
                            uuid = assetInfo.getString("uuid");

                            //for GA
                            messageUUID = uuid;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String url = apiCall.fetchARContentAssetURL(uuid);
                        Bitmap bitmap = apiCall.DownloadImageToBitmap(url);
//                        CreateNode(context, R.drawable.image_icon, bitmap);

                        CreateImageAsset(context, 0, bitmap);

                        activity.runOnUiThread(() -> {
                            if (finalText != null && !finalText.equals("")) {
                                imageView.setOnClickListener(v -> {
                                    Uri uri = Uri.parse(finalText); // missing 'http://' will cause crashed
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    AnalyticsManager.addClick(messageUUID);
                                    activity.startActivity(intent);
                                });
                            }

                            try {
                                sleep(20);
                                _doReplace();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    });
                    thread.start();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private eRot ConvertToEuler(qRot q) {
        eRot rot = new eRot();

        double sinr_cosp = +2.0 * (q.w * q.w);
        double cosr_cosp = +1.0 - 2.0 * (q.x * q.x + q.y * q.y);
        double temp1 = toDegrees(atan2(sinr_cosp, cosr_cosp));

        double sinp = +2.0 * (q.w * q.y - q.z * q.x);
        double temp2;
        if (abs(sinp) >= 1) {
            temp2 = toDegrees(copySign(PI / 2, sinp));
        } else {
            temp2 = toDegrees(asin(sinp));
        }

        double siny_cosp = +2.0 * (q.w * q.z + q.x * q.y);
        double cosy_cosp = +1.0 - 2.0 * (q.y * q.y + q.z * q.z);
        double temp3 = toDegrees(atan2(siny_cosp, cosy_cosp));

        rot.z = temp1;
        rot.y = temp2;
        rot.x = temp3;
        Log.e(TAG, "ConvertToEuler:  x - " + rot.x + " y - " + rot.y + " z - " + rot.z);
        return rot;
    }

    private void CreateImageAsset(Context context, int resID, @Nullable Bitmap bitmap) {
//        Activity activity = (Activity) context;
        Activity activity = (Activity) activityContext;
        activity.runOnUiThread(() -> {
            imageView = new ImageView(context);

            if (bitmap == null)
                imageView.setImageResource(resID);
            else
                imageView.setImageBitmap(bitmap);

            imageView.requestLayout();
            imageView.setAdjustViewBounds(true);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(layoutParams);

//            Log.d(TAG, AssetName + ":- Width: " + width + "  height:" + height);

            AssetNode = null;
//            Log.d(TAG, "run: " + ViewId);
            if (bitmap == null) {

                float defaultIconWidth = getInstance().getDefaultScale();
                float defaultIconHeight = getInstance().getDefaultScale();

                AssetNode = ViewRenderable.builder()
                        .setView(context, imageView)
                        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                        .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                        .setSizer(new FixedWidthViewSizer(defaultIconWidth))
                        .setSizer(new FixedHeightViewSizer(defaultIconHeight))
                        .build();
            } else if ((width == 0) && (height == 0)) {
                AssetNode = ViewRenderable.builder()
                        .setView(context, imageView)
                        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                        .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                        .build();
            } else {
                AssetNode = ViewRenderable.builder()
                        .setView(context, imageView)
                        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                        .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                        .setSizer(new FixedWidthViewSizer(width))
                        .setSizer(new FixedHeightViewSizer(height))
                        .build();
            }
        });

        Log.d("CREATEIMAGEASSET","Asset has been created");
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        currentImage = image;

        if (Video) {
            if (mediaPlayer == null)
                return;
        }

        if (AssetNode == null)
            return;

        if (!AssetNode.isDone()) {
            CompletableFuture.allOf(AssetNode)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        Anchor anchor;

        if (image.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            try {
                anchor = image.createAnchor(image.getCenterPose());
            } catch (Exception e) {
                RemoveAnchor(activityContext);
                HelloSceneformActivity.tracking = false;
                return;
            }

        } else {
            RemoveAnchor(activityContext);
            HelloSceneformActivity.tracking = false;
            return;
        }


        setAnchor(anchor);

        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(this);

        positionNode = new Node();
        positionNode.setParent(anchorNode);
        positionNode.setLocalPosition(new Vector3(xPos * getInstance().getPositionScale(), -zPos * getInstance().getPositionScale(), -yPos * getInstance().getPositionScale()));
        positionNode.setLocalRotation(new Quaternion(new Vector3(90, (float) rot.x, 180)));
        positionNode.setLocalScale(new Vector3(xScl * getInstance().getScale() * componentScale, zScl * getInstance().getScale() * componentScale, yScl * getInstance().getScale() * componentScale));

        if(playButton != null){

            if(!playButtonNode.isDone()) {
                CompletableFuture.allOf(playButtonNode)
                        .thenAccept((Void aVoid) -> {
                            setImage(image);
                            if(playButtonPositionNode == null) {
                            componentScale = componentScale * 0.2f;
                            playButtonPositionNode = new Node();
                            playButtonPositionNode.setParent(anchorNode);
                            playButtonPositionNode.setLocalPosition(new Vector3(xPos * getInstance().getPositionScale(), -zPos * getInstance().getPositionScale() + 0.01f, -yPos * getInstance().getPositionScale()));
                            playButtonPositionNode.setLocalRotation(new Quaternion(new Vector3(90, (float) rot.x, 180)));
                            playButtonPositionNode.setLocalScale(new Vector3(xScl * getInstance().getScale() * componentScale, zScl * getInstance().getScale() * componentScale, yScl * getInstance().getScale() * componentScale));
                            playButtonPositionNode.setRenderable(playButtonNode.getNow(null));
                            }
                        })
                        .exceptionally(
                                throwable -> {
                                    Log.e(TAG, "Exception loading: ", throwable);
                                    return null;
                                });
            }
            else {
                componentScale = componentScale * 0.2f;
                playButtonPositionNode = new Node();
                playButtonPositionNode.setParent(anchorNode);
                playButtonPositionNode.setLocalPosition(new Vector3(xPos * getInstance().getPositionScale(), -zPos * getInstance().getPositionScale() + 0.01f, -yPos * getInstance().getPositionScale()));
                playButtonPositionNode.setLocalRotation(new Quaternion(new Vector3(90, (float) rot.x, 180)));
                playButtonPositionNode.setLocalScale(new Vector3(xScl * getInstance().getScale() * componentScale, zScl * getInstance().getScale() * componentScale, yScl * getInstance().getScale() * componentScale));
                playButtonPositionNode.setRenderable(playButtonNode.getNow(null));
            }
        }
//
//        if(gif){
//            positionNode.setLocalScale(new Vector3(
//                    xScl * getInstance().getScale() * getInstance().getVideoScale(), yScl * getInstance().getScale() * getInstance().getVideoScale(), zScl * getInstance().getScale() * getInstance().getVideoScale()));
//
//            positionNode.setRenderable((VideoNode));
////            externalTexture
////                    .getSurfaceTexture()
////                    .setOnFrameAvailableListener(
////                            (SurfaceTexture surfaceTexture) -> {
////                                positionNode.setRenderable(VideoNode);
//////                                externalTexture.getSurfaceTexture().setOnFrameAvailableListener(null);
////                            });
//            positionNode.setRenderable((VideoNode));
//        }
//        else if (Video) {
        if (Video) {
            float videoWidth = mediaPlayer.getVideoWidth() * 0.001f;
            float videoHeight = mediaPlayer.getVideoHeight() * 0.001f;

            positionNode.setLocalScale(new Vector3(
                    xScl * getInstance().getScale() * videoWidth * getInstance().getVideoScale(), yScl * getInstance().getScale() * getInstance().getVideoScale() * videoHeight, zScl * getInstance().getScale() * getInstance().getVideoScale()));


            positionNode.setLocalRotation(new Quaternion(new Vector3(-90, (float) rot.x, 0)));

            mediaPlayer.start();
            playButton.setAlpha(0f);

            if (audioUUID != null) {
                AnalyticsManager.startRecordingEngagement(audioUUID);
            } else if (videoUUID != null) {
                AnalyticsManager.startRecordingEngagement(videoUUID);
            }

            // Wait to set the renderable until the first frame of the  video becomes available.
            // This prevents the renderable from briefly appearing as a black quad before the video
            // plays.
            Log.d(TAG, "setImage: MEDIAPLAYER STARTING HERE");
            externalTexture
                    .getSurfaceTexture()
                    .setOnFrameAvailableListener(
                            (SurfaceTexture surfaceTexture) -> {
                                positionNode.setRenderable(VideoNode);
                                externalTexture.getSurfaceTexture().setOnFrameAvailableListener(null);
                            });

            if (!autoPlay) {
                mediaPlayer.pause();
                playButton.setAlpha(1f);
                AnalyticsManager.pauseRecordingEngagement(videoUUID);
            }

            positionNode.setOnTapListener(
                    (hitTestResult, motionEvent) -> {
                        ToggleMedia();
//                        if (!mediaPlayer.isPlaying()) {
//                            mediaPlayer.start();
//                             //for GA
////                            if(mediaPlayer.getCurrentPosition()>1){ //if was paused
////                                AnalyticsManager.resumeRecordingEngagement(videoUUID);
////                            }
////                            else{
////                                AnalyticsManager.startRecordingEngagement(videoUUID);
////                            }
//                            mediaPlayer.start();
//                            playButton.setAlpha(0f);
//                        } else {
//                            mediaPlayer.pause();
//                            playButton.setAlpha(1f);
//
//                             //for GA
////                             AnalyticsManager.pauseRecordingEngagement(videoUUID);
//                        }
                    }
            );


//            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
//            animator.addUpdateListener(animation -> {
//                imageView.setAlpha((float) animation.getAnimatedValue());
//                if (playButton != null)
//                    playButton.setAlpha((float) animation.getAnimatedValue());
//            });
//            animator.setDuration(2000);
//            animator.start();
        } else {
            positionNode.setRenderable(AssetNode.getNow(null));
//            if (imageView != null) {
//                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
//                animator.addUpdateListener(animation -> {
//                    imageView.setAlpha((float) animation.getAnimatedValue());
//                    if (playButton != null)
//                        playButton.setAlpha((float) animation.getAnimatedValue());
//                });
//                animator.setDuration(2000);
//                animator.start();
//            }
        }

//        if(gif){
//            thread = new Thread(() -> {
//
//                Activity activity = (Activity) activityContext;
//                activity.runOnUiThread(() -> {
////                        AnimatedImageDrawable animation = (AnimatedImageDrawable) imageView.getBackground();
////                        animation.start();
////                    AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) drawable;
////                    animatedImageDrawable.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
////                    animatedImageDrawable.start();
//
//                    Thread thread = new Thread(() -> {
//                    while (true){
//                        AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) drawable;
////                        animatedImageDrawable.stop();
//                        animatedImageDrawable.clearAnimationCallbacks();
//                        animatedImageDrawable.start();
//                        animatedImageDrawable.clearAnimationCallbacks();
//                        if(animatedImageDrawable.isRunning()) {
//                            Log.e(TAG, "setImage: TRUE" );
////                            animatedImageDrawable.clearAnimationCallbacks();
////
////                            animatedImageDrawable.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
////                            animatedImageDrawable.start();
//                        }
//                        else
//                            Log.e(TAG, "setImage: FALSE" );
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    });
//                    thread.start();
//                });
//            });
//            thread.start();
//        }
    }

    private void ToggleMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setAlpha(1f);

            if (audioGlow != null) {
                audioGlow.cancel();
                imageView.setAlpha(1f);
            }

            if (audioUUID != null) {
                AnalyticsManager.pauseRecordingEngagement(audioUUID);
            } else if (videoUUID != null) {
                AnalyticsManager.pauseRecordingEngagement(videoUUID);
            }

        } else {
            mediaPlayer.start();
            playButton.setAlpha(0f);
            if (audioGlow != null) {
                audioGlow.start();
            }
            if (audioUUID != null) {
                if (mediaPlayer.getCurrentPosition() != 0) { //if was paused
                    AnalyticsManager.resumeRecordingEngagement(audioUUID);
                } else {
                    AnalyticsManager.startRecordingEngagement(audioUUID);
                }
            } else if (videoUUID != null) {
                if (mediaPlayer.getCurrentPosition() != 0) { //if was paused
                    AnalyticsManager.resumeRecordingEngagement(videoUUID);
                } else {
                    AnalyticsManager.startRecordingEngagement(videoUUID);
                }
            }
        }
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

            if (positionNode != null) {
                positionNode.setParent(null);
                positionNode.setRenderable(null);
                positionNode = null;
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
        //TODO: Other Cleaup

    }

    //pauses all videos and audios...used when going to instructions screen or background

    public void pauseAudioOrVideo() {
        if (mediaPlayer.isPlaying()) {
            wasRunning = true;
            mediaPlayer.pause();
        }
        pauseTime = mediaPlayer.getCurrentPosition();

    }

    //resume videos that were running previously

    public void resumeAudioOrVideo() {

        if (wasRunning) {
            mediaPlayer.seekTo(pauseTime);
            mediaPlayer.start();
            wasRunning = false;
        }
    }


}

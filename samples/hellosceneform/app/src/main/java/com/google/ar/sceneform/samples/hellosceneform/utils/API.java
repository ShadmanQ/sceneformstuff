package com.google.ar.sceneform.samples.hellosceneform.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.ar.sceneform.samples.hellosceneform.BuildConfig;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.google.ar.sceneform.samples.hellosceneform.utils.Constants.getInstance;
//import static com.k.postreality.utils.Constants.getInstance;

public class API {

    public Boolean CheckAppVersion() {
        String url = getInstance().getBaseApiUrl() + "/viewer-version/postreality";
        String version = "";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("mode", "cors")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            String result = Objects.requireNonNull(response.body()).string();
            Log.d("APIResponse", result);
            version = new JSONObject(result).getString("viewer_version");
            Log.d(TAG, "CheckAppVersion: " + BuildConfig.VERSION_NAME);
            Log.d(TAG, "CheckAppVersion: " + version);
        } catch (Exception e) {
            Log.d(TAG, "CheckAppVersion: Exception" + e);
        }
        return BuildConfig.VERSION_NAME.compareTo(version) >= 0;
    }

    public String UploadDescriptors(String descriptors) {
        String token = getInstance().getAccessToken();
        String url = getInstance().getBaseApiUrl() + "/marker/detect-from-descriptors";

        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
////                .addFormDataPart("descriptors", descriptorFile.getName(),
////                        RequestBody.create(descriptorFile, MediaType.parse("application/octet-stream")))
//                .addFormDataPart("descriptors.txt", descriptors)
//                .build();

        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody requestBody = RequestBody.create(descriptors, mediaType);

//                RequestBody requestBody = new FormBody.Builder()
//                .add("descriptors.txt", descriptors)
//                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("mode", "cors")
                .addHeader("content-type", "application/octet-stream")
                .addHeader("Authorization", token)
                .post(requestBody)
                .build();

        String result = "";
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            result = Objects.requireNonNull(response.body()).string(); //store the http call response in a variable in string format
//            Log.d("UploadDescriptors", result);
        } catch (Exception e){
        }

//        Log.e("UploadDescriptors", result);
        return result;
    }

    public String UploadImage(Bitmap bitmap) { //Upload Image to Jeremy's endpoint to retrieve matches
        String token = getInstance().getAccessToken();
        String url = getInstance().getBaseApiUrl() + "/marker/detect"; //Initialize base url
        byte[] image = toByteArray(bitmap); //convert bitmap data to byte array (byte array is required to send information over http calls)
        OkHttpClient client = new OkHttpClient.Builder().build();

//        client.retryOnConnectionFailure();

        MediaType mediaType = MediaType.parse("image/jpeg; charset=utf-8"); //set media type to image to be sent over http calls

        RequestBody requestBody = RequestBody.create(mediaType, image); //(in built function) to create an http request

        Request request = new Request.Builder() //Sends the image (in built function) to the url
                .url(url)
                .addHeader("mode", "cors")
                .addHeader("Content-Type", "image/jpeg")
                .addHeader("Authorization", token)
                .post(requestBody)
                .build();
        String result = "";
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            result = Objects.requireNonNull(response.body()).string();
            Log.d("APIResponse",Objects.requireNonNull(response.body()).string());//store the http call response in a variable in string format
            Log.d("APIResponse", result);
        } catch (Exception ignored) {
        }
        return result; // return the result string
    }

    public String UploadImagewOCR(Bitmap bitmap, String text){
        String token = getInstance().getAccessToken();
        String url = getInstance().getBaseApiUrl() + "/marker/detect_ocr";
        //Log.d("API_CALL_OCR",Integer.toString(bitmap.getByteCount()));
        byte[] image = toByteArray(bitmap);
        Log.d("API_CALL",text);

        MediaType bmp_mediaType = MediaType.parse("image/jpeg; charset=utf-8");

        MediaType str_mediaType = MediaType.parse("application/octet-stream");

        OkHttpClient client = new OkHttpClient.Builder().build();

        MultipartBody.Builder mpBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        RequestBody imageBody = RequestBody.create(bmp_mediaType,image);
        RequestBody textBody = RequestBody.create(text, str_mediaType);

        RequestBody requestBody = mpBuilder.addFormDataPart("image","image_file",imageBody)
                                            .addFormDataPart("OCR",text,textBody)
                                            .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("mode","cors")
                .addHeader("Content-Type","multipart/form-data")
                .addHeader("Authorization",token)
                .post(requestBody).build();



        String result = "";
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            result = Objects.requireNonNull(response.body()).string();//store the http call response in a variable in string format
        } catch (Exception ignored) {
        }
        return result;
    }

    public Bitmap DownloadImageToBitmap(String url) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            byte[] result = Objects.requireNonNull(response.body()).bytes();
//            Log.d("APIResponse", result.toString());
            return toBitmap(result); //converting raw data to bitmap using toBitmap function
        } catch (Exception ignored) {
        }
        return null;
    }

    public byte[] DownloadImageToByteArray(String url) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            byte[] result = Objects.requireNonNull(response.body()).bytes();
//            Log.d("APIResponse", result.toString());
            return result; //converting raw data to bitmap using toBitmap function
        } catch (Exception ignored) {
        }
        return null;
    }

    public String fetchARExperienceByExpID(String uuid) {
        String url = getInstance().getBaseApiUrl() + "/ar-experience/" + uuid;
        return APICall(url);
    }

    public String fetchARExperience(String uuid) {
        String url = getInstance().getBaseApiUrl() + "/ar-experience/marker/" + uuid;
        return APICall(url);
    }

    public String fetchTargetMarkerURL(String uuid) {
        String url = getInstance().getBaseApiUrl() + "/marker/" + uuid + "/url";
        return APICall(url);
    }

    public String fetchARContentAssetURL(String uuid) {
        String url = getInstance().getBaseApiUrl() + "/asset/" + uuid + "/url";
        Log.d("TAG", "fetchARContentAssetURL: " + url);
        return APICall(url);
    }

    private String APICall(String url) {
        String token = getInstance().getAccessToken();
        String result = "";
        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.d("API Call",url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("mode", "cors")
                .addHeader("Authorization", token)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected Code" + response);
            result = Objects.requireNonNull(response.body()).string();
            Log.d("LOOK",result);
            Log.d("APIResponse", result);
        } catch (Exception ignored) {
        }
        return result;
    }

    private byte[] toByteArray(Bitmap bitmap) {//converts bitmap to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private Bitmap toBitmap(byte[] bytes) {//converts byte array to bitmap
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

//    public void OpenAppStore() {
//
//    }
}


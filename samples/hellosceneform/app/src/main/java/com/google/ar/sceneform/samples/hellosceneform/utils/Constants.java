package com.google.ar.sceneform.samples.hellosceneform.utils;

public class Constants {
    private static Constants Instance = null;
    private static boolean prod = false;
    private static final String BASE_API_URL  = "https://s.postreality.io/api/v1";
    private static final String AR_CONTENT_BASE_URL = "https://s.postreality.io/api/v1/asset/download/";
    private static final String ACCESS_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE1NTA1OTcxNTUsIm5iZiI6MTU1MDU5NzE1NSwianRpIjoiYzgzNTU4YzAtZjM2Ny00NGJlLWJiMjItYTc4YmE4Mjg0NjJhIiwiaWRlbnRpdHkiOjExNiwiZnJlc2giOmZhbHNlLCJ0eXBlIjoiYWNjZXNzIn0.FUGuHrfZG66XYWThq6u1x0IYkFAmsMampkVkjlZwiMc";
    private static final String APP_VERSION = "1.3";
    private static final float SCALE = 0.82f;
    private static final float POSITION_SCALE = 0.001f;
    private static final float VIDEO_SCALE = 1.22f;
    private static final float DEFAULT_ICON_SCALE = 0.3f;

    private static final String PROD_BASE_API_URL  = "https://editor.postreality.io/api/v1";
    private static final String PROD_AR_CONTENT_BASE_URL = "https://editor.postreality.io/api/v1/asset/download/";
    private static final String PROD_ACCESS_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE1NTE3NDM0MTYsIm5iZiI6MTU1MTc0MzQxNiwianRpIjoiZmZlODRmNzQtMDA1Mi00MTg0LTgwMTEtNDRjY2M0ODM2ZDlhIiwiaWRlbnRpdHkiOiIxNTkiLCJmcmVzaCI6ZmFsc2UsInR5cGUiOiJhY2Nlc3MifQ.tzDDtKDUBYUyvKZPXbusuBsBssiomXdGohCLMCHd7T0";

    private Constants() {};

    public static Constants getInstance() {
        if(Instance == null) {
            Instance = new Constants();
        }
        return(Instance);
    }

    public Boolean prodCheck() {
        return prod;
    }

    public float getPositionScale() {
        return POSITION_SCALE;
    }

    public float getDefaultScale() { return DEFAULT_ICON_SCALE; }

    public String getBaseApiUrl() {
        if(prod)
            return PROD_BASE_API_URL;
        else
            return BASE_API_URL;
    }

    public String getArContentBaseUrl() {
        if(prod)
            return PROD_AR_CONTENT_BASE_URL;
        else
            return AR_CONTENT_BASE_URL;
    }

    public String getAccessToken() {
        if(prod)
            return PROD_ACCESS_TOKEN;
        else
            return ACCESS_TOKEN;
    }

    public String getAppVersion() {
        return APP_VERSION;
    }

    public float getScale() {
        return SCALE;
    }

    public float getVideoScale() {
        return VIDEO_SCALE;
    }
}

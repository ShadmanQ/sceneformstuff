package com.google.ar.sceneform.samples.hellosceneform.utils;

import android.os.Build;
import android.os.Bundle;

import java.io.Serializable;
import java.util.HashMap;

public class AnalyticsManager implements Serializable {

    //for GA
    private static final String TAG = AnalyticsManager.class.getSimpleName();
    public static stopwatch sw = new stopwatch();
    private final String AnalyticsEventViewExperience = "exp_end";
    private final String AnalyticsEventViewARComponent = "comp_end";
    private final String AnalyticsEventMarkerDetect = "marker_detect";

    private boolean experienceTimerStarted = false;

    //phone information
    private String operatingSystem = Build.VERSION.RELEASE;
    private int sdkVersion = Build.VERSION.SDK_INT;
    private String manufacturer = Build.MANUFACTURER;
    private String model  = Build.MODEL;

    // Experience Engagement Analytics
    private String experienceUUID;
    private EngagementAnalytics experienceInfo = new EngagementAnalytics();

    // AV Component Engagement Analytics
    private HashMap<String,EngagementAnalytics> EngagementAnalyticsHashMap;

    //create an Analytics manager
    public AnalyticsManager(){
        experienceUUID="";
        EngagementAnalyticsHashMap = new HashMap<>();
    }

    public void addEngagementAnalytic(String uuid){
        EngagementAnalyticsHashMap.put(uuid,new EngagementAnalytics());
    }


    //METHODS FOR THE EXPERIENCE TIMER

    //start experience timer
    public void startExperienceTimer(){
        if(!experienceTimerStarted){
            experienceInfo.startEngagement();
            experienceTimerStarted=true;
        }
    }
    //pause the experience timer
    public void pauseExperienceTimer(){
        experienceInfo.pauseEngagement();
    }


    //METHODS FOR THE COMPONENTS

    //start recording engagement time
    public void startRecordingEngagement(String uuid){
        if(EngagementAnalyticsHashMap.get(uuid) != null)
            EngagementAnalyticsHashMap.get(uuid).startEngagement();
    }
    //pause recording engagement time
    public void pauseRecordingEngagement(String uuid){
        if(EngagementAnalyticsHashMap.get(uuid) != null)
            EngagementAnalyticsHashMap.get(uuid).pauseEngagement();
    }
    //resume....resume checks that the component was active before. Best to use resume if engagement was paused
    public void resumeRecordingEngagement(String uuid){
        if(EngagementAnalyticsHashMap.get(uuid) != null)
            EngagementAnalyticsHashMap.get(uuid).resumeEngagement();
    }
    //add number of Clicks to engagement (for messages and links)
    public void addClick(String uuid){ EngagementAnalyticsHashMap.get(uuid).addClick();
    }
    //reset timers of the individual engagements
    private void resetEngagementStopWatches(){
        for(String s: EngagementAnalyticsHashMap.keySet()){
            EngagementAnalyticsHashMap.get(s).resetEngagementTimes();
        }
    }

    //STOPWATCH METHODS and FOR ALL THE WATCHES OR EFFECT ALL OF THE ENGAGEMENT ANALYTICS COMPONENTS
    //start stopwatch/resume
    public void startStopWatch(){
        sw.start();
    }

    //pause stopwatch
    public void pauseStopWatch(){
        sw.pause();
    }
    //reset stopwatch
    public void resetStopWatch(){
        sw.reset();
    }

    //resume timers of the engagements
    public void resumeRunningWatches(){
        for(String s: EngagementAnalyticsHashMap.keySet()){
            EngagementAnalyticsHashMap.get(s).resumeEngagement();
        }
    }
    //pauses all watches.
    public void pauseAllWatches(){
        for(String s: EngagementAnalyticsHashMap.keySet()){
            EngagementAnalyticsHashMap.get(s).pauseEngagement();
        }
    }

    //GETTER METHODS!!

    //get the time of the Experience Duration
    private double getExperienceDuration(){
        return experienceInfo.totalEngagementDuration();
    }

    //get the time of an individual component (based on the UUID)
    private double getComponentDuration(String uuid){
        return EngagementAnalyticsHashMap.get(uuid).totalEngagementDuration();
    }

    //get the number of times an individual component was clicked on
    private int getEngagementNumClicks(String uuid){
        return EngagementAnalyticsHashMap.get(uuid).numClicks();
    }

    //get number of components an experience has
    private int getNumComponents(){
        return EngagementAnalyticsHashMap.size();
    }


    //SETTER METHOD
    //set UUID of the experience
    public void setExperienceUUID(String uuid){
        experienceUUID = uuid;
    }

    //RESETTING AND CLEARING ANALYTICS

    //resets analytics
    public void clearUserAnalytics(){
        experienceUUID="";
        EngagementAnalyticsHashMap.clear();
        experienceTimerStarted=false;
        resetStopWatch();
    }
    //resets the analytics, but does not clear the hashmap objects
    public void resetUserAnalytics(){
        resetEngagementStopWatches();
        resetEngagementClicks();
        experienceInfo.resetEngagementTimes();
    }
    private void resetEngagementClicks(){
        for(String uuid:EngagementAnalyticsHashMap.keySet()){
            EngagementAnalyticsHashMap.get(uuid).resetNumClicks();
        }
    }

    //print out the Manager Activity Information
    public String toString(){
        String s ="";

        for (String key : EngagementAnalyticsHashMap.keySet()) {
            EngagementAnalytics value = EngagementAnalyticsHashMap.get(key);

            s = s + "UUID: " + key + "\t" + value.toString() + "\n";
        }
        return "Analytics Manager Info:\n Experience Time: "+getExperienceDuration()+"\nNumber of components: "+getNumComponents()+"\n\nComponent Information\n"+s;

    }

    //LOGGING ANALYTICS TO GOOGLE ANALYTICS

    //method will be used to push all analytics to google analytics..CV analytics are to be added
    //public void logGoogleAnalytics(){
    //    logEngagementAnalytics();
    }

//    ///method that is to log user events and resets them
//    private void logEngagementAnalytics(){
//        if(experienceInfo.totalEngagementDuration()>0){
//            logExpEngagementAnalytics();
//            logComponentEngagementAnalytics();
//            resetUserAnalytics();
//        }
//    }

//    //google analytics to log experience engagement analytics
//    private void logExpEngagementAnalytics() {
//        //for the experience info
//        Bundle bundle = new Bundle();
//        bundle.putString("exp_uuid", experienceUUID);
//        bundle.putDouble("engagement_duration", getExperienceDuration());
//
//        mFirebaseAnalytics.logEvent(AnalyticsEventViewExperience, bundle);
//        //---put what you need to send to Google Analytics here--
//
////        Log.d(TAG, "logExpEngagementAnalytics: EXPERIENCE ANALYTICS: UUID: " + experienceUUID + " ENGAGEMENT_DURATION: " + getExperienceDuration() );
//    }
//    //log the time for the individual components to google analytics
//    private void logComponentEngagementAnalytics() {
//        for (String s : EngagementAnalyticsHashMap.keySet()) {
//            Bundle bundle = new Bundle();
//            bundle.putString("comp_uuid", s);
//            bundle.putDouble("engagement_duration", getComponentDuration(s));
//            bundle.putInt("click_count", getEngagementNumClicks(s));
//
//            //--send into Google Analytics here--
//            mFirebaseAnalytics.logEvent(AnalyticsEventViewARComponent, bundle);
////            Log.d(TAG, "logComponentEngagementAnalytics: COMPONENT ANALYTICS: UUID: " + s + " ENGAGEMENT_DURATION: " + getComponentDuration(s) + " NUMBER OF CLICKS: " + getEngagementNumClicks(s));
//        }
//    }
//}


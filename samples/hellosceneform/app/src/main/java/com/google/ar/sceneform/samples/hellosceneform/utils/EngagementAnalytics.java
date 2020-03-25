package com.google.ar.sceneform.samples.hellosceneform.utils;

import java.util.LinkedList;

public class EngagementAnalytics {
    stopwatch sw = AnalyticsManager.sw;
    private int numClicks;
    private double startTime;
    LinkedList<Double> engagementTimes = new LinkedList<Double>();

    //from old clock
    private boolean running = false;
    private boolean wasRunning= false;


    public EngagementAnalytics(){
        numClicks=0;
    }

    //starts the timer for the engagement
    public void startEngagement(){
        setStartTime();
        running = true;
    }

    //pause the component stopwatch
    public void pauseEngagement(){
        if(isRunning()){
            wasRunning = true;
            if(timeElapse()>0)
                engagementTimes.add(timeElapse());
            running=false;
        }

    }
    //resume the component stopwatch
    public void resumeEngagement(){
        if(wasRunning) {
            startEngagement();
            wasRunning = false;
        }
    }

    public void resetEngagementTimes(){
        engagementTimes.clear();
        startTime=0;
    }

    //get the current time of the stopwatch
    public double totalEngagementDuration(){
        pauseEngagement();
        double sum = 0;
        for(double time: engagementTimes){
            sum+=time;
        }
        resumeEngagement();
        return sum;
    }

    //adds a click to the component
    void addClick(){
        numClicks+=1;
    }

    //returns number of clicks of the component
    int numClicks(){
        return numClicks;
    }

    //reset number of clicks of the engagement
    public void resetNumClicks(){
        numClicks = 0;
    }

    //print out the stop watch time and number of clicks
    public String toString(){
        return "StopWatchTime: "+totalEngagementDuration()+" NumClicks: "+numClicks();
    }

    /////to use one instead of many stopwatches!!!!!!!!

    //set the start time of the component engagement
    private void setStartTime(){
        startTime = sw.getTime();
    }

    //find time that has past since the last component engagement
    private double timeElapse(){
        return sw.getTime()-startTime;
    }

    //from old stopwatch
    private boolean isRunning(){
        return running;
    }

}

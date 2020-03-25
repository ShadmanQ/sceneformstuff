package com.google.ar.sceneform.samples.hellosceneform.utils;

public class stopwatch {
    private double timeElapsed;
    private long startTimeMillis;
    private boolean running;
    private double overallTime;


    //create the stopwatch
    public stopwatch() {
        overallTime = 0;
    }

    //starts the time of the stopwatch
    void start(){
        if(running)
            return;
        startTimeMillis=System.currentTimeMillis();
        running=true;
        timeElapsed=0;
    }

    //pauses the stopwatch
    void pause(){
        overallTime=getTime();
        running=false;
        timeElapsed=0;
    }
    public void reset(){
        pause();
        overallTime = 0;
        timeElapsed=0;
    }

    public double getTime(){ //rounds to the nearest thousand
        int time = (int)getTimeMillis();
        return (double)(time/1000);
    }

    //get time of stopwatch in milliseconds
    private double getTimeMillis(){
        if(running) {
            timeElapsed=System.currentTimeMillis()-startTimeMillis;
        }
        return overallTime*1000+timeElapsed;
    }
}

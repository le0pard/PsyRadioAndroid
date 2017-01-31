package com.catware.psyradio.util;

/**
 * Created by officemac on 25.03.16.
 */
public class TimeUtils {


    public static final String FORMAT_WITH_POINTS = "%02d:%02d";
    public static final String FORMAT_WITHOUT_POINTS = "%02d %02d";

    public static int getHours(long timeInSeconds) {
        return (int) ((timeInSeconds / (60 * 60)) % 24);
    }

    public static int getMinutes(long timeInSeconds) {
        return (int) ((timeInSeconds / (60)) % 60);
    }



    public static String getTimeWithPoints(long timeInSeconds) {

        int hours = (int) ((timeInSeconds / (60 * 60)) % 24);
        int minutes = (int) ((timeInSeconds / (60)) % 60);
       // int seconds = (int) timeInSeconds % 60;

        //if(hours == 0 && seconds!=0){
          //  minutes=minutes+1;
       // }
        return String.format(FORMAT_WITH_POINTS, hours, minutes);
    }

    public static String getTimeWithoutPoints(long timeInSeconds) {
        int hours = (int) ((timeInSeconds / (60 * 60)) % 24);
        int minutes = (int) ((timeInSeconds / (60)) % 60);
       // int seconds = (int) timeInSeconds % 60;

        //if(hours == 0 && minutes!=0 && seconds!=0){
         //   minutes=minutes+1;
       // }

        return String.format(FORMAT_WITHOUT_POINTS, hours, minutes);

    }
}

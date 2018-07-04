package com.shaeen.launcher.utils;

import com.shaeen.launcher.AppDetail;

import java.util.Comparator;

/**
 * Used to sort apps based on their start count
 */

class NoOfStartsSorter implements Comparator<AppDetail> {
    @Override
    public int compare(AppDetail app1, AppDetail app2) {
        int returnVal = 0;

        if(app1.getmNumberOfStarts() < app2.getmNumberOfStarts()){
            returnVal =  1;
        }else if(app1.getmNumberOfStarts() > app2.getmNumberOfStarts()){
            returnVal =  -1;
        }else if(app1.getmNumberOfStarts() == app2.getmNumberOfStarts()){
            returnVal =  0;
        }
        return returnVal;
    }
}

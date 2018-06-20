package com.example.alstn0107.mypiano_real;

import android.app.Application;

/**
 * Created by alstn0107 on 2018-06-05.
 */

public class MyApplication extends Application {

    private String type = "piano";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class PermissionProvider {

     static public boolean isNotReady(Context c, Activity a, String Permission) {
        if (ContextCompat.checkSelfPermission(c, Permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a,
                    new String[]{Permission}, 123);
            if (ContextCompat.checkSelfPermission(c, Permission)
                    == PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                String text = Permission+" is not granted..";
                Log.e("Permission",text);
                Toast.makeText(c,text , Toast.LENGTH_LONG).show();
                return true;
            }
        }
        else return false;
    }
}

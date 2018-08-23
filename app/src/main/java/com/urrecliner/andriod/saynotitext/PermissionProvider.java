package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionProvider {

    public static int isPermitted(Context c, Activity a, String Permission, int NormalCode) {
        if (ContextCompat.checkSelfPermission(c, Permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a,
                    new String[]{Permission}, NormalCode);
            if (ContextCompat.checkSelfPermission(c, Permission)
                    == PackageManager.PERMISSION_GRANTED) {
                return NormalCode;
            } else {
                Toast.makeText(c,Permission + " is not granted..", Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        else return NormalCode;
    }

}

package examples.aaronhoskins.com.mapsandlocationsdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    public static final int LOCATION_PERMISSION_REQUEST_GROUP_CODE = 42;
    IPermissionManager iPermissionManager;
    Context context;

    public PermissionManager(Context context) {
        this.context = context;
        this.iPermissionManager = (IPermissionManager)context;
    }

    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    (Activity)context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //If you want to explain WHY you need this permission, create a UI or dialog to explain
                //   and setup to display here
            } else {
                requestPermission();
            }
        } else {
            iPermissionManager.onPermissionRequestResponse(true);
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(
                (Activity)context,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_GROUP_CODE);
    }

    public void checkResult(int requestCode, int[] grantedResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_GROUP_CODE:
                if(grantedResults.length > 0 && grantedResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iPermissionManager.onPermissionRequestResponse(true);
                } else {
                    iPermissionManager.onPermissionRequestResponse(false);
                }
                break;

        }
    }

    public interface IPermissionManager{
        void onPermissionRequestResponse(boolean isGranted);
    }
}

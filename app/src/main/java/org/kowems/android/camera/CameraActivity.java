package org.kowems.android.camera;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends FragmentActivity {

    private final static int REQUEST_DELETE_PHOTO = 1;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 请求窗口特性：无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //添加窗口特性：全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        PackageManager pm = getPackageManager();
        boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ||
                Camera.getNumberOfCameras() > 0;

        if(!hasCamera) {
            setContentView(R.layout.activity_no_camera);
            return;
        }

        setContentView(R.layout.activity_camera);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer_camera);

        if(fragment == null) {
            fragment = new CameraFragment();
            fm.beginTransaction().add(R.id.fragmentContainer_camera,fragment).commit();
        }
    }
}

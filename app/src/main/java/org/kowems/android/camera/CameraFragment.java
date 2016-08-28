package org.kowems.android.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.hardware.Camera.Size;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Eric Ju on 2016/8/27.
 */
public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";
    //startActivityForResult的请求常量
    private final static int REQUEST_DELET_PHOTO = 1;

    //自定义时间类
    private MyTime mTime= new MyTime();

    //相机类
    private Camera mCamera;

    //预览视图接口
    private SurfaceHolder mSurfaceHolder;

    //进度条控件
    private View mProgressContainer;

    //当前打开的是哪个摄像头
    private int switchCamera=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //生成fragment视图
        View v = inflater.inflate(R.layout.fragment_camera,container,false);

        //隐藏进度条控件
        mProgressContainer = v.findViewById(R.id.camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        //显示最新照片的缩略图的按钮
        ImageButton viewButton = (ImageButton)v.findViewById(R.id.camera_view_button);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"view Button is clicked!");
            }
        });

        ImageButton rotationViewButton = (ImageButton)v.findViewById(R.id.camera_rotationview_button);

        rotationViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //如果摄像头数目小于等于1，该按钮无效
                if (Camera.getNumberOfCameras() <= 1) {
                    return;
                }

                if (switchCamera == 1) {
                    switchCamera(0);
                } else {
                    switchCamera(1);
                }
            }
        });

        ImageButton takePictureButton = (ImageButton)v.findViewById(R.id.camera_take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCamera != null) {
                    //相机的拍照方法
                    mCamera.takePicture(

                            //快门回调方法
                            new Camera.ShutterCallback() {
                                @Override
                                public void onShutter() {
                                    //该方法触发快门声音，并设置进度条显示
                                    mProgressContainer.setVisibility(View.VISIBLE);
                                }
                            },
                            //第二，三个回调方法为空
                            null,
                            null,
                            //最后一个回调方法，jpg图像回调方法
                            new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] date, Camera camera) {

                                    //根据当前时间自定义格式生成文件名
                                    String filename = mTime.getYMDHMS()+".jpg";

                                    //文件输出流
                                    FileOutputStream os = null;

                                    //默认文件保存成功
                                    boolean success = true;

                                    try {
                                        //私有打开应用沙盒文件夹下的文件
                                        os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                                        //写文件
                                        os.write(date);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        success = false;
                                    } finally {
                                        try{
                                            if (os != null) {
                                                os.close();
                                            }
                                        } catch (IOException e) {
                                            success=false;
                                            e.printStackTrace();
                                        }
                                    }

                                    if (success) {
                                        //如果文件保存成功，进度条隐藏
                                        mProgressContainer.setVisibility(View.INVISIBLE);

                                        //再次预览
                                        try {
                                            mCamera.startPreview();
                                        } catch (Exception e) {
                                            mCamera.release();
                                            mCamera=null;
                                        }
                                    }
                                }
                            });
                }
            }
        });
        //预览视图实例化
        SurfaceView mSurfaceView = (SurfaceView)v.findViewById(R.id.camera_surfaceView);
        //得到预览视图接口
        mSurfaceHolder = mSurfaceView.getHolder();
        //设置预览视图接口类型
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //添加预览视图接口的回调方法，监听视图的生命周期
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (mCamera!=null) {
                        mCamera.setPreviewDisplay(surfaceHolder);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
                if (mCamera==null) {
                    return;
                }

                Camera.Parameters parameters = mCamera.getParameters();
                Size s = getBestSupportSize(parameters.getSupportedPictureSizes(),w,h);

                parameters.setPreviewSize(s.width,s.height);

                //mCamera.setParameters(parameters);

                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    mCamera.release();
                    mCamera=null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (mCamera!=null) {
                    mCamera.stopPreview();
                }
            }
        });
        return v;
    }

    private void switchCamera(int camera) {
        //停掉原摄像头的预览，并释放原来摄像头
        mCamera.stopPreview();
        mCamera.release();
        mCamera=null;

        //打开当前选中的摄像头
        switchCamera = camera;
        mCamera = Camera.open(switchCamera);
        try {
            //通过surfaceview显示取景画面
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开始预览
        mCamera.startPreview();
    }
    /*
     * 穷举法找出具有最大数目像素的尺寸
     */
    public Size getBestSupportSize(List<Size> sizes, int width,int height) {
        Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Size s:sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }

    @Override
    public void onActivityResult(int request,int result,Intent mIntent) {
        if (request == REQUEST_DELET_PHOTO) {
            if(result == Activity.RESULT_OK) {
                int requestCode = 1;
                Intent i = new Intent();
                Log.d(TAG,"onActivityResult is called!");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause");

        if (mCamera!=null) {
            mCamera.release();
            mCamera=null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onStop");
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(switchCamera);
        } else {
            mCamera = Camera.open();
        }
    }

}

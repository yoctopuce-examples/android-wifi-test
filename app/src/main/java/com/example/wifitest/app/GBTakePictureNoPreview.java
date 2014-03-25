package com.example.wifitest.app;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class GBTakePictureNoPreview implements SurfaceHolder.Callback {
    // open back facing camera by default
    private Camera myCamera = null;
    private int camId = GBCameraUtil.findBackFacingCamera();

    // Tell me if the preview is running or stopped
    private boolean mPreviewRunning = false;

    // Parameters
    private Context context = null;
    private String fileName = "";
    private boolean usingLandscape = false;
    private String _onPictureMessage="nothing";

    public GBTakePictureNoPreview (Context context) {
        this.context = context;
    }

    public boolean setUseFrontCamera(boolean useFrontCamera) {
        int c = GBCameraUtil.findFrontFacingCamera();
        if (c != -1) {
            camId = c;
            return true;
        } else {
            camId = GBCameraUtil.findBackFacingCamera();
            return false;
        }
    }

    public void setFileName (String fileName) {
        this.fileName = fileName;
    }

    public void setLandscape () {
        this.usingLandscape = true;
    }

    public void setPortrait () {
        this.usingLandscape = false;
    }

    public boolean cameraIsOk() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && camId > -1;
    }

    public void takePicture () {
        // do we have a camera?
        if (cameraIsOk()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (myCamera == null)
                myCamera = Camera.open(camId);

            if (myCamera != null)
            {
                SurfaceView surfaceView = new SurfaceView(context);
                surfaceView.setFocusable(true);

                SurfaceHolder holder = surfaceView.getHolder();
                holder.addCallback(this);
                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                try {
                    Camera.Parameters parameters = myCamera.getParameters();

                    List<Size> sizes = parameters.getSupportedPreviewSizes();
                    Size optimalSize = GBCameraUtil.getOptimalPreviewSize(sizes, 1024, 768);

                    int sdkBuildVersion = Integer.parseInt( android.os.Build.VERSION.SDK );

                    if (sdkBuildVersion < 5 || usingLandscape)
                    {
                        // Picture size should be landscape
                        if (optimalSize.width < optimalSize.height || usingLandscape)
                            parameters.setPictureSize( optimalSize.height, optimalSize.width );
                        else
                            parameters.setPictureSize( optimalSize.width, optimalSize.height );
                    }
                    else
                    {
                        // If the device is in portraint and width > height,
                        // or if the device is in landscape and height > height, so we need to rotate them.
                        switch (context.getResources().getConfiguration().orientation) {
                            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                                if (optimalSize.height > optimalSize.width ) {
                                    parameters.setRotation(camId == GBCameraUtil.findFrontFacingCamera() ? 270 : 90);
                                }

                                break;
                            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                                if (optimalSize.width > optimalSize.height) {
                                    parameters.setRotation(camId == GBCameraUtil.findFrontFacingCamera() ? 270 : 90);
                                }

                                break;
                        }

                        parameters.setPictureSize (optimalSize.width, optimalSize.height);
                    }

                    myCamera.setParameters(parameters);

                    myCamera.setPreviewDisplay(holder);
                    myCamera.startPreview();

                    myCamera.takePicture(null, null, getJpegCallback());

                    Thread.sleep(200);
                } catch (Exception e) {
                    // Sorry, nothing to do
                }
            }
        }
    }

    private PictureCallback getJpegCallback(){
        PictureCallback jpeg=new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                myCamera.stopPreview();
                mPreviewRunning = false;

                if (data != null) {
                    if (fileName.equals("")) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                        String date = dateFormat.format(new Date());
                        fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + date + ".jpg";
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(fileName);

                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length).copy(Bitmap.Config.ARGB_8888, true);
                        if (_onPictureMessage != null) {
                            Log.d("IODBG", "Write \"" + _onPictureMessage + "\" on Picture");
                            //myArray is the byteArray containing the image. Use copy() to create a mutable bitmap. Feel free to change the config-type. Consider doing this in two steps so you can recycle() the immutable bitmap.
                            Canvas canvas = new Canvas(bmp);
                            Paint paint = new Paint();
                            paint.setColor(Color.GREEN);
                            paint.setTextSize(20);
                            canvas.drawText(_onPictureMessage, 10, 10, paint);
                        }
                        //dstfile is a File-object that you want to save to. You probably need to add some exception-handling here.
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos); //Output as JPG
                        fos.flush();
                        fos.close();//Don't forget to close the stream.

                        // Tell the media scanner about the new file so that it is
                        // immediately available to the user.
                        MediaScannerConnection.scanFile(context,
                                new String[]{fileName}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri)
                                    {
                                        Log.i("ExternalStorage", "Scanned " + path + ":");
                                        Log.i("ExternalStorage", "-> uri=" + uri);
                                    }
                                }
                        );

                        fos.write(data);
                        fos.close();

                    }  catch (IOException e) {
                        Log.d("IODBG","Unable to save picture to " + fileName);
                        //do something about it
                    }
                }

                myCamera.release();
                myCamera = null;
            }
        };

        return jpeg;
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        myCamera = Camera.open(camId);

        try {
            if (myCamera != null)
                myCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            myCamera.release();
            myCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mPreviewRunning)
        {
            myCamera.stopPreview();
        }

        Camera.Parameters parameters = myCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = GBCameraUtil.getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        myCamera.setParameters(parameters);
        myCamera.startPreview();

        mPreviewRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    }


}
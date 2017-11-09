package com.example.khalil.myrobot;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khalil on 11/8/17.
 */

public class RobotController extends RosActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    TextView ultrasonicReading;
    TextView robotState;
    Button btn;
    Spinner spinner;
    Talker talkernode = new Talker(this);
    String command = null;

    private static final String TAG = "RobotController";
    private JavaCameraView javaCameraView;
    Mat imgHSV, mYellowThresh, circles, mRgba, mRedThresh;

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    protected RobotController() {
        super("Example", "Example");
    }

    @Override
    public void startMasterChooser() {
        URI uri;
        try {
            uri = new URI("http://192.168.1.101:11311/");
        } catch (URISyntaxException e) {
            throw new RosRuntimeException(e);
        }

        nodeMainExecutorService.setMasterUri(uri);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                RobotController.this.init(nodeMainExecutorService);
                return null;
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_controller);
        ultrasonicReading = (TextView) findViewById(R.id.ultrasonic_reading);
        robotState = (TextView) findViewById(R.id.robot_state);
        btn = (Button) findViewById(R.id.robot_button);
        spinner = (Spinner) findViewById(R.id.action_spinner);
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                publishGo(v);
            }
        });
        javaCameraView = (JavaCameraView) findViewById(R.id.HelloOpenCvView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 1);
            }
        }
        javaCameraView.setMaxFrameSize(640, 480);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Listener lisnode = new Listener(this, this);

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(
                InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(talkernode, nodeConfiguration);
        nodeMainExecutor.execute(lisnode, nodeConfiguration);
    }

    protected void publishGo(View view) {
        if (command != null) {
            talkernode.publish(command);
            Log.d("ALAASASAK", "I WOOORKKKKKKK");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallBack);
        } else {
            Log.d(TAG, "OpenCV loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgHSV = new Mat(height, width, CvType.CV_8UC4);
        mYellowThresh = new Mat(height, width, CvType.CV_8UC1);
        mRedThresh = new Mat(height, width, CvType.CV_8UC1);
        circles = new Mat();
        Log.d(TAG, "SIZE: " + height);
        Log.d(TAG, "SIZE: " + width);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, imgHSV, Imgproc.COLOR_BGR2HSV);
        Core.inRange(imgHSV, new Scalar(50, 100, 50), new Scalar(95, 255, 255), mYellowThresh);
        Core.inRange(imgHSV, new Scalar(120, 100, 60), new Scalar(179, 255, 255), mRedThresh);
        Imgproc.blur(mYellowThresh, mYellowThresh, new Size(10,10));
        Imgproc.threshold(mYellowThresh, mYellowThresh, 150, 255, Imgproc.THRESH_BINARY);
        Imgproc.blur(mRedThresh, mRedThresh, new Size(10,10));
        Imgproc.threshold(mRedThresh, mRedThresh, 150, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> yellowContours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> redContours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mYellowThresh, yellowContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(mRedThresh, redContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxYellowArea = 5000;
        double maxRedArea = 5000;
        float[] yellowRadius = new float[1];
        float[] redRadius = new float[1];
        Point yellowCenter = new Point();
        Point redCenter = new Point();
        for (int i = 0; i < yellowContours.size(); i++) {
            MatOfPoint c = yellowContours.get(i);
            if (Imgproc.contourArea(c) > maxYellowArea) {
                MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
                Imgproc.minEnclosingCircle(c2f, yellowCenter, yellowRadius);
            }
        }
        for (int i = 0; i < redContours.size(); i++) {
            MatOfPoint d = redContours.get(i);
            if (Imgproc.contourArea(d) > maxRedArea) {
                MatOfPoint2f c2f = new MatOfPoint2f(d.toArray());
                Imgproc.minEnclosingCircle(c2f, redCenter, redRadius);
            }
        }
        Imgproc.circle(mRgba, yellowCenter, (int)yellowRadius[0], new Scalar(0, 255, 0), 2);
        Imgproc.circle(mRgba, redCenter, (int)redRadius[0], new Scalar(255, 0, 0), 2);

        // TODO: USE SLACK TO SEND MESSAGE HERE!

        return mRgba;
    }
}

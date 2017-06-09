package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/8/17
 * This is the app's MainActivity. Here the app's view is set up, an instance of IOIOClass is
 * created and controlled, NavigationService is called, the picture is taken, and is uploaded
 * to Twitter. This is the app's main hub.
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TaskActivity extends AppCompatActivity implements CameraFragmentResultListener {
    public String TAG = "TaskActivity";
    private Intent mIntent; // The intent that starts the NavigationService.
    public float direction; // The robot's bearing to the next location point.
    public String message;  // The SMS message passed in through the EventBus.
    IOIOClass myRobot;  // An instance of the robot. A setter method will be used on this instance
            // to move the robot.
    public static final String FORWARDS = "forwards";   // A string command to move forwards.
    public static final String RIGHT = "right"; // A string command to turn right.
    public static final String LEFT = "left";   // A string command to turn left.
    public static final String STOP = "stop";   // A string command to stop.
    public String motionDirection = "not moving";  // An initialization of the direction the robot
            // is moving in.
    public final CameraFragment cameraFragment =
            CameraFragment.newInstance(new Configuration.Builder().build()); // A camera fragment
            // used to take the picture.

    /**
     * This method sets up the entire app, from the different intents that will be issued throughout
     * its lifecycle to the main XML file that is issued to the user.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task); // Sets the XML view file that appears to the user.
        mIntent = new Intent(this, NavigationService.class); // Associates mIntent with
                // NavigationService.
        myRobot = new IOIOClass(this); // Creates the robot instance from IOIOClass.
        myRobot.getIOIOAndroidApplicationHelper().create(); // Retrieves the IOIO helper, which is
                // responsible for starting the IOIO loop from another class, and creates it.
                // This allows TaskActivity to access the IOIOClass instance.


        // This IF block insures the camera permission is granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.
                        PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(this, "Grant camera permission please.", Toast.LENGTH_LONG).show();
            return;
        }

        // Attaches the camera fragment to the XML file so the user can see it.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, cameraFragment, "TheCameraThing")
                .commit();
    }


    public void onclicksend(View view){
        String msg = "Engineering Fountain";
        if (msg != null) {
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
            callNavigationService(msg);
        }
    }
    /**
     * This BroadcastReceiver starts when an intent is sent from NavigationActivity to TaskActivity.
    */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        // onReceive receives the intent from NavigationService
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Broadcasting");
            updateUI(intent); // The updateUI method is started. It needs the received intent to be
                    // passed to it.

            // This IF block stops the robot after 150ms after a command is issued for it to turn
                    // left or right. Else if the robot is moving forwards, it will stop the robot
                    // after 3 seconds.
            if (motionDirection.equals(RIGHT) || motionDirection.equals(LEFT)) {
                // This handler block runs a certain code after a period of time, in this case it
                        // stops the robot after 150ms and updates the TextViews on the main screen.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myRobot.setMotion(STOP);
                        motionDirection = STOP;
                        TextView directionTextView = (TextView) findViewById(R.id.bearingToDis);
                        directionTextView.setText("Direction: " + direction);

                        TextView motionTextView = (TextView) findViewById(R.id.motion);
                        motionTextView.setText("I am moving: " + motionDirection);
                    }
                }, 150);
            } else {
                // This handler block runs a certain code after a period of time, in this case it
                        // stops the robot after 3s and updates the TextViews on the main screen.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myRobot.setMotion(STOP);
                        motionDirection = STOP;
                        TextView directionTextView = (TextView) findViewById(R.id.bearingToDis);
                        directionTextView.setText("Direction: " + direction);

                        TextView motionTextView = (TextView) findViewById(R.id.motion);
                        motionTextView.setText("I am moving: " + motionDirection);
                    }
                }, 3000);
            }
        }
    };

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is restarted due to
     * a global event occurring, i.e. the phone's orientation is flipped, an SMS or an email
     * is received, a call is incoming, etc.
    */
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        myRobot.getIOIOAndroidApplicationHelper().start();
    }

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is stopped due to
     * a global event occurring, i.e. the phone's orientation is flipped, an SMS or an email
     * is received, a call is incoming, etc.
    */
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        myRobot.getIOIOAndroidApplicationHelper().stop();
    }

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is paused due to
     * a global event occurring, i.e. the phone's orientation is flipped, an SMS or an email
     * is received, a call is incoming, etc.
    */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        stopService(mIntent);
    }

    /**
     * This method insures the app's lifecycle doesn't get messed up if the app is returned to after
     * a global event occurs, i.e. the phone's orientation is flipped, an SMS or an email is
     * received, a call is incoming, etc.
    */
    @Override
    public void onResume() {
        super.onResume();
        startService(mIntent);
        registerReceiver(broadcastReceiver, new IntentFilter(NavigationService.BROADCAST_ACTION));
    }

    /**
     * This method is where the commands are issued to move the robot. The intent passed to this
     * method is from NavigationService, which contains two parameters, direction and
     * distance. Direction is used to orient the robot and move it in the correct direction.
     * Distance is the distance between the robot and its final destination, it is used to
     * stop the robot when it is 5m away from the target and take a picture.
    */
    private void updateUI(Intent intent) {
        direction = intent.getFloatExtra("direction", 0.0f); // obtains the direction parameter from
            // the intent passed by NavigationService.
        double distance = intent.getDoubleExtra("distance", 0.0f); // obtains the distance parameter
        // from the intent passed by NavigationService.

        // This IF block sets the robot's motion based on the next location's bearing with respect
                // to the robot. If the robot is within 5m from the destination, the robot will
                // stop and take a picture.
        if (!(distance < 5 && distance > 0)) {
            if (direction > 350 || direction < 10) {
                myRobot.setMotion(FORWARDS); // A setter method that sets the robot's motion.
                motionDirection = FORWARDS;
            } else if (direction < 350 && direction > 180) {
                myRobot.setMotion(RIGHT);
                motionDirection = RIGHT;
            } else if (direction < 180 && direction > 10) {
                myRobot.setMotion(LEFT);
                motionDirection = LEFT;
            }
        } else {
            myRobot.setMotion(STOP);
            motionDirection = "I have arrived to destination.";
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cameraFragment.takePhotoOrCaptureVideo(TaskActivity.this,
                            "/storage/self/primary", "thePicture001");
                }
            }, 5000);
        }

        Log.d("OUTSIDE", String.valueOf(direction));  // Used for debugging.

        // This block updates the user with the phone's direction to the next point. Used for
                // debugging.
        TextView directionTextView = (TextView) findViewById(R.id.bearingToDis);
        directionTextView.setText("Direction: " + direction);

        // This block updates the user with the phone's motion to the next point. Used for
        // debugging.
        TextView motionTextView = (TextView) findViewById(R.id.motion);
        motionTextView.setText("I am moving: " + motionDirection);

        // This block updates the user with the phone's distance to the final destination. Used for
        // debugging.
        TextView distanceTextView = (TextView) findViewById(R.id.distanceToFinal);
        distanceTextView.setText("I am: " + distance + "m away.");
    }

    /**
     * This method must be included in order for CameraFragmentResultListener to be implemented.
     * This method would be used if we were recording a video.
    */
    @Override
    public void onVideoRecorded(String filePath) {
        Toast.makeText(this, "Video", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is triggered when a picture is taken. Here the picture is uploaded to Twitter.
    */
    @Override
    public void onPhotoTaken(byte[] bytes, String filePath) {
        Toast.makeText(this, "Photo: " + bytes.length + filePath, Toast.LENGTH_SHORT).show();

        // This is the background task in charge of accessing Twitter and uploading the picture to
                // it.
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {
                ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();
                twitterConfigBuilder.setDebugEnabled(true);
                twitterConfigBuilder.setOAuthConsumerKey("lxRCnjL6HaMUg7HjxAJC1k6IH");
                twitterConfigBuilder.setOAuthConsumerSecret(
                        "6E3oLs4kln9p4oMkBRi2LceOkXuDYKASlXIm53UEq1wDNC4FxI");
                twitterConfigBuilder.setOAuthAccessToken(
                        "854512192879820800-zcc88HtCEEcHyXO0JjgZJEFKmLP2HUi");
                twitterConfigBuilder.setOAuthAccessTokenSecret(
                        "bUPpgmB6ipYkVb2kQ0LgAOeUPQtzZ78qBRB2iSrHQdJAe");

                Twitter twitter = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
                File file = new File(params[0]);

                StatusUpdate status = new StatusUpdate("This is my view from " + message + "!");
                status.setMedia(file); // set the image to be uploaded here.
                try {
                    twitter.updateStatus(status);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(filePath);
    }

    /**
     * This class receives the SMS sent by the EventBus triggered by IncomingSms.
    */
    public static class OnReceiverEvent {
        private String smsMessage;

        public OnReceiverEvent(String sms) {
            this.smsMessage = sms;
        }

        public String getSmsMessage() {
            return smsMessage;
        }
    }

    /**
     * This method is triggered when TaskActivity receives the SMS. Here the NavigationService
     * is started.
    */
    @Subscribe
    public void onSmsReceived(OnReceiverEvent event) {
        message = event.getSmsMessage();

        // This IF block ensures the SMS is not null. If not, the service starts.
        if (message != null) {
            callNavigationService(message);
        }
    }

    /**
     * This is the method that starts the NavigationService by filing the mIntent when called.
    */
    private void callNavigationService(String message) {
        mIntent.putExtra("message", message); // The SMS, which is the destination name, is passed
                // to NavigationService, because it needs it to file the API request.
        Log.d(TAG,"callNavigationService");
        this.startService(mIntent);  // This line is what actually starts the NavigationService.
    }
}


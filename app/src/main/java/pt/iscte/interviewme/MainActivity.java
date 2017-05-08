package pt.iscte.interviewme;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.microsoft.projectoxford.emotion.contract.Order;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.iscte.interviewme.remoteservices.BingSpeechToText;
import pt.iscte.interviewme.remoteservices.EmotionDetection;

public class MainActivity extends AppCompatActivity implements Observer
{
    private HashMap<String,CheckBox> modalitiesMap;
    private Button recordButton;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private List<RecognizeResult> emotionResultsList;
    private EmotionDetection emotionDetection;
    private EditText emotionFeedback;
    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPerms();

        emotionResultsList = null;

        modalitiesMap = new HashMap<>();
        modalitiesMap.put("speech",(CheckBox) findViewById(R.id.speechBox));
        modalitiesMap.put("emotion", (CheckBox) findViewById(R.id.emotionBox));

        emotionFeedback = (EditText) findViewById(R.id.speechFeedback);

        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera, emotionDetection, emotionResultsList, setCameraDisplayOrientation(this, 1));
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                for(Map.Entry<String, CheckBox> entry : modalitiesMap.entrySet())
                {
                    if(entry.getValue().isChecked())
                    {
                        switch(entry.getKey())
                        {
                            case "speech":
                                startActivity(new Intent(view.getContext(), BingSpeechToText.class));
                                break;

                            case "emotion":
                                emotionDetection = new EmotionDetection(view.getContext());
                                emotionDetection.addObserver(MainActivity.this);
                                emotionFeedback.setText("");
                                emotionFeedback.setVisibility(View.VISIBLE);
                                mPreview.getPreviewCallback().setEmotionDetection(emotionDetection);
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static int setCameraDisplayOrientation(Activity activity,
                                                   int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    private static Camera getCameraInstance()
    {
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d("TAG", "Camera is not available (in use or does not exist)" + e.getMessage());
        }
//        setCameraDisplayOrientation(this, 1, c);
//        c.setDisplayOrientation(90);
        return c; // returns null if camera is unavailable
    }

    private void requestPerms()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            // No explanation needed, we can request the permission.

            System.out.println("Self permission checked.");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                }
                else
                {
                    System.out.println("Record Audio permission denied!");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void postEmotionResults(List<RecognizeResult> result)
    {
        int count = 0;

        for (RecognizeResult r : result)
        {
            List<Map.Entry<String, Double>> collection = r.scores.ToRankedList(Order.DESCENDING);

            String detectedEmotion = collection.get(0).getKey();
            Double emotionScore = collection.get(0).getValue();

            emotionFeedback.append(String.format("\t %s: %f\n", detectedEmotion, emotionScore));

            count++;
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        List<RecognizeResult> result = (List<RecognizeResult>) arg;
        postEmotionResults(result);
    }
}

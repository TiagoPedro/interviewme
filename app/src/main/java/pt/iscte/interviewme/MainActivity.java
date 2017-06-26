package pt.iscte.interviewme;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.emotion.contract.Order;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.iscte.interviewme.remoteservices.EmotionDetection;
import pt.iscte.interviewme.remoteservices.SpeechRecognition;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;

public class MainActivity extends AppCompatActivity implements Observer, ISpeechRecognitionServerEvents
{
    // UI attributes
    private Button recordButton;
    private Button switchCameraButton;
    private EditText emotionFeedback;
    private EditText speechFeedback;

    // Logical attributes
    private HashMap<String,CheckBox> modalitiesMap;
    private List<RecognizeResult> emotionResultsList;
    private EmotionDetection emotionDetection;
    private SpeechRecognition speechRecognition;
    private Synchronizer sync;
    private String previousString;
    public static boolean IS_RECORDING = false;

    // Camera attributes
    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout preview;
    public static int CURRENT_CAMERA_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    @Override
    public void onBackPressed()
    {
        finishAndRemoveTask();
    }

    private void setupEmotionDetector(Context context)
    {
        emotionDetection = new EmotionDetection(context);
        emotionDetection.addObserver(MainActivity.this);
        emotionFeedback.setText("");
        emotionFeedback.setVisibility(View.VISIBLE);
        mPreview.getPreviewCallback().setEmotionDetection(emotionDetection);
    }

    private void setupSpeechRecognition()
    {
        speechRecognition = new SpeechRecognition(this);
    }

    private void startRecording(View view)
    {
        sync.openFile();

        for(Map.Entry<String, CheckBox> entry : modalitiesMap.entrySet())
        {
            if(entry.getValue().isChecked())
            {
                switch(entry.getKey())
                {
                    case "speech":
                        setupSpeechRecognition();
                        speechRecognition.start();
                        break;

                    case "emotion":
                        setupEmotionDetector(view.getContext());
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private void stopRecording()
    {
        if(emotionDetection != null)
        {
            mPreview.getPreviewCallback().setEmotionDetection(null);
            emotionDetection = null;
        }

        sync.closeFile();
    }

    @SuppressWarnings("deprecation")
    private void switchCamera()
    {
        if (mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
        preview.removeView(mPreview);

        if(CURRENT_CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            CURRENT_CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else
        {
            CURRENT_CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera = Camera.open(CURRENT_CAMERA_ID);

        setCameraDisplayOrientation(MainActivity.this, CURRENT_CAMERA_ID);

        mPreview = new CameraPreview(this, mCamera, emotionDetection, setCameraDisplayOrientation(this, 1));
        preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
    }

    private void setupButtons()
    {
        // Setting up record button
        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                IS_RECORDING = !IS_RECORDING;
                if(IS_RECORDING)
                {
                    startRecording(view);
                    recordButton.setBackgroundResource(R.mipmap.stop);
                }
                else
                {
                    stopRecording();
                    recordButton.setBackgroundResource(R.mipmap.rec);
                }
            }
        });

        switchCameraButton = (Button) findViewById(R.id.cameraToggle);
        switchCameraButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switchCamera();
            }
        });
    }

    private void initialize()
    {
        emotionResultsList = null;
        previousString = "";
        sync = new Synchronizer();

        // Setting up hashmap with recognized modalities
        modalitiesMap = new HashMap<>();
        modalitiesMap.put("speech",(CheckBox) findViewById(R.id.speechBox));
        modalitiesMap.put("emotion", (CheckBox) findViewById(R.id.emotionBox));

        // Setting up TextViews for feedback
        emotionFeedback = (EditText) findViewById(R.id.emotionFeedback);
        speechFeedback = (EditText) findViewById(R.id.speechFeedback);

        // Setting up Camera Preview and props
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera, emotionDetection, setCameraDisplayOrientation(this, 1));
        preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        setupButtons();
    }

    @SuppressWarnings("deprecation")
    public static int setCameraDisplayOrientation(Activity activity, int cameraId)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation)
        {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }
        else
        {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    private static Camera getCameraInstance()
    {
        Camera c = null;
        try {
            c = Camera.open(CURRENT_CAMERA_ID);
        }
        catch (Exception e)
        {
            Log.d("TAG", "Camera is not available (in use or does not exist)" + e.getMessage());
        }

        return c; // returns null if camera is unavailable
    }

    private String translateEmotion(String originalString)
    {
        String translatedString = "";
        switch(originalString.toLowerCase())
        {
            case "anger":
                translatedString = getString(R.string.anger);
                break;
            case "contempt":
                translatedString = getString(R.string.contempt);
                break;
            case "disgust":
                translatedString = getString(R.string.disgust);
                break;
            case "fear":
                translatedString = getString(R.string.fear);
                break;
            case "happiness":
                translatedString = getString(R.string.happiness);
                break;
            case "neutral":
                translatedString = getString(R.string.neutral);
                break;
            case "sadness":
                translatedString = getString(R.string.sadness);
                break;
            case "surprise":
                translatedString = getString(R.string.surprise);
                break;
            default:
                break;
        }
        return translatedString;
    }

    public void postEmotionResults(List<RecognizeResult> result)
    {
        for (RecognizeResult r : result)
        {
            List<Map.Entry<String, Double>> collection = r.scores.ToRankedList(Order.DESCENDING);

            String firstDetectedEmotion = collection.get(0).getKey();
            String secondDetectedEmotion = collection.get(1).getKey();

            firstDetectedEmotion = translateEmotion(firstDetectedEmotion);
            secondDetectedEmotion = translateEmotion(secondDetectedEmotion);

            Double firstEmotionScore = collection.get(0).getValue() * 100;
            Double secondEmotionScore = collection.get(1).getValue() * 100;
            DecimalFormat df = new DecimalFormat("##.##");
            df.setRoundingMode(RoundingMode.HALF_UP);

            String output = String.format("%s: %s%%\t%s: %s%%\n",
                    firstDetectedEmotion, df.format(firstEmotionScore),
                    secondDetectedEmotion, df.format(secondEmotionScore));

            emotionFeedback.append(output);

            sync.writeToFile(output);
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        List<RecognizeResult> result = (List<RecognizeResult>) arg;
        postEmotionResults(result);
    }

    /*********************************/
    /******** Speech methods *********/
    /*********************************/
    @Override
    public void onPartialResponseReceived(String response)
    {
        this.WriteLine(response);
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult response)
    {
        if(response.Results.length > 0)
            this.WriteLine(response.Results[response.Results.length-1].DisplayText);

        if(MainActivity.IS_RECORDING && response.Results.length > 0)
        {
            speechRecognition.stop();
            setupSpeechRecognition();
            speechRecognition.start();
        }
        else if(MainActivity.IS_RECORDING && response.Results.length == 0)
        {
            stopRecording();
            recordButton.setBackgroundResource(R.mipmap.rec);
        }
        else
        {
            speechRecognition.stop();
        }
    }

    @Override
    public void onIntentReceived(String s)
    {

    }

    @Override
    public void onError(final int errorCode, final String response)
    {
        this.WriteLine("--- Error received by onError() ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
        this.WriteLine("");
    }

    @Override
    public void onAudioEvent(boolean recording)
    {

    }

    private void WriteLine(String text)
    {
        speechFeedback.append(text + "\n");
        sync.writeToFile(text + "\n");
    }
}

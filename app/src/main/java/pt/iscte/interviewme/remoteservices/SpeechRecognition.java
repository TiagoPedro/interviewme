package pt.iscte.interviewme.remoteservices;

import android.app.Activity;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import pt.iscte.interviewme.R;

/**
 * Created by tiago on 25/05/2017.
 */

public class SpeechRecognition
{
    private MicrophoneRecognitionClient micClient;
    private String defaultLocale;
    private SpeechRecognitionMode recMode;
    private FinalResponseStatus isReceivedResponse;
    private boolean isActive;

    private Activity hostActivity;

    private enum FinalResponseStatus { NotReceived, OK, Timeout }

    public SpeechRecognition(Activity hostActivity)
    {
        defaultLocale = "pt-PT";
        isReceivedResponse = FinalResponseStatus.NotReceived;
        recMode = SpeechRecognitionMode.ShortPhrase;
        this.hostActivity = hostActivity;
    }

    public boolean getActiveStatus()
    {
        return isActive;
    }

    public void setActiveStatus(boolean status)
    {
        isActive = status;
    }

    private String getPrimaryKey()
    {
        return hostActivity.getString(R.string.sttPrimaryKey);
    }

    private String getAuthenticationUri()
    {
        return hostActivity.getString(R.string.authenticationUri);
    }

    public MicrophoneRecognitionClient getMicClient()
    {
        return micClient;
    }

    public void start()
    {
        if (micClient == null)
        {
            micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                    hostActivity,
                    this.recMode,
                    this.defaultLocale,
                    (ISpeechRecognitionServerEvents) hostActivity,
                    this.getPrimaryKey());

            micClient.setAuthenticationUri(this.getAuthenticationUri());
        }
        isActive = true;
        micClient.startMicAndRecognition();
    }

    public void stop()
    {
        if(micClient != null)
        {
            micClient.endMicAndRecognition();
            micClient = null;
        }
        defaultLocale = "";
        recMode = null;
        isActive = false;
    }
}

package pt.iscte.interviewme.remoteservices;

/**
 * Created by tiago on 27/02/2017.
 */

import pt.iscte.interviewme.R;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

//import com.microsoft.bing.speech.Conversation;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.InputStream;


public class BingSpeechToText extends Activity implements ISpeechRecognitionServerEvents, RemoteService
{
    private MicrophoneRecognitionClient micClient;
    private FinalResponseStatus isReceivedResponse;
    private String defaultLocale;
    EditText _logText;
    private boolean activeStatus;
    private SpeechRecognitionMode recMode;

    private enum FinalResponseStatus { NotReceived, OK, Timeout }

    public BingSpeechToText()
    {
        defaultLocale = "pt-PT";
        isReceivedResponse = FinalResponseStatus.NotReceived;
        activeStatus = false;
        recMode = SpeechRecognitionMode.ShortPhrase;
    }

    private String getPrimaryKey()
    {
        return this.getString(R.string.sttPrimaryKey);
    }

    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }

    private SpeechRecognitionMode getMode() {
/*
        int id = this._radioGroup.getCheckedRadioButtonId();
        if (id == R.id.micDictationRadioButton ||
                id == R.id.dataLongRadioButton) {
            return SpeechRecognitionMode.LongDictation;
        }
*/

        return SpeechRecognitionMode.ShortPhrase;
    }

    private Boolean getUseMicrophone() {
//        int id = this._radioGroup.getCheckedRadioButtonId();
//        return id == R.id.micIntentRadioButton ||
//                id == R.id.micDictationRadioButton ||
//                id == (R.id.micRadioButton - 1);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        this._speechFeedback = (TextView) findViewById(R.id.speechFeedback);
//        this._speechFeedback.setText("");
//        this._speechFeedback.setVisibility(View.VISIBLE);
        this._logText = (EditText) findViewById(R.id.speechFeedback);
        this._logText.setText("");
        this._logText.setVisibility(View.VISIBLE);

//        requestPerms();
        start();
    }

    @Override
    public void start()
    {
        if (micClient == null)
        {
            micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                    this,
                    this.recMode,
                    this.defaultLocale,
                    this,
                    this.getPrimaryKey());

            micClient.setAuthenticationUri(this.getAuthenticationUri());
        }
        micClient.startMicAndRecognition();
    }

    @Override
    public void stop()
    {
        micClient = null;
        defaultLocale = "";
        activeStatus = false;
        recMode = null;
    }

    @Override
    public boolean getActiveStatus()
    {
        return activeStatus;
    }

    @Override
    public void setActiveStatus(boolean status)
    {
        activeStatus = status;
    }

    @Override
    public void onPartialResponseReceived(final String response)
    {
        this.WriteLine("--- Partial result received by onPartialResponseReceived() ---");
        this.WriteLine(response);
        this.WriteLine();
    }

    @Override
    public void onFinalResponseReceived(final RecognitionResult response)
    {
        boolean isFinalDictationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && this.getUseMicrophone() && ((this.getMode() == SpeechRecognitionMode.ShortPhrase) || isFinalDictationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDictationMessage) {
//            this._startButton.setEnabled(true);
            this.isReceivedResponse = FinalResponseStatus.OK;
        }

        if (!isFinalDictationMessage) {
            this.WriteLine("********* Final n-BEST Results *********");
            for (int i = 0; i < response.Results.length; i++) {
                this.WriteLine("[" + i + "]" + " Confidence=" + response.Results[i].Confidence +
                        " Text=\"" + response.Results[i].DisplayText + "\"");
            }

            this.WriteLine();
        }
    }

    @Override
    public void onIntentReceived(final String payload)
    {
        this.WriteLine("--- Intent received by onIntentReceived() ---");
        this.WriteLine(payload);
        this.WriteLine();
    }

    @Override
    public void onError(final int errorCode, final String response)
    {
//        this._startButton.setEnabled(true);
        this.WriteLine("--- Error received by onError() ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
        this.WriteLine();
    }

    @Override
    public void onAudioEvent(boolean recording)
    {
        this.WriteLine("--- Microphone status change received by onAudioEvent() ---");
        this.WriteLine("********* Microphone status: " + recording + " *********");
        if (recording) {
            this.WriteLine("Please start speaking.");
        }

        WriteLine();
        if (!recording) {
            this.micClient.endMicAndRecognition();
//            this._startButton.setEnabled(true);
        }
    }

    private void WriteLine() {
        this.WriteLine("");
    }

    private void WriteLine(String text)
    {
        this._logText.append(text + "\n");
//        this._speechFeedback.append(text + "\n");
//        System.out.println(text);
    }

    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode recoMode;
        String filename;

        RecognitionTask(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode, String filename) {
            this.dataClient = dataClient;
            this.recoMode = recoMode;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Note for wave files, we can just send data from the file right to the server.
                // In the case you are not an audio file in wave format, and instead you have just
                // raw data (for example audio coming over bluetooth), then before sending up any
                // audio data, you must first send up an SpeechAudioFormat descriptor to describe
                // the layout and format of your raw audio data via DataRecognitionClient's sendAudioFormat() method.
                // String filename = recoMode == SpeechRecognitionMode.ShortPhrase ? "whatstheweatherlike.wav" : "batman.wav";
                InputStream fileStream = getAssets().open(filename);
                int bytesRead = 0;
                byte[] buffer = new byte[1024];

                do {
                    // Get  Audio data to send into byte buffer.
                    bytesRead = fileStream.read(buffer);

                    if (bytesRead > -1) {
                        // Send of audio data to service.
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            finally {
                dataClient.endAudio();
            }

            return null;
        }
    }
}


package pt.iscte.interviewme;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.widget.CheckBox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pt.iscte.interviewme.remoteservices.BingSpeechToText;

/**
 * Created by tiago on 27/02/2017.
 */

public class ServiceManager implements IManager
{
    private static ServiceManager instance = null;

    private BingSpeechToText _bingSpeechToText;
//    private WatsonToneAnalyzer              _watsonToneAnalyzer;
//
//    private BingSpeechTranslation           _bingSpeechTranslation;
//    private WatsonTranslation               _watsonSpeechTranslation;
//
//    private AffectivaEmotionRecognition     _affectivaEmotionRecognition;
//    private BingVisualEmotionRecognition    _bingVisualEmotionRecognition;
//
//    private StatisticsModeler               _statisticsModeler;
//
//    private Synchronizer                    _synchronizer;

//    private Activity hostActivity;

    protected ServiceManager()
    {
        start();
    }

    public static ServiceManager getInstance()
    {
        if(instance == null)
            instance = new ServiceManager();

        return instance;
    }

    public void setHostActivity()
    {

    }

    @Override
    public void start()
    {
        _bingSpeechToText = new BingSpeechToText();
    }

    @Override
    public void stop()
    {

    }

    @Override
    public void update()
    {

    }

    public void startRecording(HashMap<String,CheckBox> modalitiesMap)
    {
        for(Map.Entry<String,CheckBox> entry : modalitiesMap.entrySet()) {
            switch(entry.getKey())
            {
                case "speech":
                    if(entry.getValue().isChecked())
                        _bingSpeechToText.start();
                default:
                    //@ To do
                    return;
            }

        }
    }
}

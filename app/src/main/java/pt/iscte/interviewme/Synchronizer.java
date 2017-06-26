package pt.iscte.interviewme;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;

/**
 * Created by tiago on 24/05/2017.
 */

public class Synchronizer implements Serializable
{
    private OutputStreamWriter outputStreamWriter;
    private transient FileOutputStream fOut;
    private final File folderPath = Environment.getExternalStorageDirectory();
    private static long initTimeStamp;
    private static long currentTimeStamp;

    void openFile()
    {
        File file = new File(folderPath, "output.txt");
        initTimeStamp = System.currentTimeMillis();
        try
        {
            file.createNewFile();
            fOut = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fOut);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void writeToFile(String data)
    {
        currentTimeStamp = System.currentTimeMillis() - initTimeStamp;
        String output = currentTimeStamp + ": " + data;
        try
        {
            outputStreamWriter.append(output);
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    void closeFile()
    {
        try
        {
            outputStreamWriter.close();
            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}


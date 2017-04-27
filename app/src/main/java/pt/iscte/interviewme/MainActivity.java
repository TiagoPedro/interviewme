package pt.iscte.interviewme;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.HashMap;

import pt.iscte.interviewme.remoteservices.BingSpeechToText;

public class MainActivity extends AppCompatActivity
{
    HashMap<String,CheckBox> modalitiesMap;
//    CheckBox speechBox;
    Button recordButton;
//    Activity This = this;
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPerms();

/*        this._logText = (EditText) findViewById(R.id.editText1);
        this._radioGroup = (RadioGroup)findViewById(R.id.groupMode);
        this._buttonSelectMode = (Button)findViewById(R.id.buttonSelectMode);
        this._startButton = (Button) findViewById(R.id.button1);*/
        modalitiesMap = new HashMap<>();
        modalitiesMap.put("speech",(CheckBox) findViewById(R.id.speechBox));
        recordButton = (Button) findViewById(R.id.recordButton);

//        if (getString(R.string.primaryKey).startsWith("Please"))
//        {
//            new AlertDialog.Builder(this)
//                    .setTitle(getString(R.string.add_subscription_key_tip_title))
//                    .setMessage(getString(R.string.add_subscription_key_tip))
//                    .setCancelable(false)
//                    .show();
//        }

//        Context thisContext = this.getBaseContext();

        recordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
//                ServiceManager.getInstance().startRecording(modalitiesMap);
                startActivity(new Intent(view.getContext(), BingSpeechToText.class));
            }
        });
    }

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }*/

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
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}

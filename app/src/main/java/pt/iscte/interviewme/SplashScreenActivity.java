package pt.iscte.interviewme;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;


public class SplashScreenActivity extends Activity
{
    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);

        requestPerms();
    }

    private void requestPerms()
    {
        Map<String, Integer> permsList = new HashMap<>();
        permsList.put("RECORD_AUDIO", ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO));
        permsList.put("WRITE_EXTERNAL_STORAGE", ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        permsList.put("READ_PHONE_STATE", ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE));
        permsList.put("CAMERA", ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA));

        ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.CAMERA},
                                ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        if(requestCode == ASK_MULTIPLE_PERMISSION_REQUEST_CODE)
        {
            // If request is cancelled, the result arrays are empty.
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
            else
            {
                System.out.println("Permissions denied!");
                System.exit(0);
            }
        }
    }
}

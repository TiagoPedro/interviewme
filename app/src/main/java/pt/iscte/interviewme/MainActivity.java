package pt.iscte.interviewme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.CameraDetector.CameraType;
import com.affectiva.android.affdex.sdk.Frame.ROTATE;

public class MainActivity extends AppCompatActivity
{
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //String text =
        //"I know the times are difficult! Our sales have been "
        //+ "disappointing for the past three quarters for our data analytics "
        //+ "product suite. We have a competitive data analytics product "
        //+ "suite in the industry. But we need to do our job selling it! "
        //+ "We need to acknowledge and fix our sales challenges. "
        //+ "We can’t blame the economy for our lack of execution! "
        //+ "We are missing critical sales opportunities. "
        //+ "Our product is in no way inferior to the competitor products. "
        //+ "Our clients are hungry for analytical tools to improve their "
        //+ "business outcomes. Economy has nothing to do with it.";
        //
        //
        //ToneAnalyzer toneAnalyzer = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
        //toneAnalyzer.setUsernameAndPassword("51d278da-5f47-46a5-b686-c9812bacbb40", "HbqQPVoOvq88");
        //toneAnalyzer.getTone(text, null).enqueue(new ServiceCallback<ToneAnalysis>() {
        //@Override
        //public void onFailure(Exception e) {}
        //
        //@Override
        //public void onResponse(final ToneAnalysis tone) {
        //// Run view-related code back on the main thread
        //MainActivity.this.runOnUiThread(new Runnable() {
        //@Override
        //public void run() {
        //System.out.print(tone);
        //}
        //});
        //};
        //});
        //
        //CameraDetector detector = new CameraDetector(this, CameraType.CAMERA_FRONT, surfaceView);
        //
        //int rate = 10;
        //detector.setMaxProcessRate(rate);
        //detector.setSendUnprocessFrames(true);
    }

//
//@Override
//public void onCameraSizeSelected(int cameraWidth, int cameraHeight, ROTATE rotation)
//{
//int cameraPreviewWidth;
//int cameraPreviewHeight;
//
////cameraWidth and cameraHeight report the unrotated dimensions of the camera frames, so switch
//// the width and height if necessary
//
//if (rotation == ROTATE.BY_90_CCW || rotation == ROTATE.BY_90_CW) {
//cameraPreviewWidth = cameraHeight;
//cameraPreviewHeight = cameraWidth;
//} else {
//cameraPreviewWidth = cameraWidth;
//cameraPreviewHeight = cameraHeight;
//}
//
////retrieve the width and height of the ViewGroup object containing our SurfaceView (in an actual
//// application, we would want to consider the possibility that the mainLayout object may not
//// have been sized yet)
//
//RelativeLayout rlParent = (RelativeLayout) findViewById(R.id.activity_main);
//
//int layoutWidth = rlParent.getWidth();
//int layoutHeight = rlParent.getHeight();
//
////compute the aspect Ratio of the ViewGroup object and the cameraPreview
//
//float layoutAspectRatio = (float)layoutWidth/layoutHeight;
//float cameraPreviewAspectRatio = (float)cameraWidth/cameraHeight;
//
//int newWidth;
//int newHeight;
//
//if (cameraPreviewAspectRatio > layoutAspectRatio) {
//newWidth = layoutWidth;
//newHeight =(int) (layoutWidth / cameraPreviewAspectRatio);
//} else {
//newWidth = (int) (layoutHeight * cameraPreviewAspectRatio);
//newHeight = layoutHeight;
//}
//
////size the SurfaceView
//surfaceView = new SurfaceView(this.getApplicationContext());
//
//ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
//params.height = newHeight;
//params.width = newWidth;
//surfaceView.setLayoutParams(params);
//}
}

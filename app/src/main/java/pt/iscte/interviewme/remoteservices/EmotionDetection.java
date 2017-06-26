package pt.iscte.interviewme.remoteservices;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;

import pt.iscte.interviewme.R;
import pt.iscte.interviewme.Synchronizer;


public class EmotionDetection extends Observable
{
    private EmotionServiceClient client;
    private Context parentContext;

    public EmotionDetection(Context context)
    {
        parentContext = context;
        if (client == null)
            client = new EmotionServiceRestClient(parentContext.getString(R.string.emotionSubscriptionKey));
    }

    public void recognizeImageEmotion(Uri imageUri) throws IOException
    {
        Bitmap bmap = MediaStore.Images.Media.getBitmap(parentContext.getContentResolver(), imageUri);
        File file = new File(imageUri.getPath());
        file.delete();
        String faceSubscriptionKey = parentContext.getString(R.string.faceSubscriptionKey);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here"))
            System.out.println("No face subscription key detected.");
        else
        {
            // Do emotion detection using face rectangles provided by Face API.
            try
            {
                if(Build.VERSION.SDK_INT >= 11)
                    new EmotionRecognitionRequest(bmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    new EmotionRecognitionRequest(bmap).execute();
            }
            catch (Exception e)
            {
                System.out.println("Error encountered. Exception is: " + e.toString());
            }
        }
    }

    private List<RecognizeResult> processWithFaceRectangles(Bitmap bmap) throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException
    {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = parentContext.getString(R.string.faceSubscriptionKey);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        if(faces.length == 0)
            Log.d("emotion", String.format("No faces :("));

        if (faces != null)
        {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++)
            {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null)
        {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
//            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    private class EmotionRecognitionRequest extends AsyncTask<String, String, List<RecognizeResult>>
    {
        private Exception e;
        private Bitmap bmap;

        EmotionRecognitionRequest(Bitmap bmap)
        {
            e = null;
            this.bmap = bmap;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args)
        {
            System.out.println("Do in background");
            try
            {
                return processWithFaceRectangles(bmap);
            }
            catch (Exception e)
            {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result)
        {
            super.onPostExecute(result);
            // Display based on error existence

            if (e != null)
            {
                System.out.println("Error: " + e.getMessage());
                this.e = null;
            }
            else
            {
                if (result.size() == 0)
                {
                    System.out.println("No emotion detected :(");
                }
                else
                {
                    EmotionDetection.this.setChanged();
                    EmotionDetection.this.notifyObservers(result);
                }
            }
        }
    }
}

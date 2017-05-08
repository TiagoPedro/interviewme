package pt.iscte.interviewme.remoteservices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import pt.iscte.interviewme.R;


public class EmotionDetection extends Observable
{
    private EmotionServiceClient client;
    private ArrayList<Bitmap> bmapList;
    private List<RecognizeResult> emotionResultsList;
    private Context parentContext;

    public EmotionDetection(Context context)
    {
        parentContext = context;
        if (client == null)
            client = new EmotionServiceRestClient(parentContext.getString(R.string.emotionSubscriptionKey));

        bmapList = new ArrayList<>();
        emotionResultsList = null;
    }

//    public void recognizeImageEmotion(Bitmap bmap, List<RecognizeResult> emotionResultsList)
//    {
//        ImageHelper.
//
//        String faceSubscriptionKey = parentContext.getString(R.string.faceSubscriptionKey);
//        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here"))
//        {
//            System.out.println("No face subscription key detected.");
//        }
//        else
//        {
//            // Do emotion detection using face rectangles provided by Face API.
//            try
//            {
//                if(Build.VERSION.SDK_INT >= 11)
//                {
//                    int corePoolSize = 60;
//                    int maximumPoolSize = 80;
//                    int keepAliveTime = 10;
//
//                    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
//                    Executor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
//
//                    new EmotionRecognitionRequest(bmap, emotionResultsList).executeOnExecutor(threadPoolExecutor);
//                }
//
//                else
//                    new EmotionRecognitionRequest(bmap, emotionResultsList).execute();
////                new EmotionRecognitionRequest(bmap, emotionResultsList).execute();
//            }
//            catch (Exception e)
//            {
//                System.out.println("Error encountered. Exception is: " + e.toString());
//            }
//        }
//        bmapList.add(bmap);
//    }

    public void recognizeImageEmotion(Uri imageUri, List<RecognizeResult> emotionResultsList) throws IOException
    {
        Bitmap bmap = MediaStore.Images.Media.getBitmap(parentContext.getContentResolver(), imageUri);
        String faceSubscriptionKey = parentContext.getString(R.string.faceSubscriptionKey);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here"))
            System.out.println("No face subscription key detected.");
        else
        {
            // Do emotion detection using face rectangles provided by Face API.
            try
            {
                if(Build.VERSION.SDK_INT >= 11)
                    new EmotionRecognitionRequest(bmap, emotionResultsList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    new EmotionRecognitionRequest(bmap, emotionResultsList).execute();
            }
            catch (Exception e)
            {
                System.out.println("Error encountered. Exception is: " + e.toString());
            }
        }
        bmapList.add(bmap);
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
        else
            Log.d("emotion", String.format("Faces length: %d", faces.length));

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
            Log.d("emotion", "Start emotion detection using Emotion API");
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

//    private void customNotifyObservers()
//    {
//        notifyObservers();
//    }

    private class EmotionRecognitionRequest extends AsyncTask<String, String, List<RecognizeResult>>
    {
        private Exception e;
        private Bitmap bmap;
        private List<RecognizeResult> emotionResultsList;

        EmotionRecognitionRequest(Bitmap bmap, List<RecognizeResult> emotionResultsList)
        {
            e = null;
            this.bmap = bmap;
            this.emotionResultsList = emotionResultsList;
        }

//        @Override
//        protected void onPreExecute()
//        {
//            System.out.println("Pre executing");
//            super.onPreExecute();
//        }

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
                    this.emotionResultsList = result;
//                    if(EmotionDetection.this.hasChanged())
//                    {
                    EmotionDetection.this.setChanged();
                    EmotionDetection.this.notifyObservers(result);
//                        EmotionDetection.this.clearChanged();
//                    customNotifyObservers();
//                    }
                }
            }
        }
    }
}

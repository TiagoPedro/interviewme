package pt.iscte.interviewme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import pt.iscte.interviewme.remoteservices.EmotionDetection;

/** Camera preview class */

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    public SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback pc;
    private int cameraOrientaiton;

    public CameraPreview(Context context, Camera camera, EmotionDetection emotionDetection, int cameraOrientation) {
        super(context);
        mCamera = camera;
        pc = new PreviewCallback(emotionDetection);
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.cameraOrientaiton = cameraOrientation;
    }

    public PreviewCallback getPreviewCallback()
    {
        return pc;
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(cameraOrientaiton);
            mCamera.setPreviewCallback(pc);
            mCamera.startPreview();
        }
        catch (IOException e)
        {
            Log.d("TAG", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        if (mHolder.getSurface() == null)
        {
            return;
        }

        try
        {
            mCamera.stopPreview();
        }
        catch (Exception e)
        {

        }

        try
        {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(cameraOrientaiton);
            mCamera.setPreviewCallback(pc);
            mCamera.startPreview();

        } catch (Exception e)
        {
            Log.d("TAG", "Error starting camera preview: " + e.getMessage());
        }
    }
}

@SuppressWarnings("deprecation")
class PreviewCallback implements Camera.PreviewCallback
{
    private Bitmap bmap;
    private long _baseTime = System.currentTimeMillis();
    private EmotionDetection emotionDetection;
    private Uri mUriPhotoTaken;

    PreviewCallback(EmotionDetection emotionDetection)
    {
        this.emotionDetection = emotionDetection;
    }

    void setEmotionDetection(EmotionDetection emotionDetection)
    {
        this.emotionDetection = emotionDetection;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if(emotionDetection != null)
        {
            if(System.currentTimeMillis() - _baseTime >= 2*1000)
            {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();

                int imageFormat = parameters.getPreviewFormat();

                if (imageFormat == ImageFormat.NV21)
                {
                    YuvImage img = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    img.compressToJpeg(new android.graphics.Rect(0, 0, img.getWidth(), img.getHeight()), 50, out);
                    byte[] imageBytes = out.toByteArray();
                    bmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    Matrix matrix = new Matrix();
                    if(MainActivity.CURRENT_CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        matrix.postRotate(-90);
                    else
                        matrix.postRotate(90);
                    Bitmap rotatedBmap = Bitmap.createBitmap(bmap, 0, 0, bmap.getWidth(), bmap.getHeight(), matrix, true);

                    File storageDir = Environment.getExternalStorageDirectory();
                    FileOutputStream fileStream = null;
                    File file = null;
                    try
                    {
                        file = File.createTempFile("IMG_", ".jpg", storageDir);
                        fileStream = new FileOutputStream(file);
                    }
                    catch (IOException e)
                    {
//                        setInfo(e.getMessage());
                    }
                    rotatedBmap.compress(Bitmap.CompressFormat.PNG, 50, fileStream);
                    mUriPhotoTaken = Uri.fromFile(file);

                    _baseTime = System.currentTimeMillis();
                }

                try
                {
                    emotionDetection.recognizeImageEmotion(mUriPhotoTaken);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}

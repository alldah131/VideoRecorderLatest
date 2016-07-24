package com.dahlstore.videorecorder2latest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Allan on 2016-07-24.
 */
public class UploadActivity extends Activity {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    private Button btnUpload;
    long totalSize = 0;
    File sourceFile;
    String fileName = null;
    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;
    int serverResponseCode;
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);

        // Changing action bar background color
//        getActionBar().setBackgroundDrawable(
//                new ColorDrawable(Color.parseColor(getResources().getString(
//                        R.color.action_bar))));

        // Receiving the data from previous activity
        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");
        sourceFile = new File(filePath);
        fileName = i.getStringExtra("filePath");


        // boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
//                new UploadFileToServer().execute();
                if(!sourceFile.isFile()) {
                    Toast.makeText(UploadActivity.this, "Source file doesn't exist" + filePath, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(UploadActivity.this, "Source file does exist!" + sourceFile, Toast.LENGTH_SHORT).show();
                    Toast.makeText(UploadActivity.this, "The name of the file is " + fileName, Toast.LENGTH_SHORT).show();
                    try {
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL("http://192.168.0.22/AndroidFileUpload/uploads/fileUpload.php");
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", fileName);
                        dos = new DataOutputStream(conn.getOutputStream());
                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        //Adding Parameter filepath

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        String address="http://192.168.0.22/AndroidFileUpload/uploads/fileUpload.php"+fileName;

                        dos.writeBytes("Content-Disposition: form-data; name=\"filepath\"" + lineEnd);
                        //dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                        //dos.writeBytes("Content-Length: " + name.length() + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(address); // mobile_no is String variable
                        dos.writeBytes(lineEnd);


//Adding Parameter media file(audio,video and image)

                        dos.writeBytes(twoHyphens + boundary + lineEnd);

                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0)
                        {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }

                        // send multipart form data necesssary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                        if (serverResponseCode == 200) {

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                            + "c:/htdocs/AndroidFileUpload/uploads/fileUpload.php";

                                    Toast.makeText(UploadActivity.this,
                                            "File Upload Complete."+ msg, Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();



                    }  catch (MalformedURLException ex) {


                        ex.printStackTrace();

                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(UploadActivity.this,
                                        "MalformedURLException", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    } catch (final Exception e) {


                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(UploadActivity.this,
                                        "Got Exception : see logcat ",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.e("Upload file to server Exception",
                                "Exception : " + e.getMessage(), e);
                        serverResponseCode();
                    }


                }


            }
        });

    }

    public int serverResponseCode(){
        return serverResponseCode;
    }

    /**
     * Displaying captured image/video on the screen
     */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            vidPreview.setVideoPath(filePath);
            // start playing
            vidPreview.start();
        }
    }
}



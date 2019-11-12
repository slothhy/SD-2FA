package com.example.sd2fa;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;
    private static final int SAMPLING_RATE_IN_HZ = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_FACTOR = 2;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;
    private static final int SOUND_SIMILARITY_MODE = 1;
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);
    TextView title, text;

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FileWriter writer;
    private MediaRecorder mRecordManager;
    private Context context;
    private AudioRecord recorder = null;

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!checkPermissions()) {
            requestPermission();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        title = (TextView) findViewById(R.id.name);
        title.setText("SD-2FA");

        text = (TextView) findViewById(R.id.txt);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            int count = 0;

            @Override
            public void run() {
                count++;

                if (count == 1)
                {
                    text.setText("Listening.");
                }
                else if (count == 2)
                {
                    text.setText("Listening..");
                }
                else if (count == 3)
                {
                    text.setText("Listening...");
                }

                if (count == 3)
                    count = 0;

                handler.postDelayed(this, 2 * 1000);
            }
        };
        handler.postDelayed(runnable, 1 * 1000);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int mode = intent.getIntExtra("mode", 0);
        if (mode == SOUND_SIMILARITY_MODE) {
            new RecorderAsyncTask().execute(mode);
        }
    }


    private class RecorderAsyncTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int mode = params[0];

            recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
                    CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

            try {
                Log.d("Recording", "Begins");
                recordingInProgress.set(true);

                if (mode == SOUND_SIMILARITY_MODE) {

                    String path = Environment.getExternalStorageDirectory().getPath() + "/phone.wav";
                    WavRecorder wavRecorder = new WavRecorder(path);
                    wavRecorder.startRecording();
                    Thread.sleep(1500);
                    playTone();
                    Thread.sleep(1500);
                    wavRecorder.stopRecording();
                    Log.d("Recording", "Ends");
                    uploadRecording("/phone.wav");

                }
//                else if (mode == DISTANCE_VERIFICATION_MODE) {
//
//                    String path = Environment.getExternalStorageDirectory().getPath() + "/phone_distance.wav";
//                    WavRecorder wavRecorder = new WavRecorder(path);
//                    wavRecorder.startRecording();
//                    Thread.sleep(1600);
//                    playTone();
//                    Thread.sleep(1500);
//                    wavRecorder.stopRecording();
//
//                    Log.d("Recording", "Ends");
//                    uploadRecording("/phone_distance.wav");
//
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private void playTone() {
            GenerateChirpSignal arr = new GenerateChirpSignal(16000, 20000, 0.05);
            double[] freqArr = arr.getArr();
            int idx = 0;
            final byte generatedSnd[] = new byte[2 * 2205];
            for (final double dVal : freqArr) {
                // scale to maximum amplitude
                final short val = (short) ((dVal * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

            }

            final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    44100, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                    AudioTrack.MODE_STATIC);
            audioTrack.write(generatedSnd, 0, generatedSnd.length);
            audioTrack.play();
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, "Recording Complete", Toast.LENGTH_SHORT).show();
        }


        private void uploadRecording(String filename) {
            try {
                // Upload file
                Log.d("Uploading", "Starts");
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
                String url = "http://192.168.43.178:3000/api/phone";
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                MultipartEntity entity = new MultipartEntity();
                entity.addPart("file", new FileBody(file));
                post.setEntity(entity);
                HttpResponse resp = httpclient.execute(post);
                Log.d("Uploading", resp.getStatusLine().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

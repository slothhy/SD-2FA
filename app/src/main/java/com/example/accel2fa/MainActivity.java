package com.example.accel2fa;

import androidx.annotation.NonNull;
import  androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FileWriter writer;
    private MediaRecorder mRecordManager;

    TextView title,tvx,tvy,tvz;
    EditText txtSub;
    String ip;
    ServerSocket server;

    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

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
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        title = (TextView) findViewById(R.id.name);
        tvx = (TextView) findViewById(R.id.xval);
        tvy = (TextView) findViewById(R.id.yval);
        tvz = (TextView) findViewById(R.id.zval);
        title.setText("Accel2FA");
        txtSub = (EditText) findViewById(R.id.txtSub);


        Runnable conn = new Runnable() {

            public void run() {
                try {
                    server = new ServerSocket(53000);
                    Socket socket = server.accept();

                    while (true) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

                        String str = in.readLine();

                        Log.i("received response from client", str);
                        if (str.equals("TEST")) {
                            in.close();
                            socket.close();
                            Log.i("received response from client", "start recording");
                            new startRecord().execute();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(conn).start();
    }

    private class startRecord extends AsyncTask {
        @Override
        protected String doInBackground(Object[] objects) {
            //audio
            mRecordManager = new MediaRecorder();
            mRecordManager.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecordManager.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecordManager.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecordManager.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sound.3gp");
            try {
                mRecordManager.prepare();
                mRecordManager.start();
                TimerTask t = new TimerTask() {
                    @Override
                    public void run() {
                        mRecordManager.stop();
                        mRecordManager.release();
                        //play to test
                        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sound.3gp");
                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                        it.setDataAndType(uri,"video/3gpp");
                        startActivity(it);
                    }
                };
                new Timer().schedule(t, 3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Executed";
        }
    }


    public void onStartClick(View view)
    {
        //audio
        mRecordManager = new MediaRecorder();
        mRecordManager.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecordManager.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecordManager.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecordManager.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sound.3gp");
        try {
            mRecordManager.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //accelerometer
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(getBaseContext(), "Started recording", Toast.LENGTH_SHORT).show();
        try {
            writer = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/acc.txt");
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            writer.write(ts + "\n");
            mRecordManager.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onStopClick(View view)
    {
        Toast.makeText(getBaseContext(), "Stopped recording", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener(this);
        mRecordManager.stop();
        mRecordManager.release();
        if(writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ip = txtSub.getText().toString();
        new sendData().execute();
        //play to test
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sound.3gp");
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        it.setDataAndType(uri,"video/3gpp");
        startActivity(it);

    }

    private class sendData extends AsyncTask {
        @Override
        protected String doInBackground(Object[] objects) {
            try {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/acc.txt");
                //Socket socket = new Socket("192.168.86.29", 50505);
                Socket socket = new Socket(ip, 50505);
                OutputStream out = socket.getOutputStream();
                PrintWriter output = new PrintWriter(out);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while((line = br.readLine()) != null){
                    //process the line
                    output.println(line);
                }
                output.flush();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Executed";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        tvx.setText("X = "+ String.valueOf(x));
        tvy.setText("Y = "+ String.valueOf(y));
        tvz.setText("Z = "+ String.valueOf(z));

        try {
            writer.write(x+","+y+","+z+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

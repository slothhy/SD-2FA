package com.example.accelerometer;

import  androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FileWriter writer;
    TextView title,tvx,tvy,tvz;
    EditText txtSub;
    String ip;


    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        title = (TextView)findViewById(R.id.name);
        tvx = (TextView)findViewById(R.id.xval);
        tvy = (TextView)findViewById(R.id.yval);
        tvz = (TextView)findViewById(R.id.zval);
        title.setText("Accelerometer Testing");
        txtSub = (EditText)findViewById(R.id.txtSub);

    }


    public void onStartClick(View view)
    {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(getBaseContext(), "Started recording", Toast.LENGTH_SHORT).show();
        try {
            writer = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/acc.txt");
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            writer.write(ts + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onStopClick(View view)
    {
        Toast.makeText(getBaseContext(), "Stopped recording", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener(this);
        if(writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ip = txtSub.getText().toString();
        new sendData().execute();

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

//    protected void onResume() {
//        super.onResume();
//        try {
//            writer = new FileWriter("/storage/emulated/0/acc/acc.txt");
//            Long tsLong = System.currentTimeMillis()/1000;
//            String ts = tsLong.toString();
//            writer.write(ts + "\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    protected void onPause() {
//        super.onPause();
//
//        if(writer != null) {
//            try {
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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

package com.example.brightness;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.SettingInjectorService;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SeekBar seekbar;
    TextView textView;
    final Handler handler = new Handler();
    final int delay = 15000; // 1000 milliseconds == 1 second
    boolean success = false;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    seekbar = findViewById(R.id.seekBarBrightness);
    textView = findViewById(R.id.textView);
    int cBrightness = Settings.System.getInt(getContentResolver(),
            Settings.System.SCREEN_BRIGHTNESS,0);
            seekbar.setProgress(cBrightness);

        runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("myHandler: here!"); // Do your work here
                handler.postDelayed(this, delay);
                Settings.System.putInt(getApplicationContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,0);
            }
        };

        handler.postDelayed( runnable, delay);

    seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Context context = getApplicationContext();
            boolean canWrite = Settings.System.canWrite(context);
            if (canWrite){
                int sBrightness = i*255/255;
                float rangeSize = 255 - 0;
                float mappedTo0to1 = (i - 0) / rangeSize; // Map to range 0 - 1.
                int mappedResult = (int) (mappedTo0to1 * 100); // Map to range you want. (0 - 255).
                textView.setText(mappedResult+"/100");
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,sBrightness);


                sleepScreen(15000);




            }else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    });
    }
    public void sleepScreen(int milliseconds){
        boolean value = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            value = Settings.System.canWrite(getApplicationContext());
            if (value){
                Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,milliseconds);
                Settings.System.putInt(getApplicationContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,0);
                Toast.makeText(MainActivity.this,"Sleep",Toast.LENGTH_SHORT).show();
                success = true;
            }else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse(getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        }else {
            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,milliseconds);
        }
    }

    public void restart() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, delay);
    }

    @Override
    public void onUserInteraction(){
        Settings.System.putInt(getApplicationContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,255);
        restart();
    }
}
package com.ab.sync;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.ab.sync.View.DatabaseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends AppCompatActivity {

    ProgressBar progressbar;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        prog();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoadingActivity.this, ContactsActivity.class));
                finish();
            }
        },10000);
    }
    private void prog() {

        progressbar = findViewById(R.id.progressbar);

        final Timer t = new Timer();
        TimerTask tt = new TimerTask(){

            @Override
            public void run() {

                counter++;
                progressbar.setProgress(counter);

                if (counter == 100)
                    t.cancel();
            }
        };

        t.schedule(tt,0,100);

    }
}
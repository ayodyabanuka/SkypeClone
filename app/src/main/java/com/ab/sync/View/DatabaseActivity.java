package com.ab.sync.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.ab.sync.ContactsActivity;
import com.ab.sync.Members;
import com.ab.sync.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class DatabaseActivity extends AppCompatActivity {
    Members members;
    FirebaseDatabase database;
    GoogleSignInAccount signInAccount;
    DatabaseReference reference;
    ProgressBar progressbar;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        prog();

        members = new Members();

        reference = database.getInstance().getReference().child("User");
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rundatabase();
                startActivity(new Intent(DatabaseActivity.this, ContactsActivity.class));
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

    private void rundatabase() {

        members.setUserName(signInAccount.getDisplayName());
        reference.child(signInAccount.getDisplayName()).setValue(members);

        members.setImageUrl(signInAccount.getPhotoUrl().toString());
        reference.child(signInAccount.getDisplayName()).setValue(members);


    }
}
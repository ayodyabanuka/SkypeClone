package com.ab.sync;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.ab.sync.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

public class profileActivity extends AppCompatActivity {

    TextView userNameTV,emailTV;
    ImageView profileImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        userNameTV = findViewById(R.id.userName);
        profileImageView = findViewById(R.id.settings_profile_image);
        emailTV = findViewById(R.id.email);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null){
            userNameTV.setText(signInAccount.getDisplayName());
            emailTV.setText(signInAccount.getEmail());
            Picasso.get().load(signInAccount.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(profileImageView);
        }


    }
}
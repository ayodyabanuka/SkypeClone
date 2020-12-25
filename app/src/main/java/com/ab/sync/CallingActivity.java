package com.ab.sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelBtn,acceptCallBtn;

    private String receiverUserImage="",receiverUserName="";
    private String senderUserImage="",senderUserName="",checker = "";
    private String callingName = "",ringingName = "";
    private DatabaseReference userRef;

    private MediaPlayer mediaPlayer;

    GoogleSignInAccount signInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        receiverUserName = getIntent().getExtras().get("visit_user_name").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        nameContact = findViewById(R.id.calling_name);
        profileImage = findViewById(R.id.calling_image);
        cancelBtn = findViewById(R.id.call_end_btn);
        acceptCallBtn = findViewById(R.id.call_btn);

        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        senderUserName = signInAccount.getDisplayName();

        mediaPlayer = MediaPlayer.create(this,R.raw.ringingtone);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                checker = "clicked";

                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                final HashMap<String, Object>callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked","picked");

                userRef.child(senderUserName).child("Ringing").updateChildren(callingPickUpMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(CallingActivity.this,VideoChatActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        getAndSetUserProfileInfo();

    }

    private void getAndSetUserProfileInfo() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(receiverUserName).exists()){
                    receiverUserImage = snapshot.child(receiverUserName).child("imageUrl").getValue().toString();
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                    receiverUserName = snapshot.child(receiverUserName).child("userName").getValue().toString();
                    nameContact.setText(receiverUserName);
                }
                if (snapshot.child(senderUserName).exists()){
                    senderUserImage = snapshot.child(senderUserName).child("imageUrl").getValue().toString();
                    senderUserName = snapshot.child(senderUserName).child("userName").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();

        userRef.child(receiverUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")){



                    final HashMap<String, Object>callingInfo = new HashMap<>();
                    callingInfo.put("calling",receiverUserName);

                    userRef.child(senderUserName).child("Calling").updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                final HashMap<String, Object>ringingInfo = new HashMap<>();
                                ringingInfo.put("ringing",senderUserName);

                              userRef.child(receiverUserName).child("Ringing").updateChildren(ringingInfo);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserName).hasChild("Ringing") && !snapshot.child(senderUserName).hasChild("Calling")){
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }
                if (snapshot.child(receiverUserName).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this,VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelCallingUser(){

        //sender Side
        userRef.child(senderUserName).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("calling")){
                    callingName = snapshot.child("calling").getValue().toString();

                    userRef.child(callingName).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.child(senderUserName).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(CallingActivity.this,ContactsActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }else{
                    startActivity(new Intent(CallingActivity.this,ContactsActivity.class));
                    finish();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
//receiverside

        userRef.child(senderUserName).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("ringing")){
                    ringingName = snapshot.child("ringing").getValue().toString();

                    userRef.child(ringingName).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.child(senderUserName).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(CallingActivity.this,ContactsActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }else{
                    startActivity(new Intent(CallingActivity.this,ContactsActivity.class));
                    finish();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
}
package com.ab.sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;


import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements  Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_Key = "47047334";
    private static String SESSION_ID = "2_MX40NzA0NzMzNH5-MTYwODAzOTcyMjk5OH5ZMFZCdmowbW9qMHJNVU5DdUZHRGZXdHB-fg";
    private static  String TOKEN = "T1==cGFydG5lcl9pZD00NzA0NzMzNCZzaWc9YTg3ZmFjNWJmYjI1NTE2ZmNlYzU4NWIzMzRhMzVhMWFmZjUyMmQzMzpzZXNzaW9uX2lkPTJfTVg0ME56QTBOek16Tkg1LU1UWXdPREF6T1RjeU1qazVPSDVaTUZaQ2Rtb3diVzlxTUhKTlZVNURkVVpIUkdaWGRIQi1mZyZjcmVhdGVfdGltZT0xNjA4MDM5NzgwJm5vbmNlPTAuNzM5Nzk3MjI4ODQyNDI3JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE2MTA2MzE3NzkmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;

    private Session mSession;
    private Publisher mPublisher;

    private Subscriber mSubscriber;

    private ImageView closeVideoChatBtn;
    private  DatabaseReference usersRef;
    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        usersRef = FirebaseDatabase.getInstance().getReference().child("User");

        closeVideoChatBtn = findViewById(R.id.closeVideoChat);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(userName).hasChild("Ringing")){
                            usersRef.child(userName).child("Ringing").removeValue();

                            if (mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            Intent callinguserintent = new Intent(VideoChatActivity.this,ContactsActivity.class);
                            startActivity(callinguserintent);
                            finish();
                        }
                        if (snapshot.child(userName).hasChild("Calling")){
                            usersRef.child(userName).child("Calling").removeValue();
                            if (mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            Intent callinguserintent = new Intent(VideoChatActivity.this,ContactsActivity.class);
                            startActivity(callinguserintent);
                            finish();
                        }else{
                            if (mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            Intent callinguserintent = new Intent(VideoChatActivity.this,ContactsActivity.class);
                            startActivity(callinguserintent);
                            finish();
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);


    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String[] prems = {Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this,prems)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //1.initialize and connect to the session
                mSession = new Session.Builder(this,API_Key,SESSION_ID).build();
                mSession.setSessionListener(VideoChatActivity.this);
                mSession.connect(TOKEN);

        }else{
            EasyPermissions.requestPermissions(this,"Hey this app need Mic and Camera Permissions",RC_VIDEO_APP_PERM,prems);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher = new  Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView)mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");

        if (mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");

        if(mSubscriber != null){
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
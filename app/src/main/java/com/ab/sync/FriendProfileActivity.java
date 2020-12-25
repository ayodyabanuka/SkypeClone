package com.ab.sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FriendProfileActivity extends AppCompatActivity {

    private String receiverUserID="",receiverUserImage="",receiverUserName="";
    private ImageView profileImage;
    private TextView name;
    private Button addFriend,declineFriend;

    private String senderUserName;
    private String currentState = "new";
    private DatabaseReference friendRequestRef,contactsRef;
    GoogleSignInAccount signInAccount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        senderUserName = signInAccount.getDisplayName();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserImage = getIntent().getExtras().get("profile_image").toString();
        receiverUserName = getIntent().getExtras().get("profile_name").toString();

        profileImage = findViewById(R.id.settings_profile_image);
        name = findViewById(R.id.userName);
        addFriend = findViewById(R.id.accept);
        declineFriend = findViewById(R.id.decline);

        Picasso.get().load(receiverUserImage).into(profileImage);
        name .setText(receiverUserName);


        manageClickEvents();
    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserName)){
                    String requestType = snapshot.child(receiverUserName).child("request_type").getValue().toString();
                    if (requestType.equals("sent")){
                        currentState = "request_sent";
                        addFriend.setText("Cancel Friend Request");
                    }
                    else if (requestType.equals("received")){
                        currentState = "request_received";
                        addFriend.setText("Accept Friend Request");
                        declineFriend.setVisibility(View.VISIBLE);
                        declineFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }else{
                        contactsRef.child(senderUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(receiverUserName)){
                                    currentState = "friends";
                                    addFriend.setText("Delete Contact");
                                }else{
                                    currentState = "new";
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (senderUserName.equals(receiverUserName)){
            addFriend.setVisibility(View.GONE);
        }else{
            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new")){
                        sendFriendRequest();
                    }
                    if (currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if (currentState.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if (currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }
                }
            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(senderUserName).child(receiverUserName).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactsRef.child(receiverUserName).child(senderUserName).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestRef.child(senderUserName).child(receiverUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            friendRequestRef.child(receiverUserName).child(senderUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        currentState = "friends";
                                                        addFriend.setText("Delete Account");
                                                        declineFriend.setVisibility(View.GONE);
                                                        addFriend.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                contactsRef.child(senderUserName).child(receiverUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            friendRequestRef.child(receiverUserName).child(senderUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        currentState = "new";
                                                                                        addFriend.setText("Add Friend");
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserName).child(receiverUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserName).child(senderUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                currentState = "new";
                                addFriend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserName).child(receiverUserName).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserName).child(senderUserName).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                currentState = "request_sent";
                                addFriend.setText("Cancel Friend Request");
                                addFriend.setBackgroundColor(Color.RED);
                                Toast.makeText(FriendProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
package com.ab.sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView notification_list;
    private DatabaseReference friendRequestRef,contactsRef,usersRef;
    private String currentUserName;
    GoogleSignInAccount signInAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);;

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("User");

        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        currentUserName = signInAccount.getDisplayName();

        notification_list = findViewById(R.id.notifications_list);
        notification_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Members>().setQuery(friendRequestRef.child(currentUserName),Members.class).build();
        FirebaseRecyclerAdapter<Members,NotificationsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Members, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationsViewHolder notificationsViewHolder, int i, @NonNull Members members) {
                notificationsViewHolder.accept.setVisibility(View.VISIBLE);
                notificationsViewHolder.delete.setVisibility(View.VISIBLE);

                final String listUserName = getRef(i).getKey();
                DatabaseReference requestTypeRef = getRef(i).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = snapshot.getValue().toString();

                            if (type.equals("received")) {
                                notificationsViewHolder.cardView.setVisibility(View.VISIBLE);

                                    usersRef.child(listUserName).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            final String nameStr = snapshot.child("userName").getValue().toString();
                                            final String imageStr = snapshot.child("imageUrl").getValue().toString();
                                            notificationsViewHolder.userNameText.setText(nameStr);
                                            Picasso.get().load(imageStr).into(notificationsViewHolder.profileImageView);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    notificationsViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            contactsRef.child(currentUserName).child(listUserName).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        contactsRef.child(listUserName).child(currentUserName).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    friendRequestRef.child(currentUserName).child(listUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                friendRequestRef.child(listUserName).child(currentUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            Toast.makeText(NotificationActivity.this, "New Contact Saved!", Toast.LENGTH_SHORT).show();
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
                                    });
                                    notificationsViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            friendRequestRef.child(currentUserName).child(listUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        friendRequestRef.child(listUserName).child(currentUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(NotificationActivity.this, "Request Deleted!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            } else {
                                notificationsViewHolder.cardView.setVisibility(View.GONE);
                            }
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_design, parent, false);
                NotificationsViewHolder viewHolder = new NotificationActivity.NotificationsViewHolder(view);
                return viewHolder;
            }
        };
        notification_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        CardView cardView;
        ImageView profileImageView;
        Button accept,delete;


        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.name_notification);
            profileImageView = itemView.findViewById(R.id.notification_image);
            cardView = itemView.findViewById(R.id.user_notification_card_view);
            accept = itemView.findViewById(R.id.addFriend);
            delete = itemView.findViewById(R.id.deleteFriend);

        }
    }

}
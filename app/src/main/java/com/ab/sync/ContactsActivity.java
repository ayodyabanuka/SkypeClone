package com.ab.sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.sync.auth.LoginActivity;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ContactsActivity extends AppCompatActivity {

    RecyclerView myContactList;
    ImageView findpeoplebtn;
    BottomNavigationView navView;
    Members members;
    FirebaseDatabase database;
    DatabaseReference reference;
    GoogleSignInAccount signInAccount;
    private DatabaseReference contactsRef,usersRef;
    private String currentUserName;
    private String userName = "",profileImage = "";
    private String calledBy = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("User");
        members = new Members();

        findpeoplebtn = findViewById(R.id.find_people_btn);
        myContactList = findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        reference = database.getInstance().getReference().child("User");
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        currentUserName = signInAccount.getDisplayName();



        findpeoplebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(intent);
            }
        });



        }



    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.navigation_home:
                 break;

                case R.id.navigation_settings:
                    Intent settingsIntent = new Intent(ContactsActivity.this, profileActivity.class);
                    startActivity(settingsIntent);
                    break;

                case R.id.navigation_notifications:
                    Intent notificationIntent = new Intent(ContactsActivity.this,NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;

                case R.id.navigation_logout:
                FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(ContactsActivity.this,LoginActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
            }

            return true;

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        checkForReceivingCall();

        FirebaseRecyclerOptions<Members> options = new FirebaseRecyclerOptions.Builder<Members>().setQuery(contactsRef.child(currentUserName),Members.class).build();

        FirebaseRecyclerAdapter<Members,ContactViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Members, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactViewHolder contactViewHolder, int i, @NonNull Members members) {
                final String listUserName = getRef(i).getKey();
                if (currentUserName.equals(listUserName)){
                    contactViewHolder.itemView.setVisibility(View.GONE);
                }else{
                    usersRef.child(listUserName).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                userName = snapshot.child("userName").getValue().toString();
                                profileImage = snapshot.child("imageUrl").getValue().toString();

                                contactViewHolder.userNameText.setText(userName);
                                Picasso.get().load(profileImage).into(contactViewHolder.profileImageView);

                            }
                            contactViewHolder.videoCallBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent callintent = new Intent(ContactsActivity.this,CallingActivity.class);
                                    callintent.putExtra("visit_user_name",listUserName);
                                    startActivity(callintent);
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }


                }



            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                ContactViewHolder viewHolder = new ContactsActivity.ContactViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        CardView cardView;
        ImageView profileImageView;
        Button videoCallBtn;


        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.name_contact);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.contact_card_view);
            videoCallBtn = itemView.findViewById(R.id.call_btn);

        }
    }
    private void checkForReceivingCall() {
        usersRef.child(currentUserName).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ringing")){
                    calledBy = snapshot.child("ringing").getValue().toString();

                    Intent callingintent = new Intent(ContactsActivity.this,CallingActivity.class);
                    callingintent.putExtra("visit_user_name",calledBy);
                    startActivity(callingintent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.ab.sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {

    private RecyclerView findFriendList;
    private EditText searchET;
    private String str="";
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        usersRef = FirebaseDatabase.getInstance().getReference().child("User");

        searchET = findViewById(R.id.search_user_text);
        findFriendList = findViewById(R.id.find_friend_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchET.getText().toString().equals("")){
                    Toast.makeText(FindPeopleActivity.this, "Please enter a name of contact", Toast.LENGTH_SHORT).show();
                }else {
                    str = s.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Members> options = null;
        if (str.equals("")){
            options = new FirebaseRecyclerOptions.Builder<Members>().setQuery(usersRef,Members.class).build();
        }else{
            options = new FirebaseRecyclerOptions.Builder<Members>().setQuery(usersRef.orderByChild("userName").startAt(str).endAt(str + "\uf8ff"),Members.class).build();
        }
        FirebaseRecyclerAdapter<Members,FindFriendsViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Members, FindFriendsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, int i, @NonNull Members members) {
                findFriendsViewHolder.userNameText.setText(members.getUserName());

                Picasso.get().load(members.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(findFriendsViewHolder.profileImageView);

                findFriendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FindPeopleActivity.this, FriendProfileActivity.class);
                        intent.putExtra("profile_image",members.getImageUrl());
                        intent.putExtra("profile_name",members.getUserName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        findFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        CardView cardView;
        ImageView profileImageView;


        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.name_findPeople);
            profileImageView = itemView.findViewById(R.id.image_findPeople);
            cardView = itemView.findViewById(R.id.user_card_view);
        }
    }


}
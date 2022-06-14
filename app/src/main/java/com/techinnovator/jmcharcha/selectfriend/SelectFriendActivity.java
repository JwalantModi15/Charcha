package com.techinnovator.jmcharcha.selectfriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techinnovator.jmcharcha.ChatActivity;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.common.Extras;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SelectFriendAdapter selectFriendAdapter;
    private List<SelectFriendModel> list;
    private View progressBar;
    private DatabaseReference databaseReferenceChats, databaseReferenceUsers;
    private FirebaseUser currentUser;
    private ValueEventListener valueEventListener;
    private String selMsg, selMsgId, selMsgType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        recyclerView = findViewById(R.id.rvSelectFriend);
        progressBar = findViewById(R.id.progressBarSelectFriend);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        list = new ArrayList<>();
        selectFriendAdapter = new SelectFriendAdapter(list, this);
        recyclerView.setAdapter(selectFriendAdapter);
        progressBar.setVisibility(View.VISIBLE);

        if(getIntent()!=null){
            selMsg = getIntent().getStringExtra("msg");
            selMsgId = getIntent().getStringExtra("msgId");
            selMsgType = getIntent().getStringExtra("msgType");
        }
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child("Chats").child(currentUser.getUid());
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren()){
                    String friendId = ds.getKey();

                    databaseReferenceUsers.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userName = snapshot.child("Name").getValue()!=null?snapshot.child("Name").getValue().toString():"";

                            SelectFriendModel selectFriendModel = new SelectFriendModel(friendId, userName, friendId+".jpg");
                            list.add(selectFriendModel);
                            selectFriendAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SelectFriendActivity.this, "Unable to fetch the friends list", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectFriendActivity.this, "Unable to fetch the friends list", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReferenceChats.addValueEventListener(valueEventListener);

    }

    public void returnSelectedFriend(String id, String name, String photo){
        databaseReferenceChats.removeEventListener(valueEventListener);
        Intent intent = new Intent();
        intent.putExtra(Extras.USER_ID, id);
        intent.putExtra(Extras.USER_NAME, name);
        intent.putExtra(Extras.USER_PHOTO, photo);

        intent.putExtra("msg", selMsg);
        intent.putExtra("msgId", selMsgId);
        intent.putExtra("msgType", selMsgType);

        setResult(RESULT_OK, intent);
        finish();
    }
}
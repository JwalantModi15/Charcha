package com.techinnovator.jmcharcha.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.chats.ChatListAdapter;
import com.techinnovator.jmcharcha.chats.ChatListModel;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ChatListModel> list;
    private List<String> userIds;
    private DatabaseReference databaseReferenceChats, databaseReferenceUsers, databaseReferenceMessages;
    private FirebaseUser currentUser;
    private View progressBar;
    private ChildEventListener childEventListener;
    private Query query;
    private TextView txt;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewChats);
        txt = view.findViewById(R.id.txtEmptyChats);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        list = new ArrayList<>();
        userIds = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(getContext(), list);
        recyclerView.setAdapter(chatListAdapter);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = view.findViewById(R.id.progressBarChats);

        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child("Chats").child(currentUser.getUid());
        databaseReferenceMessages = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUser.getUid());
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);
        txt.setVisibility(View.VISIBLE);

        query = databaseReferenceChats.orderByChild("timestamp");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot, true, snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot, false, snapshot.getKey());

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        query.addChildEventListener(childEventListener);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }

    public void updateList(DataSnapshot dataSnapshot, boolean isNew, String userId){
        progressBar.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);

        databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name, lastMes="", lastMesTime="", img, unReadCount="";
                    unReadCount = dataSnapshot.child("unread_count").getValue()!=null?dataSnapshot.child("unread_count").getValue().toString():"0";
                    lastMes = dataSnapshot.child("last_message").getValue()==null?"":dataSnapshot.child("last_message").getValue().toString();
                    lastMesTime = dataSnapshot.child("last_message_time").getValue()==null?"":dataSnapshot.child("last_message_time").getValue().toString();

                        name = snapshot.child("Name").getValue().toString();
                    img = snapshot.child("Photo").getValue().toString();

                    if(isNew){
                        list.add(new ChatListModel(userId, name, img, unReadCount, lastMes, lastMesTime));
                        userIds.add(userId);
                    }
                    else{
                        int index = userIds.indexOf(userId);
                        list.set(index, new ChatListModel(userId, name, img, unReadCount, lastMes, lastMesTime));
                    }
                    chatListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch chats", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
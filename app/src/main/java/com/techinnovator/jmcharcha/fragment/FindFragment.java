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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.findfriends.FindFriendAdapter;
import com.techinnovator.jmcharcha.findfriends.FindFriendModel;

import java.util.ArrayList;
import java.util.List;

public class FindFragment extends Fragment {

    private RecyclerView recyclerView;
    private FindFriendAdapter findFriendAdapter;
    private List<FindFriendModel> list;
    private TextView txt;

    private View progressBar;
    private DatabaseReference databaseReference, databaseReferenceFriendRequest;
    private FirebaseUser firebaseUser;

    public FindFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewFind);
        txt = view.findViewById(R.id.txtEmptyFindList);
        progressBar = view.findViewById(R.id.layoutProgressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        list = new ArrayList<>();
        findFriendAdapter = new FindFriendAdapter(getActivity(), list);
        recyclerView.setAdapter(findFriendAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceFriendRequest = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(firebaseUser.getUid());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);

        databaseReference.orderByChild("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    String userId = ds.getKey();

                    if(!userId.equals(firebaseUser.getUid())){
                        String name = ds.child("Name").getValue().toString();
                        String photo = ds.child("Photo").getValue().toString();

                        databaseReferenceFriendRequest.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String request_type = snapshot.child("request_type").getValue().toString();

                                    if(request_type.equals("sent")){
                                        list.add(new FindFriendModel(photo, name, userId, true));
                                        findFriendAdapter.notifyDataSetChanged();
                                    }
                                    else{
                                        list.add(new FindFriendModel(photo, name, userId, false));
                                        findFriendAdapter.notifyDataSetChanged();
                                    }
                                }
                                else{
                                    list.add(new FindFriendModel(photo, name, userId, false));
                                    findFriendAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                        progressBar.setVisibility(View.GONE);
                        txt.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}
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
import com.techinnovator.jmcharcha.requests.RequestsAdapter;
import com.techinnovator.jmcharcha.requests.RequestsModel;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {

    private TextView txt;
    private View progressBar;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private List<RequestsModel> list;
    private RequestsAdapter requestsAdapter;
    private DatabaseReference databaseReferenceUsers, databaseReferenceRequests;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewRequests);
        txt = view.findViewById(R.id.txtEmptyRequests);
        list = new ArrayList<>();
        progressBar = view.findViewById(R.id.friendRequestProgressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requestsAdapter = new RequestsAdapter(list, getContext(), new RequestsAdapter.OnClick() {
            @Override
            public void displayTxt() {
                txt.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.setAdapter(requestsAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        databaseReferenceRequests = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(currentUser.getUid());

        progressBar.setVisibility(View.VISIBLE);
        txt.setVisibility(View.VISIBLE);

        databaseReferenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressBar.setVisibility(View.GONE);

                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.exists()){
                        String requestType = ds.child("request_type").getValue().toString();

                        if(requestType.equals("received")){
                            String userId = ds.getKey();
                            databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String name = snapshot.child("Name").getValue().toString();
                                    String photo = "";

                                    if(snapshot.child("Photo").getValue()!=null){
                                        photo = snapshot.child("Photo").getValue().toString();
                                    }
                                    list.add(new RequestsModel(userId, photo, name));
                                    requestsAdapter.notifyDataSetChanged();
                                    txt.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), "Failed to fetch friend requests", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
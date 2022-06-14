package com.techinnovator.jmcharcha.findfriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.common.Util;

import java.util.List;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {

    private Context context;
    private List<FindFriendModel> list;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private String userId;

    public FindFriendAdapter(Context context, List<FindFriendModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout, parent, false);
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {

        FindFriendModel findFriendModel = list.get(position);
        holder.txtName.setText(findFriendModel.getName());

        if(!findFriendModel.getImg().equals("")){
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://charcha-e15d6.appspot.com").child("Images/"+findFriendModel.getUserId()+".jpg");

            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference.child(currentUser.getUid()).child(findFriendModel.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.child("request_type").getValue().toString();
                    if(status.equals("accepted")){
                        holder.btnSend.setVisibility(View.GONE);
                        holder.btnCancel.setVisibility(View.GONE);
                        holder.btnAccepted.setVisibility(View.VISIBLE);
                        holder.btnAccepted.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(findFriendModel.isRequestStatus()){
            holder.btnSend.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        holder.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.progressBar.setVisibility(View.VISIBLE);

                userId = findFriendModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReference.child(userId).child(currentUser.getUid()).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(context, "Request sent successfully!", Toast.LENGTH_SHORT).show();

                                        String title = "New friend request";
                                        String mes = "Friend request from "+currentUser.getDisplayName();
                                        Util.sendNotification(context, title, mes, userId);
                                        holder.btnSend.setVisibility(View.GONE);
                                        holder.btnCancel.setVisibility(View.VISIBLE);
                                        holder.progressBar.setVisibility(View.GONE);
                                    }
                                    else{
                                        Toast.makeText(context, "Failed to sent request!", Toast.LENGTH_SHORT).show();
                                        holder.progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(context, "Failed to sent request!", Toast.LENGTH_SHORT).show();
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.progressBar.setVisibility(View.VISIBLE);

                userId = findFriendModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child("request_type").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReference.child(userId).child(currentUser.getUid()).child("request_type").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(context, "Request cancel successfully!", Toast.LENGTH_SHORT).show();
                                        holder.btnSend.setVisibility(View.VISIBLE);
                                        holder.btnCancel.setVisibility(View.GONE);
                                        holder.progressBar.setVisibility(View.GONE);
                                    }
                                    else{
                                        Toast.makeText(context, "Failed to cancel request!", Toast.LENGTH_SHORT).show();
                                        holder.progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(context, "Failed to sent request!", Toast.LENGTH_SHORT).show();
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder{

        private TextView txtName;
        private Button btnSend, btnCancel, btnAccepted;
        private ImageView img;
        private ProgressBar progressBar;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgFFProfile);
            btnSend = itemView.findViewById(R.id.btnSendRequest);
            btnCancel = itemView.findViewById(R.id.btnCancelRequest);
            txtName = itemView.findViewById(R.id.txtUserName);
            progressBar = itemView.findViewById(R.id.progressBarFindFriends);
            btnAccepted = itemView.findViewById(R.id.btnAccepted);
        }
    }
}

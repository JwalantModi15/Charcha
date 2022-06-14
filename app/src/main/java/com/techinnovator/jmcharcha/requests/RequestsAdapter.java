package com.techinnovator.jmcharcha.requests;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.common.Util;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    List<RequestsModel> list;
    Context context;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReferenceRequests, databaseReferenceChats;
    OnClick onClick;

    public RequestsAdapter(List<RequestsModel> list, Context context, OnClick onClick) {
        this.list = list;
        this.context = context;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frient_request_layout, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestsModel requestsModel = list.get(position);
        holder.txtName.setText(requestsModel.getUserName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://charcha-e15d6.appspot.com").child("Images/"+requestsModel.getUserId()+".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.img);
            }
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceRequests = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child("Chats");
        String userId = requestsModel.getUserId();
        holder.btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.btnDeny.setVisibility(View.GONE);
                holder.btnAccept.setVisibility(View.GONE);

                databaseReferenceRequests.child(firebaseUser.getUid()).child(requestsModel.getUserId()).child("request_type").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReferenceRequests.child(requestsModel.getUserId()).child(firebaseUser.getUid()).child("request_type").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        list.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Deny request successfully", Toast.LENGTH_SHORT).show();

                                        String title = "Friend request denied";
                                        String mes = "Friend request denied by "+firebaseUser.getDisplayName();
                                        Util.sendNotification(context, title, mes, userId);
                                        holder.progressBar.setVisibility(View.GONE);
                                        if(list.size()==0){
                                            onClick.displayTxt();
                                        }
                                    }
                                    else{
                                        Toast.makeText(context, "Failed to deny request", Toast.LENGTH_SHORT).show();
                                        holder.progressBar.setVisibility(View.GONE);
                                        holder.btnDeny.setVisibility(View.VISIBLE);
                                        holder.btnAccept.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(context, "Failed to deny request", Toast.LENGTH_SHORT).show();
                            holder.progressBar.setVisibility(View.GONE);
                            holder.btnDeny.setVisibility(View.VISIBLE);
                            holder.btnAccept.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.btnDeny.setVisibility(View.GONE);
                holder.btnAccept.setVisibility(View.GONE);

                databaseReferenceChats.child(firebaseUser.getUid()).child(requestsModel.getUserId()).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            databaseReferenceChats.child(requestsModel.getUserId()).child(firebaseUser.getUid()).child("timestamp").setValue(ServerValue.TIMESTAMP)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        databaseReferenceRequests.child(firebaseUser.getUid()).child(requestsModel.getUserId()).child("request_type").setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    list.remove(position);
                                                    notifyDataSetChanged();
                                                    Toast.makeText(context, "Accept request successfully", Toast.LENGTH_SHORT).show();
                                                    String title = "Friend request accepted";
                                                    String mes = "Friend request accepted by "+firebaseUser.getDisplayName();
                                                    Util.sendNotification(context, title, mes, userId);

                                                    holder.progressBar.setVisibility(View.GONE);
                                                    if(list.size()==0){
                                                        onClick.displayTxt();
                                                    }
                                                }
                                                else{
                                                    Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show();
                                                    holder.progressBar.setVisibility(View.GONE);
                                                    holder.btnDeny.setVisibility(View.VISIBLE);
                                                    holder.btnAccept.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });
                                        databaseReferenceRequests.child(requestsModel.getUserId()).child(firebaseUser.getUid()).child("request_type").setValue("accepted");
                                    }
                                    else{
                                        Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show();
                                        holder.progressBar.setVisibility(View.GONE);
                                        holder.btnDeny.setVisibility(View.VISIBLE);
                                        holder.btnAccept.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show();
                            holder.progressBar.setVisibility(View.GONE);
                            holder.btnDeny.setVisibility(View.VISIBLE);
                            holder.btnAccept.setVisibility(View.VISIBLE);
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

    public class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView txtName;
        ProgressBar progressBar;
        Button btnAccept;
        Button btnDeny;
        ImageView img;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtFriendName);
            progressBar = itemView.findViewById(R.id.progressBarFriendRequests);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDeny = itemView.findViewById(R.id.btnDeny);
            img = itemView.findViewById(R.id.imgFRProfile);
        }
    }
    public interface OnClick {
        void displayTxt();
    }

}

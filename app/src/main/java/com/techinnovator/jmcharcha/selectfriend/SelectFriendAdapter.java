package com.techinnovator.jmcharcha.selectfriend;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techinnovator.jmcharcha.R;

import java.util.List;

public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.SelectFriendHolder>{

    private List<SelectFriendModel> list;
    private Context context;

    public SelectFriendAdapter(List<SelectFriendModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public SelectFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("s");
        View view = LayoutInflater.from(context).inflate(R.layout.select_friend_layout, parent, false);
        System.out.println("e");
        return new SelectFriendHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectFriendHolder holder, int position) {
        SelectFriendModel selectFriendModel = list.get(position);
        holder.txtName.setText(selectFriendModel.getUserName());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images").child(selectFriendModel.getUserPhoto());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).error(R.drawable.default_profile).placeholder(R.drawable.default_profile)
                        .into(holder.img);
            }
        });

        holder.llSelectFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context instanceof SelectFriendActivity){
                    ((SelectFriendActivity)context).returnSelectedFriend(selectFriendModel.getUserId(), selectFriendModel.getUserName(), selectFriendModel.getUserPhoto());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SelectFriendHolder extends RecyclerView.ViewHolder{

        TextView txtName;
        ImageView img;
        LinearLayout llSelectFriend;

        public SelectFriendHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtSelectedFriend);
            img = itemView.findViewById(R.id.imgSelectFriend);
            llSelectFriend = itemView.findViewById(R.id.llSelectFriend);
        }
    }
}

package com.techinnovator.jmcharcha.chats;

import android.content.Context;
import android.content.Intent;
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
import com.techinnovator.jmcharcha.ChatActivity;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.common.Extras;
import com.techinnovator.jmcharcha.common.Util;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListHolder>{

    private Context context;
    private List<ChatListModel> list;

    public ChatListAdapter(Context context, List<ChatListModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout, parent, false);
        return new ChatListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListHolder holder, int position) {
        ChatListModel chatListModel = list.get(position);
        holder.txtName.setText(chatListModel.getName());
//        holder.txtLastMes.setText(chatListModel.getLastMessage());
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://charcha-e15d6.appspot.com").child("Images").child(chatListModel.getUserId()+".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(holder.img);
            }
        });

        if(!chatListModel.getUnReadCount().equals("0")){
            holder.txtCount.setVisibility(View.VISIBLE);
            holder.txtCount.setText(chatListModel.getUnReadCount());
        }
        else{
            holder.txtCount.setVisibility(View.GONE);
        }

        holder.txtLastMes.setText(chatListModel.getLastMessage());
        String time = "";
        try{
            time = Util.getTimeAgo(Long.parseLong(chatListModel.getLastMessageTime()));
        }
        catch (Exception e){
            
        }
        holder.txtTime.setText(time);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Extras.USER_ID, chatListModel.getUserId());
                intent.putExtra(Extras.USER_NAME, chatListModel.getName());
                intent.putExtra(Extras.USER_PHOTO, chatListModel.getImg());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChatListHolder extends RecyclerView.ViewHolder{

        TextView txtName;
        TextView txtLastMes;
        TextView txtTime;
        ImageView img;
        TextView txtCount;
        LinearLayout linearLayout;

        public ChatListHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.llChatList);
            txtName = itemView.findViewById(R.id.txtChatUserName);
            txtLastMes = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            img = itemView.findViewById(R.id.imgCLProfile);
            txtCount = itemView.findViewById(R.id.txtCount);
        }
    }
}

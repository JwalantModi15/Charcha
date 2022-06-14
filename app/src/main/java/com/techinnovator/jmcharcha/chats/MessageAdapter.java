package com.techinnovator.jmcharcha.chats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.techinnovator.jmcharcha.ChatActivity;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.requests.RequestsAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder>{
    private Context context;
    private List<MessageModel> list;
    private View sent, receive;
    private ConstraintLayout vi;
    private ActionMode actionMode;

    public MessageAdapter(Context context, List<MessageModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        MessageModel message = list.get(position);

        String currUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fromUserId = message.getFrom();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa dd-MM-yyyy");
        String dateTime = simpleDateFormat.format(new Date(message.getTime()));
        String []arr = dateTime.split(" ");

        String messageTime = arr[0]+" "+arr[1];

        if(fromUserId.equals(currUserId)){
            if(message.getType().equals("text")){
                holder.lLSend.setVisibility(View.VISIBLE);
                holder.lLReceive.setVisibility(View.GONE);

                holder.llSentFile.setVisibility(View.GONE);
                holder.llReceiveFile.setVisibility(View.GONE);

                holder.txtSendMes.setText(message.getMessage());
                holder.txtSendMesTime.setText(messageTime);
            }
            else if(message.getType().equals("image")){
                holder.lLSend.setVisibility(View.GONE);
                holder.lLReceive.setVisibility(View.GONE);

                holder.llSentFile.setVisibility(View.VISIBLE);
                holder.llReceiveFile.setVisibility(View.GONE);

                holder.layoutPlayVideo.setVisibility(View.GONE);
                holder.receiverLayoutPlayVideo.setVisibility(View.GONE);

                holder.txtSentFileTime.setText(messageTime);
                Glide.with(context).load(message.getMessage()).placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                        .into(holder.imgSent);
            }
            else{
                holder.lLSend.setVisibility(View.GONE);
                holder.lLReceive.setVisibility(View.GONE);

                holder.llSentFile.setVisibility(View.VISIBLE);
                holder.llReceiveFile.setVisibility(View.GONE);

                holder.layoutPlayVideo.setVisibility(View.VISIBLE);
                holder.receiverLayoutPlayVideo.setVisibility(View.GONE);

                holder.txtSentFileTime.setText(messageTime);
                Glide.with(context).load(message.getMessage()).placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                        .into(holder.imgSent);
            }

        }
        else{
            if(message.getType().equals("text")) {
                holder.llSentFile.setVisibility(View.GONE);
                holder.llReceiveFile.setVisibility(View.GONE);

                holder.lLSend.setVisibility(View.GONE);
                holder.lLReceive.setVisibility(View.VISIBLE);
                holder.txtRecMes.setText(message.getMessage());
                holder.txtRecMesTime.setText(messageTime);
            }
            else if(message.getType().equals("image")){
                holder.lLSend.setVisibility(View.GONE);
                holder.lLReceive.setVisibility(View.GONE);

                holder.receiverLayoutPlayVideo.setVisibility(View.GONE);
                holder.layoutPlayVideo.setVisibility(View.GONE);

                holder.llSentFile.setVisibility(View.GONE);
                holder.llReceiveFile.setVisibility(View.VISIBLE);
                holder.txtReceiveFileTime.setText(messageTime);
                Glide.with(context).load(message.getMessage()).placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                        .into(holder.imgReceive);
            }
            else{
                holder.lLSend.setVisibility(View.GONE);
                holder.lLReceive.setVisibility(View.GONE);

                holder.receiverLayoutPlayVideo.setVisibility(View.VISIBLE);
                holder.layoutPlayVideo.setVisibility(View.GONE);

                holder.llSentFile.setVisibility(View.GONE);
                holder.llReceiveFile.setVisibility(View.VISIBLE);
                holder.txtReceiveFileTime.setText(messageTime);
                Glide.with(context).load(message.getMessage()).placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                        .into(holder.imgReceive);
            }
        }
        holder.constraintLayout.setTag(R.id.TAG_MESSAGE, message.getMessage());
        holder.constraintLayout.setTag(R.id.TAG_MESSAGE_ID, message.getMessageId());
        holder.constraintLayout.setTag(R.id.TAG_MESSAGE_TYPE, message.getType());

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = v.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri uri = Uri.parse(v.getTag(R.id.TAG_MESSAGE).toString());

                if(type.equals("video")){
                    holder.layoutPlayVideo.setVisibility(View.GONE);
                    holder.receiverLayoutPlayVideo.setVisibility(View.GONE);
                    sent = holder.layoutPlayVideo;
                    receive = holder.receiverLayoutPlayVideo;
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "video/mp4");
                    ((Activity)context).startActivityForResult(intent, 500);

                }
                else if(type.equals("image")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "image/jpg");
                    context.startActivity(intent);
                }

            }
        });

        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(actionMode!=null){
                    return false;
                }
                vi = holder.constraintLayout;
                actionMode = ((AppCompatActivity)context).startSupportActionMode(callback);
                holder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.highlight));
                System.out.println("colour");
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MessageHolder extends RecyclerView.ViewHolder{

        TextView txtSendMes;
        TextView txtSendMesTime;
        TextView txtRecMes, txtRecMesTime;
        LinearLayout lLSend, lLReceive, llSentFile, llReceiveFile;
        ImageView imgSent, imgReceive;
        TextView txtSentFileTime, txtReceiveFileTime;
        ConstraintLayout constraintLayout;
        View layoutPlayVideo, receiverLayoutPlayVideo;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            txtSendMes = itemView.findViewById(R.id.txtActualMes);
            txtSendMesTime = itemView.findViewById(R.id.txtMesTime);
            txtRecMes = itemView.findViewById(R.id.txtActualMesRec);
            txtRecMesTime = itemView.findViewById(R.id.txtRecMesTime);
            lLSend = itemView.findViewById(R.id.lLSend);
            lLReceive = itemView.findViewById(R.id.lLReceive);
            constraintLayout = itemView.findViewById(R.id.cLayout);

            llSentFile = itemView.findViewById(R.id.lLSentFile);
            llReceiveFile = itemView.findViewById(R.id.lLReceiveFile);
            imgSent = itemView.findViewById(R.id.imgSent);
            imgReceive = itemView.findViewById(R.id.imgReceive);
            txtSentFileTime = itemView.findViewById(R.id.txtImgSentMesTime);
            txtReceiveFileTime = itemView.findViewById(R.id.txtImgReceiveMesTime);
            layoutPlayVideo = itemView.findViewById(R.id.layoutPlayVideo);
            receiverLayoutPlayVideo = itemView.findViewById(R.id.receiverLayoutPlayVideo);
        }
    }
    public void onResult(){
        System.out.println("aaa");
        if(sent!=null){
            sent.setVisibility(View.VISIBLE);
        }
        if(receive!=null){
            receive.setVisibility(View.VISIBLE);
        }
    }

    public ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_chat_options, menu);
            String msgType = vi.getTag(R.id.TAG_MESSAGE_TYPE).toString();
            if(msgType.equals("text")){
                MenuItem menuItem = menu.findItem(R.id.downloadFile);
                menuItem.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            String msg = vi.getTag(R.id.TAG_MESSAGE).toString();
            String msgId = vi.getTag(R.id.TAG_MESSAGE_ID).toString();
            String msgType = vi.getTag(R.id.TAG_MESSAGE_TYPE).toString();
            switch (id){
                case R.id.deleteMsg:
//                    Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                    if(context instanceof ChatActivity){
                        ((ChatActivity)context).deleteMessage(msgId, msgType);
                    }
                    mode.finish();
                    break;
                case R.id.forwardMessage:
                    if(context instanceof ChatActivity){
                        ((ChatActivity)context).forwardMsg(msgId, msg, msgType);
                    }
                    mode.finish();
                    break;
                case R.id.shareMsg:
//                    Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
                    if(msgType.equals("text")){
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, msg);
                        intent.setType("text/plain");
                        context.startActivity(intent);
                    }
                    else{
                        if(context instanceof ChatActivity){
                            ((ChatActivity)context).downloadFile(msgId, msgType, true);
                        }
                    }
                    mode.finish();
                    break;
                case R.id.downloadFile:
                    if(context instanceof ChatActivity){
                        ((ChatActivity)context).downloadFile(msgId, msgType, false);
                    }
                    mode.finish();
                    break;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            vi.setBackgroundColor(context.getResources().getColor(R.color.bg_color));
        }
    };
}
interface onMsg {
    void onMsgClick(String msgId, String msgType);
}

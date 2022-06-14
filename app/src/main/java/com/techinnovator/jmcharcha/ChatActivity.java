package com.techinnovator.jmcharcha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techinnovator.jmcharcha.chats.MessageAdapter;
import com.techinnovator.jmcharcha.chats.MessageModel;
import com.techinnovator.jmcharcha.common.Extras;
import com.techinnovator.jmcharcha.common.Util;
import com.techinnovator.jmcharcha.requests.RequestsAdapter;
import com.techinnovator.jmcharcha.selectfriend.SelectFriendActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity{

    private ImageView imgAttach, imgSend, imgProfile, imgBackArrow;
    private EditText etMessage;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String currentUserId, chatUserId;
    private TextView txtUserName;

    private RecyclerView recyclerView;
    private List<MessageModel> list;
    private MessageAdapter messageAdapter;
    private DatabaseReference databaseReferenceMessages;
    private ChildEventListener childEventListener;
    private BottomSheetDialog bottomSheetDialog;
    private int requestCodeToPickImage = 100;
    private int requestCodeToPickVideo = 101;
    private int requestCodeToCaptureImage = 102;
    private int REQUEST_CODE_FORWARD_MSG = 60;
    private LinearLayout llProgress;
    private TextView txtUserStatus;

    private String type, id;
    boolean share;
    private String userName, imgLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("");
            ViewGroup actionBarLayout = (ViewGroup)getLayoutInflater().inflate(R.layout.custom_action_bar, null);

//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);

            actionBar.setElevation(0);
            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()|ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        imgAttach = findViewById(R.id.imgAttachment);
        imgSend = findViewById(R.id.imgSendMes);
        etMessage = findViewById(R.id.etMessage);
        llProgress = findViewById(R.id.llProgress);
        imgProfile = findViewById(R.id.imgProfileOnToolbar);
        txtUserName = findViewById(R.id.txtUserNameOnToolBar);
        imgBackArrow = findViewById(R.id.imgBackArrow);
        txtUserStatus = findViewById(R.id.userStatus);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getUid();

        recyclerView = findViewById(R.id.recyclerViewChats);
        list = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        if(getIntent()!=null){
            chatUserId = getIntent().getStringExtra(Extras.USER_ID);
            userName = getIntent().getStringExtra(Extras.USER_NAME);
            imgLink = getIntent().getStringExtra(Extras.USER_PHOTO);
        }

        txtUserName.setText(userName);
        if(!TextUtils.isEmpty(imgLink)){
            System.out.println(imgLink);
            String img[] = imgLink.split("/");
            StorageReference storageReferenceProfileImage = FirebaseStorage.getInstance().getReference().child("Images").child(img[img.length-1]);
            storageReferenceProfileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ChatActivity.this).load(uri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                            .into(imgProfile);
                }
            });
        }

        imgBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Util.isConnectionAvailable(ChatActivity.this)){
                    System.out.println("yes");
                    DatabaseReference reference = databaseReference.child("Messages").child(currentUserId).child(chatUserId).push();
                    String pushId = reference.getKey();
                    sendMessage(etMessage.getText().toString(), "text", pushId);
                }
                else {
                    Toast.makeText(ChatActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(getIntent().hasExtra("msg") && getIntent().hasExtra("msgId") && getIntent().hasExtra("msgType")){
            String message = getIntent().getStringExtra("msg");
            String messageId = getIntent().getStringExtra("msgId");
            String messageType = getIntent().getStringExtra("msgType");

            DatabaseReference push = databaseReference.child("Messages").child(currentUserId).child(chatUserId).push();
            String newMsgId = push.getKey();

            if(messageType.equals("text")){
                sendMessage(message, messageType, newMsgId);
            }
            else{
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                String folderName = messageType.equals("image")?Extras.messageImages:Extras.messageVideos;
                String oldFileName = messageType.equals("image")?messageId+".jpg":messageId+".mp4";
                String newFileName = messageType.equals("image")?newMsgId+".jpg":newMsgId+".mp4";

                StorageReference newFileRef = storageReference.child(folderName).child(newFileName);

                String localFilePath = getExternalFilesDir(null).getAbsolutePath()+"/"+oldFileName;
                File file = new File(localFilePath);

                storageReference.child(folderName).child(oldFileName).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        UploadTask uploadTask = newFileRef.putFile(Uri.fromFile(file));
                        uploadProgress(uploadTask, newFileRef, newMsgId, messageType);
                    }
                });
            }
        }

        loadMessage();

        databaseReference.child("Chats").child(currentUserId).child(chatUserId).child("unread_count").setValue("0");
        recyclerView.scrollToPosition(list.size()-1);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                curPage++;
//                loadMessage();
//            }
//        });

        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.chat_file_options, null);
        view.findViewById(R.id.lLCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, requestCodeToCaptureImage);
            }
        });
        view.findViewById(R.id.lLGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, requestCodeToPickImage);
            }
        });
        view.findViewById(R.id.lLVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, requestCodeToPickVideo);
            }
        });
        view.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(view);

        imgAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    if(bottomSheetDialog!=null) {
                        bottomSheetDialog.show();
                    }
                }
                else{
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(inputMethodManager!=null){
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        DatabaseReference databaseReferenceChatUser = FirebaseDatabase.getInstance().getReference().child("Users").child(chatUserId);

        databaseReferenceChatUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status="";
                if(snapshot.child("Online").getValue()!=null){
                    status = snapshot.child("Online").getValue().toString();
                    if(status.equals("true")){
                        txtUserStatus.setText("Online");
                    }
                    else{
                        txtUserStatus.setText("Offline");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                DatabaseReference currUserReference = databaseReference.child("Chats").child(currentUserId).child(chatUserId);
                if(s.toString().matches("")){
                    currUserReference.child("typing").setValue("0");
                }
                else{
                    currUserReference.child("typing").setValue("1");
                }
            }
        });
        DatabaseReference typingReference = databaseReference.child("Chats").child(chatUserId).child(currentUserId);
        typingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String typing="";
                if(snapshot.child("typing").getValue()!=null){
                    typing = snapshot.child("typing").getValue().toString();
                    if(typing.equals("1")){
                        txtUserStatus.setText("typing...");
                    }
                    else{
                        txtUserStatus.setText("Online");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        databaseReference.child("Chats").child(currentUserId).child(chatUserId).child("unread_count").setValue("0");
        super.onBackPressed();
    }

    public void forwardMsg(String msgId, String msg, String msgType){
        Intent intent = new Intent(ChatActivity.this, SelectFriendActivity.class);
        intent.putExtra("msg", msg);
        intent.putExtra("msgId", msgId);
        intent.putExtra("msgType", msgType);
        startActivityForResult(intent, REQUEST_CODE_FORWARD_MSG);
    }
    public void downloadFile(String msgId, String msgType, boolean isShare){
        if(ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            String folderName = msgType.equals("image")?Extras.messageImages:Extras.messageVideos;
            String fileName = msgType.equals("image")?msgId+".jpg":msgId+".mp4";

            StorageReference storageReferenceFile = storageReference.child(folderName).child(fileName);

            String localFilePath = getExternalFilesDir(null).getAbsolutePath()+"/"+fileName;
            File localFile = new File(localFilePath);

            try{
                if(localFile.exists() || localFile.createNewFile()){

                    FileDownloadTask downloadTask = storageReferenceFile.getFile(localFile);
                    View view = getLayoutInflater().inflate(R.layout.file_progress, null);
                    ProgressBar progressBar = view.findViewById(R.id.fileDownloadProgress);
                    TextView txtProgress = view.findViewById(R.id.txtProgress);
                    ImageView imgPlay = view.findViewById(R.id.imgPlay);
                    ImageView imgPause = view.findViewById(R.id.imgPause);
                    ImageView imgCancel = view.findViewById(R.id.imgCancel);

                    imgPause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadTask.pause();
                            imgPause.setVisibility(View.GONE);
                            imgPlay.setVisibility(View.VISIBLE);
                        }
                    });

                    imgPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadTask.resume();
                            imgPause.setVisibility(View.VISIBLE);
                            imgPlay.setVisibility(View.GONE);
                        }
                    });

                    imgCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadTask.cancel();
                        }
                    });

                    llProgress.addView(view);
                    txtProgress.setText("Downloading "+msgType+" (0%)");

                    downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                            double progress = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                            progressBar.setProgress((int)progress);
                            txtProgress.setText("Downloading "+msgType+" ("+progressBar.getProgress()+"%)");
                        }
                    });

                    downloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            llProgress.removeView(view);
                            if(task.isSuccessful()){
                                if(isShare){
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(localFilePath));
                                    if(msgType.equals("video")){
                                        intent.setType("video/mp4");
                                    }
                                    else{
                                        intent.setType("image/jpg");
                                    }
                                    startActivity(Intent.createChooser(intent, "Share with"));
                                }
                                else{
                                    Snackbar snackbar = Snackbar.make(llProgress, "File downloaded successfully", Snackbar.LENGTH_INDEFINITE)
                                            .setAction("View", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Uri uri = Uri.parse(localFilePath);
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    if(msgType.equals("video")){
                                                        intent.setDataAndType(uri, "video/mp4");
                                                    }
                                                    else if(msgType.equals("image")){
                                                        intent.setDataAndType(uri, "image/jpg");
                                                    }
                                                    startActivity(intent);
                                                }
                                            });
                                    snackbar.show();
                                }

                            }
                        }
                    });

                    downloadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            llProgress.removeView(view);
                            Toast.makeText(ChatActivity.this, "Failed to download!", Toast.LENGTH_SHORT).show();
                        }
                    });


                }else{
                    Toast.makeText(this, "Failed to store file", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e){

            }

        }
        else{
            type = msgType;
            id = msgId;
            share = isShare;
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }
    }
    public void deleteMessage(String messageId, String msgType){
        DatabaseReference databaseReferenceMsg = databaseReference.child("Messages").child(currentUserId).child(chatUserId)
                .child(messageId);
        databaseReferenceMsg.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            DatabaseReference df = databaseReference.child("Messages").child(chatUserId).child(currentUserId).child(messageId);
                            df.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        if(!msgType.equals("text")){
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                            String folderName = msgType.equals("image")?Extras.messageImages:Extras.messageVideos;
                                            String fileName = msgType.equals("image")?messageId+".jpg":messageId+".mp4";

                                            StorageReference fileRef = storageReference.child(folderName).child(fileName);
                                            fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(ChatActivity.this, "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(ChatActivity.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(ChatActivity.this, "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        Toast.makeText(ChatActivity.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(ChatActivity.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int itemId = item.getItemId();
//
//        switch (itemId){
//            case android.R.id.home:
//                finish();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==requestCodeToCaptureImage && resultCode==RESULT_OK){
            if(data!=null){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                uploadByteArray(byteArrayOutputStream, "image");
            }
        }
        else if(requestCode==requestCodeToPickImage && resultCode==RESULT_OK){
            if(data!=null){
                Uri imageUri = data.getData();
                uploadFile(imageUri, "image");
            }
        }
        else if(requestCode==requestCodeToPickVideo && resultCode==RESULT_OK){
            if(data!=null){
                Uri videoUri = data.getData();
                uploadFile(videoUri, "video");
            }
        }
        else if(requestCode==500 && resultCode==RESULT_OK){
            messageAdapter.onResult();
        }
        else if(requestCode==REQUEST_CODE_FORWARD_MSG && resultCode==RESULT_OK){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(Extras.USER_ID, data.getStringExtra(Extras.USER_ID));
            intent.putExtra(Extras.USER_NAME, data.getStringExtra(Extras.USER_NAME));
            intent.putExtra(Extras.USER_PHOTO, data.getStringExtra(Extras.USER_PHOTO));

            intent.putExtra("msg", data.getStringExtra("msg"));
            intent.putExtra("msgId", data.getStringExtra("msgId"));
            intent.putExtra("msgType", data.getStringExtra("msgType"));

            startActivity(intent);
            finish();
        }
    }

    private void uploadFile(Uri uri, String msgType){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserId).child(chatUserId).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = msgType.equals("image")?Extras.messageImages:Extras.messageVideos;
        String fileName = msgType.equals("image")?pushId+".jpg":pushId+".mp4";

        StorageReference storageReferenceFile = storageReference.child(folderName).child(fileName);
        UploadTask uploadTask = storageReferenceFile.putFile(uri);
        uploadProgress(uploadTask, storageReferenceFile, pushId, msgType);
    }

    private void uploadProgress(UploadTask uploadTask, StorageReference filePath, String pushId, String msgType){

        View view = getLayoutInflater().inflate(R.layout.file_progress, null);
        ProgressBar progressBar = view.findViewById(R.id.fileDownloadProgress);
        TextView txtProgress = view.findViewById(R.id.txtProgress);
        ImageView imgPlay = view.findViewById(R.id.imgPlay);
        ImageView imgPause = view.findViewById(R.id.imgPause);
        ImageView imgCancel = view.findViewById(R.id.imgCancel);

        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTask.pause();
                imgPause.setVisibility(View.GONE);
                imgPlay.setVisibility(View.VISIBLE);
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTask.resume();
                imgPause.setVisibility(View.VISIBLE);
                imgPlay.setVisibility(View.GONE);
            }
        });

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTask.cancel();
            }
        });

        llProgress.addView(view);
        txtProgress.setText("Upload "+msgType+" (0%)");

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressBar.setProgress((int)progress);
                txtProgress.setText("Upload "+msgType+" ("+progressBar.getProgress()+"%)");
            }
        });

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                llProgress.removeView(view);
                if(task.isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUri = uri.toString();
                            sendMessage(downloadUri, msgType, pushId);
                        }
                    });
                }
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                llProgress.removeView(view);
                Toast.makeText(ChatActivity.this, "Failed to upload!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadByteArray(ByteArrayOutputStream byteArrayOutputStream, String msgType){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserId).child(chatUserId).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = msgType.equals("image")?Extras.messageImages:Extras.messageVideos;
        String fileName = msgType.equals("image")?pushId+".jpg":pushId+".mp4";

        StorageReference storageReferenceFile = storageReference.child(folderName).child(fileName);
        UploadTask uploadTask = storageReferenceFile.putBytes(byteArrayOutputStream.toByteArray());

        uploadProgress(uploadTask, storageReferenceFile, pushId, msgType);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==10 && grantResults.length>0 && grantResults[0]==1){
            if(bottomSheetDialog!=null) {
                bottomSheetDialog.show();
            }
        }
        else if(requestCode==50 && grantResults.length>0 && grantResults[0]==1){
            downloadFile(id, type, share);
        }
        else{
            Toast.makeText(this, "Permission required to access files", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessage(){
        list.clear();
        databaseReferenceMessages = databaseReference.child("Messages").child(currentUserId).child(chatUserId);
//        Query messageQuery = databaseReferenceMessages.limitToLast(curPage * recordPerPage);

        if(childEventListener!=null){
            databaseReferenceMessages.removeEventListener(childEventListener);
        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel messageModel = snapshot.getValue(MessageModel.class);
                list.add(messageModel);
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(list.size()-1);
//                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                loadMessage();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                swipeRefreshLayout.setRefreshing(false);
            }
        };
        databaseReferenceMessages.addChildEventListener(childEventListener);
    }

    private void sendMessage(String mes, String mesType, String pushId){
        try{

            if(!mes.equals("")){
                HashMap map = new HashMap<>();
                map.put("messageId", pushId);
                map.put("message", mes);
                map.put("from", currentUserId);
                map.put("time", ServerValue.TIMESTAMP);
                map.put("type", mesType);

                String currUserRef = "Messages/"+currentUserId+"/"+chatUserId;
                String chatUserRef = "Messages/"+chatUserId+"/"+currentUserId;

                HashMap currUserMap = new HashMap<>();
                currUserMap.put(currUserRef+"/"+pushId, map);
                currUserMap.put(chatUserRef+"/"+pushId, map);

                databaseReference.updateChildren(currUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error!=null){
                            Toast.makeText(ChatActivity.this, "Failed to sent message!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            etMessage.setText("");
                            Toast.makeText(ChatActivity.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
                            String lastMsg;
                            String title="";
                            if(mesType.equals("text")){
                                lastMsg = mes;
                                title = "New Message";
                            }
                            else if(mesType.equals("image")){
                                lastMsg = "New Image";
                                title = "New Image";
                            }
                            else{
                                lastMsg = "New Video";
                                title = "New Video";
                            }
                            Util.sendNotification(ChatActivity.this, title, mes, chatUserId);
                            Util.updateChatDetails(ChatActivity.this, currentUserId, chatUserId, lastMsg);
                        }
                    }
                });

            }
        }
        catch (Exception e){

        }
    }
}
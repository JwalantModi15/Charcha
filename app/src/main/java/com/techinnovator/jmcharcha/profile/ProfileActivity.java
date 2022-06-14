package com.techinnovator.jmcharcha.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techinnovator.jmcharcha.MainActivity;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.SignUpActivity;
import com.techinnovator.jmcharcha.login.LoginActivity;
import com.techinnovator.jmcharcha.password.ChangePasswordActivity;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etSEmail;
    private TextInputEditText etCName;
    private Button btnSave;
    private Button btnLogout;
    private ImageView imgCProfile;
    private String email;
    private String name;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri localFileUri;
    private Uri serverFileUri;
    private TextView txtChangePassword;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etSEmail = findViewById(R.id.etSEmail);
        etCName = findViewById(R.id.etCName);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        imgCProfile = findViewById(R.id.imgCProfile);
        txtChangePassword = findViewById(R.id.txtChangePassword);
        progressBar = findViewById(R.id.progressBar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        imgCProfile.setImageResource(R.drawable.default_profile);
        if(firebaseUser!=null){
            etCName.setText(firebaseUser.getDisplayName());
            etSEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if(serverFileUri!=null){
                Glide.with(this).load(serverFileUri).placeholder(R.drawable.default_profile).error(R.drawable.default_profile).into(imgCProfile);
            }
        }

        email = etSEmail.getText().toString().trim();

        etSEmail.setEnabled(false);

        imgCProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(serverFileUri==null){
                    if(ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 1);
                    }
                    else{
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                    }
                }
                else{
                    PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, v);

                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            if(item.getItemId()==R.id.changePic){
                                if(ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent, 1);
                                }
                                else{
                                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                                }
                            }
                            else if(item.getItemId()==R.id.removePic){
                                progressBar.setVisibility(View.VISIBLE);
                                serverFileUri = null;
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name)
                                        .setPhotoUri(serverFileUri).build();
                                firebaseUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if(task.isSuccessful()){
                                            imgCProfile.setImageResource(R.drawable.default_profile);
                                        }
                                    }
                                });
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etCName.getText().toString().trim();
                if(name.equals("")){
                    etCName.setError("Enter Name");
                }
                else{
                    if(localFileUri!=null){
                        storeUserPhotoInDatabase();
                    }
                    else{
                        storeUserInDatabase();
                    }
                }

            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference databaseReference = rootRef.child("Tokens").child(firebaseUser.getUid());

                databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            firebaseAuth.signOut();
                            Toast.makeText(ProfileActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
            }
        });


    }
    public void storeUserPhotoInDatabase(){
        String fileName = firebaseUser.getUid()+".jpg";

        StorageReference fileReference = storageReference.child("Images/"+fileName);
        progressBar.setVisibility(View.VISIBLE);
        fileReference.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverFileUri = uri;

                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name)
                                    .setPhotoUri(serverFileUri).build();

                            firebaseUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        String UID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

                                        HashMap<String, String> map = new HashMap();
                                        map.put("Email", email);
                                        map.put("Name", name);
                                        map.put("Status", "true");
                                        map.put("Photo", serverFileUri.getPath());

                                        databaseReference.child(UID).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBar.setVisibility(View.GONE);
                                                if(task.isSuccessful()){
                                                    Toast.makeText(ProfileActivity.this, "Changes made Successfully", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Toast.makeText(ProfileActivity.this, "Unable made changes", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    public void storeUserInDatabase(){

        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        progressBar.setVisibility(View.VISIBLE);
        firebaseUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    String UID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

                    HashMap<String, String> map = new HashMap();
                    map.put("Email", email);
                    map.put("Name", name);
                    map.put("Status", "true");
                    if(serverFileUri!=null){
                        map.put("Photo", serverFileUri.getPath());
                    }
                    else{
                        map.put("Photo", "");
                    }
                    databaseReference.child(UID).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, "Name changed successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Unable to change name", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==2 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        }
        else{
            Toast.makeText(ProfileActivity.this, "Access permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data.getData()!=null){
            localFileUri = data.getData();
            imgCProfile.setImageURI(localFileUri);
        }
    }
}
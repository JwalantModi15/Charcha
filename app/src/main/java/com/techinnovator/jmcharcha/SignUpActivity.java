package com.techinnovator.jmcharcha;

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
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techinnovator.jmcharcha.login.LoginActivity;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etSEmail;
    private TextInputEditText etSPassword;
    private TextInputEditText etName;
    private TextInputEditText etConfirmPass;
    private Button btnSignUp;
    private ImageView imgProfile;
    private String email;
    private String password;
    private String name;
    private String confirmPassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri localFileUri;
    private Uri serverFileUri;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etSEmail = findViewById(R.id.etSEmail);
        etSPassword = findViewById(R.id.etSPassword);
        etName = findViewById(R.id.etName);
        etConfirmPass = findViewById(R.id.etConfirmPass);

        btnSignUp = findViewById(R.id.btnSignUp);
        imgProfile = findViewById(R.id.imgProfile);

        progressBar = findViewById(R.id.progressBar);

        storageReference = FirebaseStorage.getInstance().getReference();
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);
                }
                else{
                    ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                }

            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = etSEmail.getText().toString().trim();
                password = etSPassword.getText().toString().trim();
                name = etName.getText().toString().trim();
                confirmPassword = etConfirmPass.getText().toString().trim();

                if(email.equals("")){
                    etSEmail.setError("Enter Email");
                }
                else if(password.equals("")){
                    etSPassword.setError("Enter Password");
                }
                else if(name.equals("")){
                    etName.setError("Enter Your Name");
                }
                else if(confirmPassword.equals("")){
                    etConfirmPass.setError("Confirm Password");
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    etSEmail.setError("Enter Correct Email");
                }
                else if(!password.equals(confirmPassword)){
                    etConfirmPass.setError("Password Mismatch");
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                firebaseUser = firebaseAuth.getCurrentUser();
                                if(localFileUri!=null){
                                    storeUserPhotoInDatabase();
                                }
                                else{
                                    storeUserInDatabase();
                                }
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "Failed to Sign Up due to: "+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        }
        else{
            Toast.makeText(SignUpActivity.this, "Access permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data.getData()!=null){
            localFileUri = data.getData();
            imgProfile.setImageURI(localFileUri);
        }
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
                                        map.put("Name", name);
                                        map.put("Email", email);
                                        map.put("Online", "true");
                                        map.put("Photo", serverFileUri.getPath());

                                        databaseReference.child(UID).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBar.setVisibility(View.GONE);
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SignUpActivity.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
                                                    firebaseAuth.signOut();
                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                    finish();
                                                }
                                                else{

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
                    map.put("Name", name);
                    map.put("Email", email);
                    map.put("Online", "true");
                    map.put("Photo", "");

                    databaseReference.child(UID).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                finish();
                            }
                            else{

                            }
                        }
                    });
                }
            }
        });

    }
}
package com.techinnovator.jmcharcha.password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.techinnovator.jmcharcha.R;
import com.techinnovator.jmcharcha.profile.ProfileActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etChangePassword, etConfirmChangePass;
    private String password, confirmPass;
    private FirebaseUser firebaseUser;
    private Button btnChangePassword;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etChangePassword = findViewById(R.id.etChangePassword);
        etConfirmChangePass = findViewById(R.id.etConfirmChangePass);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = etChangePassword.getText().toString().trim();
                confirmPass = etConfirmChangePass.getText().toString().trim();

                if(password.equals("")){
                    etChangePassword.setError("Enter Password");
                }
                else if(confirmPass.equals("")){
                    etConfirmChangePass.setError("Confirm Password");
                }
                else if(!password.equals(confirmPass)){
                    etConfirmChangePass.setError("Password Mismatched");
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(firebaseUser!=null){
                        firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                if(task.isSuccessful()){
                                    Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                else{
                                    Toast.makeText(ChangePasswordActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                }
            }
        });

    }
}
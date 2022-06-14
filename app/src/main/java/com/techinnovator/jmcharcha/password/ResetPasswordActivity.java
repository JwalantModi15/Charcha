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
import com.techinnovator.jmcharcha.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etResetEmail;
    private Button btnResetPassword;
    private String email;
    private FirebaseAuth firebaseAuth;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etResetEmail = findViewById(R.id.etResetEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etResetEmail.getText().toString().trim();
                if(email.equals("")){
                    etResetEmail.setError("Enter Email");
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "Password is sent to email: "+email, Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else{
                                Toast.makeText(ResetPasswordActivity.this, "Some went wrong, please try again!", Toast.LENGTH_LONG).show();
                                etResetEmail.setText("");
                            }
                        }
                    });
                }

            }
        });
    }
}
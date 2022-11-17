package com.moutamid.friendsmeetingtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.moutamid.friendsmeetingtracker.Constants.Constants;
import com.moutamid.friendsmeetingtracker.databinding.ActivityLoginScreenBinding;

public class LoginScreen extends AppCompatActivity {

    FirebaseUser firebaseUser;
    private ActivityLoginScreenBinding b;
    private String email,password = "";
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInfo()) {
                    pd = new ProgressDialog(LoginScreen.this);
                    pd.setMessage("Login....");
                    pd.show();
                    loginUser();
                }

            }
        });

        b.newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this,RegistrationScreen.class));
                finish();
            }
        });

    }

    private void loginUser() {
        Constants.auth().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            firebaseUser = Constants.auth().getCurrentUser();
                            sendActivityToDashboard();
                            pd.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendActivityToDashboard() {
        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Validate Input Fields
    public boolean validInfo() {

        email = b.emailInput.getText().toString();
        password = b.passInput.getText().toString();

        if (email.isEmpty()) {
            b.emailInput.setError("Input email!");
            b.emailInput.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            b.emailInput.setError("Please input valid email!");
            b.emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            b.passInput.setError("Input password!");
            b.passInput.requestFocus();
            return false;
        }

        return true;
    }



    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = Constants.auth().getCurrentUser();

        if (firebaseUser != null) {
            startActivity(new Intent(LoginScreen.this, MainActivity.class));
            finish();
        }
    }
}
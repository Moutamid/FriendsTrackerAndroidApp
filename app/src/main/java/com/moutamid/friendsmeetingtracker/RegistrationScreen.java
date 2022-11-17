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
import com.google.firebase.database.DatabaseReference;
import com.moutamid.friendsmeetingtracker.Constants.Constants;
import com.moutamid.friendsmeetingtracker.Model.User;
import com.moutamid.friendsmeetingtracker.databinding.ActivityLoginScreenBinding;
import com.moutamid.friendsmeetingtracker.databinding.ActivityRegistrationScreenBinding;

public class RegistrationScreen extends AppCompatActivity {

    private ActivityRegistrationScreenBinding b;
    private DatabaseReference db;
    private String fname,email,password,cpassword = "";
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegistrationScreenBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        db = Constants.databaseReference().child("Users");
        b.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInfo()) {
                    pd = new ProgressDialog(RegistrationScreen.this);
                    pd.setMessage("Creating Account....");
                    pd.show();
                    registerUser();
                }
            }
        });

    }

    private void registerUser() {
        Constants.auth().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = Constants.auth().getCurrentUser();

                            User model = new User(firebaseUser.getUid(),fname,email,password,"",0.0,0.0);
                            db.child(firebaseUser.getUid()).setValue(model);
                            sendActivityToDashboard();
                            pd.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegistrationScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sendActivityToDashboard() {
        Intent intent = new Intent(RegistrationScreen.this, ProfileImage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Validate Input Fields
    public boolean validInfo() {
        fname = b.userInput.getText().toString();
        email = b.emailInput.getText().toString();
        password = b.passInput.getText().toString();
        cpassword = b.cpassInput.getText().toString();

        if (fname.isEmpty()) {
            b.userInput.setError("Input Fullname!");
            b.userInput.requestFocus();
            return false;
        }

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

        if (cpassword.isEmpty()) {
            b.cpassInput.setError("Input Password!");
            b.cpassInput.requestFocus();
            return false;
        }

        if (!password.equals(cpassword)) {
            Toast.makeText(RegistrationScreen.this, "Password are not matched!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
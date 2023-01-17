package com.example.comptutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    private FirebaseAuth mauth;
    private EditText mail,password,lastName,firstName,studentLRN;
    private Button registerBtn;
    private TextView existingUserText;
    String userID;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mauth=FirebaseAuth.getInstance();
        lastName=findViewById(R.id.regLName);
        firstName = findViewById(R.id.regFName);
        mail=findViewById(R.id.registerEmail);
        password=findViewById(R.id.registerPassword);
        studentLRN = findViewById(R.id.regStudentLRN);
        registerBtn=findViewById(R.id.registerBtn);
        existingUserText=findViewById(R.id.existingUserText);
        db =FirebaseFirestore.getInstance();
        mauth = FirebaseAuth.getInstance();


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegister();
            }
        });
        existingUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this,Login.class));
            }
        });

    }

    private void openRegister() {
        String lName = lastName.getText().toString().trim();
        String fName = firstName.getText().toString().trim();
        String studLRN = studentLRN.getText().toString().trim();
        String users = mail.getText().toString().trim();
        String pass = password.getText().toString().trim();
        if(lName.isEmpty()) {
            lastName.setError("Last Name Can't be empty");
        }else if (fName.isEmpty())
        {
            firstName.setError("First Name Can't be empty");
        }
        else if (studLRN.isEmpty())
        {
            studentLRN.setError("LRN Can't be empty");
        }
        else if (users.isEmpty())
        {
            mail.setError("Email Can't be empty");
        }else if(pass.isEmpty())
        {
            password.setError("Password Can't be empty");

        }
        else
        {
            mauth.createUserWithEmailAndPassword(users,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        FirebaseUser currUser = mauth.getCurrentUser();
                        currUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Register.this,"Verification link had been sent to your email",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure:verification failed to sent"+ e);
                            }
                        });
                        Toast.makeText(Register.this,"Registered Successfully",Toast.LENGTH_LONG).show();
                        userID = mauth.getCurrentUser().getUid();
                        DocumentReference documentReference = db.collection("users").document(userID);
                        Map<String,Object> user = new HashMap<>();
                        user.put("lastName",lName);
                        user.put("firstName",fName);
                        user.put("lrn",studLRN);
                        user.put("email",users);
                        user.put("password",pass);
                        user.put("isStudent","1");
                        documentReference.set(user);
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess:user credentials has been saved on DataBase"+userID);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure:"+ e);
                            }
                        });
                        startActivity(new Intent(Register.this,Login.class));
                    }
                    else{
                        Toast.makeText(Register.this,"Register Failed, Please Try Again "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }
}
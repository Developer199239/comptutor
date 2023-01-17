package com.example.comptutor;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private FirebaseAuth mauth;
    private FirebaseFirestore mStore;
    private EditText mail,password;
    private Button loginBtn;
    private TextView registerText,forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        startActivity(new Intent(getApplicationContext(),admindashboard.class));

        mauth=FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mail=findViewById(R.id.loginEmail);
        password=findViewById(R.id.loginPassword);
        loginBtn=findViewById(R.id.loginBtn);
        registerText=findViewById(R.id.regTxt);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                login();

            }
        });
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,Register.class));
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetEmail = new EditText(view.getContext());
                AlertDialog.Builder passwordReset = new AlertDialog.Builder(view.getContext());
                passwordReset.setTitle("Reset Password?");
                passwordReset.setMessage("Enter Your Email For The Password Reset Link");
                passwordReset.setView(resetEmail);
                passwordReset.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = resetEmail.getText().toString();
                        mauth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this,"Reset Link Has been sent to your Email",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception error) {
                                Toast.makeText(Login.this,"Error Reset Link is Not Sent Please try Again"+error.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                passwordReset.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                passwordReset.create().show();

            }
        });

    }
    private void login() {
        String user = mail.getText().toString().trim();
        String pass = password.getText().toString().trim();
        if (user.isEmpty()) {
            mail.setError("Email Can't be empty");
        } else if (pass.isEmpty()) {
            password.setError("Password Can't be empty");
        } else {
            mauth.signInWithEmailAndPassword(user, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser currentUser = mauth.getCurrentUser();
                    if (currentUser != null && !currentUser.isEmailVerified())
                    {
                        Intent intent = new Intent(Login.this,Reverify.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(Login.this,"Login Successfully",Toast.LENGTH_SHORT).show();
                        checkUserLevel(authResult.getUser().getUid());
                    }
                }
            });
        }
    }

    private void checkUserLevel(String uid) {
        DocumentReference documentReference = mStore.collection("users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG","OnSuccess" + documentSnapshot.getData());
                if(documentSnapshot.getString("isTeacher")!=null)
                {
                    startActivity(new Intent(getApplicationContext(),admindashboard.class));
                }
                else if (documentSnapshot.getString("isStudent")!= null)
                {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
            }
        });
    }
}
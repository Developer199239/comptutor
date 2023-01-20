package com.example.comptutor;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.lang.String;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.example.comptutor.utils.BaseActivity;
import com.example.comptutor.utils.ComptutorApplication;
import com.example.comptutor.utils.SessionHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.FileOutputStream;
import java.io.IOException;

import kotlin.math.UMathKt;

public class MainActivity extends BaseActivity {
    FirebaseAuth mauth;
    FirebaseFirestore fdb;
    String userID;
    StorageReference storageRef;
    private Button logoutBtn;
    private TextView userlastName,userGivenName,studLrn,userEmail;
    ImageView userAvatar;
    private ImageButton guideButton, hardwareButton, simulationButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        mauth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        fdb = FirebaseFirestore.getInstance();
        FirebaseUser user = mauth.getCurrentUser();
        logoutBtn=findViewById(R.id.logoutBtn);
        userEmail=findViewById(R.id.userEmail);
        userlastName=findViewById(R.id.contlastName);
        userGivenName=findViewById(R.id.contgivenName);
        studLrn = findViewById(R.id.contstudentLRN);


        userAvatar=findViewById(R.id.userAvatar);
        hardwareButton = findViewById(R.id.hardwareBtn);
        hardwareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent openHardwarePage = new Intent(MainActivity.this,hardwarePage.class);
               startActivity(openHardwarePage);
            }
        });
        guideButton = findViewById(R.id.guideBtn);
        guideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGuide = new Intent(MainActivity.this,videoTutorialPage.class);
                startActivity(openGuide);
            }
        });
        simulationButton = findViewById(R.id.simulationBtn);
        simulationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openSimunation= new Intent(MainActivity.this,pcAssemblyGuide.class);
                startActivity(openSimunation);
            }
        });
        if(mauth.getCurrentUser() != null){
           userID= mauth.getCurrentUser().getUid();
            DocumentReference documentReference = fdb.collection("users").document(userID);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                    userlastName.setText(value.getString("lastName"));
                    userGivenName.setText(value.getString("firstName"));
                    studLrn.setText(value.getString("lrn"));
                    userEmail.setText(value.getString("email"));
                }
            });
        }
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseMessaging.getInstance().deleteToken();
                mauth.signOut();
                Intent openLogin = new Intent(MainActivity.this,Login.class);
                startActivity(openLogin);
            }
        });
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallery,1000);
            }
        });
        if (user != null)
        {
            StorageReference profileRef = storageRef.child("users/"+ mauth.getCurrentUser().getUid()+"/.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(userAvatar);
                }
            });
        }else
        {
            Intent openLogin = new Intent(MainActivity.this,Login.class);
            startActivity(openLogin);
        }

        if(new SessionHelper(getApplication()).getStringValue(SessionHelper.FIREBASE_TOKEN).isEmpty()) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ComptutorApplication.Companion.uploadTokenInServer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                uploadImageToDB(imageUri);
            }
        }
    }
    public void uploadImageToDB(Uri imageUri) {
        StorageReference fileRef = storageRef.child("users/" + mauth.getCurrentUser().getUid() + "/.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                       Glide.with(MainActivity.this).load(uri).centerCrop().into(userAvatar);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error In Uploading.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mauth.getCurrentUser();
        if (user == null)
        {
            Intent openLogin=new Intent(MainActivity.this,Login.class);
            startActivity(openLogin);
        }
    }
}
package com.example.comptutor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.String;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.example.comptutor.utils.AppConstants;
import com.example.comptutor.utils.AssignStudentModel;
import com.example.comptutor.utils.AssignStudentResultSet;
import com.example.comptutor.utils.BaseActivity;
import com.example.comptutor.utils.ComptutorApplication;
import com.example.comptutor.utils.MaterialProgress;
import com.example.comptutor.utils.PushInfoModel;
import com.example.comptutor.utils.PushNotificationResultSet;
import com.example.comptutor.utils.SessionHelper;
import com.example.comptutor.utils.StudentModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.push.PNPushRemoveChannelResult;
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {
    FirebaseAuth mauth;
    FirebaseFirestore fdb;
    String userID;
    StorageReference storageRef;
    private Button logoutBtn;
    private TextView userlastName,userGivenName,studLrn,userEmail, tvVideoAccessPermission;
    ImageView userAvatar;
    private ImageButton guideButton, hardwareButton, simulationButton;
    private MaterialProgress materialProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        materialProgress = new MaterialProgress(this);
        mauth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        fdb = FirebaseFirestore.getInstance();
        FirebaseUser user = mauth.getCurrentUser();
        logoutBtn=findViewById(R.id.logoutBtn);
        userEmail=findViewById(R.id.userEmail);
        userlastName=findViewById(R.id.contlastName);
        userGivenName=findViewById(R.id.contgivenName);
        studLrn = findViewById(R.id.contstudentLRN);
        tvVideoAccessPermission = findViewById(R.id.tvVideoAccessPermission);


        userAvatar=findViewById(R.id.userAvatar);
        hardwareButton = findViewById(R.id.hardwareBtn);
        hardwareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVideoAccessPermission("hardwareButton");
            }
        });
        guideButton = findViewById(R.id.guideBtn);
        guideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVideoAccessPermission("guideButton");
            }
        });
        simulationButton = findViewById(R.id.simulationBtn);
        simulationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkVideoAccessPermission("simulationButton");
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
        findViewById(R.id.ivLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = sessionHelper.getStringValue(SessionHelper.FIREBASE_TOKEN);
                ComptutorApplication.Companion.getPubnub().removePushNotificationsFromChannels()
                        .pushType(PNPushType.FCM)
                        .deviceId(token)
                        .channels(Arrays.asList(AppConstants.PUB_SUB_CHANNEL))
                        .async(new PNCallback<PNPushRemoveChannelResult>() {
                            @Override
                            public void onResponse(PNPushRemoveChannelResult result, PNStatus status) {
                                FirebaseMessaging.getInstance().deleteToken();
//                                mauth.signOut();
                                sessionHelper.clearSession();
                                Intent openLogin = new Intent(MainActivity.this,Login.class);
                                startActivity(openLogin);
                            }
                        });
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

        findViewById(R.id.ivCodeGenerator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchUserList(false);
            }
        });

        tvVideoAccessPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchUserList(true);
            }
        });
    }

    private void fetchUserList(boolean reqForVideoAccess){
        materialProgress.show();
        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
        mFireStore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> pnTokenList = new ArrayList<>();
                    StudentModel teacherModel = null;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        StudentModel studentModel = document.toObject(StudentModel.class);
                        if(studentModel.getRole().equals(AppConstants.ROLE_STUDENT)) {
                            pnTokenList.add(studentModel.getToken());
                        } else if(studentModel.getRole().equals(AppConstants.ROLE_TEACHER)){
                            teacherModel = studentModel;
                        }
                    }
                    if(teacherModel == null) {
                        materialProgress.dismiss();
                        Toast.makeText(MainActivity.this,"Teacher not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    pnTokenList.add(sessionHelper.getLoginInfo().getToken());
                    sendCodeRequestPush(pnTokenList, teacherModel, reqForVideoAccess);
                } else {
                    materialProgress.dismiss();
                    Toast.makeText(MainActivity.this, "Failed to load user list",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendCodeRequestPush(ArrayList<String> pnTokenList, StudentModel teacherModel, boolean reqForVideoAccess){
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();
        StudentModel loggedInUserModel = sessionHelper.getLoginInfo();
        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();
        Map<String, Object> payload = new HashMap<>();
        payload.put("pn_exceptions", pnTokenList);
        fcmPayload.setCustom(payload);
        PushPayloadHelper.FCMPayload.Notification fcmNotification =
                new PushPayloadHelper.FCMPayload.Notification()
                        .setTitle("Request")
                        .setBody(loggedInUserModel.getFirstName()+" Request for code");
        if(reqForVideoAccess) {
            fcmNotification =
                    new PushPayloadHelper.FCMPayload.Notification()
                            .setTitle("Request")
                            .setBody(loggedInUserModel.getFirstName()+" Request for Video Access");
        }
        fcmPayload.setNotification(fcmNotification);
        Map<String, Object> data = new HashMap<>();
        PushInfoModel pushInfoModel = new PushInfoModel(AppConstants.PUSH_TYPE_REQUEST_CODE, new Gson().toJson(loggedInUserModel),"");
        if(reqForVideoAccess) {
            pushInfoModel.setPushType(AppConstants.PUSH_TYPE_ACCESS_PERMISSION);
        }
        data.put("data",pushInfoModel);
        fcmPayload.setData(data);
        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> commonPayload = new HashMap<>();
        commonPayload.put("text", "Request");
        if(reqForVideoAccess) {
            commonPayload.put("text", loggedInUserModel.getFirstName()+" Request for video");
        } else {
            commonPayload.put("text", loggedInUserModel.getFirstName()+" Request for code");
        }
        pushPayloadHelper.setCommonPayload(commonPayload);

        Map<String, Object> pushPayload = pushPayloadHelper.build();
        ComptutorApplication.Companion.getPubnub().publish()
                .channel(AppConstants.PUB_SUB_CHANNEL)
                .message(pushPayload)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        Log.d("PUBNUB", "-->PNStatus.getStatusCode = " + status.getStatusCode());
                        if(status.getStatusCode() == 200) {
                            getNotification(pushInfoModel,teacherModel);
                        } else {
                            materialProgress.dismiss();
                            Toast.makeText(MainActivity.this, "Failed to send push notification", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void getNotification(PushInfoModel pushInfoModel, StudentModel teacherModel){
        pushInfoModel.setNotificationId(sessionHelper.getLoginInfo().getUserId()+""+System.currentTimeMillis());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(AppConstants.NOTIFICATION_TABLE).child(teacherModel.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PushNotificationResultSet pushNotificationResultSet = new PushNotificationResultSet();
                if (snapshot.getValue() != null) {
                    pushNotificationResultSet = snapshot.getValue(PushNotificationResultSet.class);
                }
                pushNotificationResultSet.getResult().add(pushInfoModel);
                saveNotification(pushNotificationResultSet, teacherModel);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                materialProgress.dismiss();
                Toast.makeText(MainActivity.this, "Getting error, due to: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNotification(PushNotificationResultSet pushNotificationResultSet, StudentModel teacherModel){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(AppConstants.NOTIFICATION_TABLE).child(teacherModel.getUserId()).setValue(pushNotificationResultSet).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                materialProgress.dismiss();
                Toast.makeText(MainActivity.this, "Send Success" , Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                materialProgress.dismiss();
                Toast.makeText(MainActivity.this, "Getting error, due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void checkVideoAccessPermission(String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(AppConstants.ACCESS_PERMISSION_TABLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String userId = sessionHelper.getLoginInfo().getUserId();
                    AssignStudentResultSet  assignStudentResultSet = snapshot.getValue(AssignStudentResultSet.class);
                    for(AssignStudentModel row : assignStudentResultSet.getResult()) {
                        if(row.getUserId().equals(userId)) {
                            if(type.equals("hardwareButton")) {
                                Intent openHardwarePage = new Intent(MainActivity.this,hardwarePage.class);
                                startActivity(openHardwarePage);
                            } else if(type.equals("guideButton")){
                                Intent openGuide = new Intent(MainActivity.this,videoTutorialPage.class);
                                startActivity(openGuide);
                            } else if(type.equals("simulationButton")){
                                Intent openSimunation= new Intent(MainActivity.this,pcAssemblyGuide.class);
                                startActivity(openSimunation);
                            }
                        }
                    }
                }
                Toast.makeText(MainActivity.this,"You have no permission to access video", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                materialProgress.dismiss();
                Toast.makeText(MainActivity.this, "Getting error, due to: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
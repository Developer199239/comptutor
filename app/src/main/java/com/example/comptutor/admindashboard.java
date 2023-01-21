package com.example.comptutor;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.comptutor.utils.AppConstants;
import com.example.comptutor.utils.BaseActivity;
import com.example.comptutor.utils.ClassModel;
import com.example.comptutor.utils.ComptutorApplication;
import com.example.comptutor.utils.GenerateKeyModel;
import com.example.comptutor.utils.MaterialProgress;
import com.example.comptutor.utils.NotificationDialog;
import com.example.comptutor.utils.NotificationReloadEvent;
import com.example.comptutor.utils.PushNotificationResultSet;
import com.example.comptutor.utils.SessionHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class admindashboard extends BaseActivity {
    String TAG  = "admindashboard";
    FloatingActionButton mAddFab, addNewClassFab, deleteClassFab;
    TextView addNewClassFabText, deleteClassFabText;
    Boolean isAllFabsVisible;
    private MaterialProgress materialProgress;
    private SessionHelper sessionHelper;
    DatabaseReference databaseReference;
    private ConstraintLayout classInfoLayout;
    private TextView classTitle;
    private ClassModel classModel;
    private ImageView ivCodeGenerator, ivLogout, ivNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData();
        ComptutorApplication.Companion.uploadTokenInServer();
        getNotification();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initView() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        materialProgress = new MaterialProgress(this);
        sessionHelper = new SessionHelper(getApplicationContext());
        ivLogout = findViewById(R.id.ivLogout);
        ivCodeGenerator = findViewById(R.id.ivCodeGenerator);
        ivNotification = findViewById(R.id.ivNotification);
        classInfoLayout = findViewById(R.id.classInfoLayout);
        classTitle = findViewById(R.id.classTitle);
        ivLogout.setOnClickListener(view -> {
            FirebaseMessaging.getInstance().deleteToken();
            sessionHelper.clearSession();
            Intent openLogin=new Intent(admindashboard.this,Login.class);
            startActivity(openLogin);
            finish();
        });
        ivCodeGenerator.setOnClickListener(view -> {
            generateClassKey();
        });
        classInfoLayout.setOnClickListener(view -> {
            isGenerateClassCode(true);
        });
        ivNotification.setOnClickListener(view -> {
            NotificationDialog notificationDialog = NotificationDialog.newInstance();
            notificationDialog.setCancelable(false);
            notificationDialog.show(getSupportFragmentManager(), "NotificationDialog");
        });

        initFab();

        if(sessionHelper.getStringValue(SessionHelper.FIREBASE_TOKEN).isEmpty()) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    Log.d(TAG, ""+task.getResult());
                }
            });
        }
    }

    private void initFab() {
        mAddFab = findViewById(R.id.addClass);
        addNewClassFab = findViewById(R.id.addNewClassFab);
        deleteClassFab = findViewById(R.id.deleteClassFab);
        addNewClassFabText = findViewById(R.id.addNewClassFabText);
        deleteClassFabText = findViewById(R.id.deleteClassFabText);
        addNewClassFab.setVisibility(View.GONE);
        deleteClassFab.setVisibility(View.GONE);
        addNewClassFabText.setVisibility(View.GONE);
        deleteClassFabText.setVisibility(View.GONE);
        isAllFabsVisible = false;
        mAddFab.setOnClickListener(view -> {
            fabAction();
        });
        deleteClassFab.setOnClickListener(view -> {
            if (classInfoLayout.getVisibility() == View.VISIBLE) {
                showDeleteNewsAlert();
            } else {
                showToast("You have no running class");
            }
        });

        addNewClassFab.setOnClickListener(view -> {
            if (classInfoLayout.getVisibility() == View.VISIBLE) {
                showToast("Your current class is running, please remove this one first");
            } else {
                fabAction();
                startActivity(new Intent(getApplicationContext(), AddClassActivity.class));
            }
        });
    }

    private void fabAction() {
        if (!isAllFabsVisible) {
            addNewClassFab.show();
            deleteClassFab.show();
            addNewClassFabText.setVisibility(View.VISIBLE);
            deleteClassFabText.setVisibility(View.VISIBLE);
            isAllFabsVisible = true;
        } else {
            addNewClassFab.hide();
            deleteClassFab.hide();
            addNewClassFabText.setVisibility(View.GONE);
            deleteClassFabText.setVisibility(View.GONE);
            isAllFabsVisible = false;
        }
    }

    private void fetchData() {
        materialProgress.show();
        databaseReference.child(AppConstants.CLASS_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                materialProgress.dismiss();
                if (snapshot.getValue() != null) {
                    classModel = snapshot.getValue(ClassModel.class);
                    bindClassInfo(classModel);
                } else {
                    classInfoLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                materialProgress.dismiss();
                Toast.makeText(admindashboard.this, "Getting error, due to: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindClassInfo(ClassModel classModel) {
        classTitle.setText(classModel.getClassName());
        classInfoLayout.setVisibility(View.VISIBLE);
        isGenerateClassCode(false);
        ComptutorApplication.Companion.setClassModel(classModel);
    }

    private void showToast(String message) {
        Toast.makeText(admindashboard.this, message, Toast.LENGTH_LONG).show();
    }

    private void showDeleteNewsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(admindashboard.this).create();
        alertDialog.setTitle("Delete Class");
        alertDialog.setMessage("Do you want to delete class");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> {
                    dialog.dismiss();
                    deleteClass();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {

        });
        alertDialog.show();
    }

    private void deleteClass() {
        materialProgress.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = ref.child(AppConstants.CLASS_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID));
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
                deleteCode();
                deleteAssignedStudent();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                materialProgress.dismiss();
                showToast(databaseError.getMessage());
            }
        });
    }

    private void deleteCode() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = ref.child(AppConstants.GENERATE_KEY_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID));
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
                materialProgress.dismiss();
                showToast("Delete Success");
                fetchData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                materialProgress.dismiss();
                showToast(databaseError.getMessage());
            }
        });
    }

    private void deleteAssignedStudent() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = ref.child(AppConstants.ASSIGN_STUDENT_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID));
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void generateClassKey() {
        if (classInfoLayout.getVisibility() != View.VISIBLE) {
            showToast("Please create class first!");
            return;
        }
        materialProgress.show();
        databaseReference.child(AppConstants.GENERATE_KEY_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    materialProgress.dismiss();
                    showToast("Code already generated!");
                } else {
                    String key = sessionHelper.getStringValue(SessionHelper.USER_ID) + "" + System.currentTimeMillis();
                    GenerateKeyModel model = new GenerateKeyModel(classModel.getClassId(), key);
                    databaseReference.child(AppConstants.GENERATE_KEY_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            ivCodeGenerator.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(admindashboard.this, R.color.red)));
                            materialProgress.dismiss();
                            showToast("Code generate success");
                        }
                    }).addOnFailureListener(e -> {
                        materialProgress.dismiss();
                        showToast("Code generate failed, due to: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                materialProgress.dismiss();
                Toast.makeText(admindashboard.this, "Getting error, due to: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void isGenerateClassCode(Boolean isChecked) {
        databaseReference.child(AppConstants.GENERATE_KEY_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    ivCodeGenerator.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(admindashboard.this, R.color.red)));
                    if(isChecked) {
                        ComptutorApplication.Companion.setClassModel(classModel);
                        startActivity(new Intent(admindashboard.this, ClassHomeActivity.class));
                    }
                } else {
                    if(isChecked) {
                        showToast("Please generate class code");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPushNotificationResultSet(PushNotificationResultSet event) {
        TextView tvBatchCount = findViewById(R.id.tvBatchCount);
        if(event.getResult().size() == 0) {
            tvBatchCount.setVisibility(View.GONE);
            return;
        } else {
            tvBatchCount.setVisibility(View.VISIBLE);
        }
        String batchCount = "";
        if(event.getResult().size()>9) {
            batchCount = "9+";
        }else {
            batchCount = "0"+event.getResult().size();
        }
        tvBatchCount.setText(batchCount);
    }
}
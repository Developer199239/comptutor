package com.example.comptutor;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
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
import com.example.comptutor.utils.ClassModel;
import com.example.comptutor.utils.GenerateKeyModel;
import com.example.comptutor.utils.MaterialProgress;
import com.example.comptutor.utils.SessionHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class admindashboard extends AppCompatActivity {
    FloatingActionButton mAddFab, addNewClassFab, deleteClassFab;
    TextView addNewClassFabText, deleteClassFabText;
    Boolean isAllFabsVisible;
    private MaterialProgress materialProgress;
    private SessionHelper sessionHelper;
    DatabaseReference databaseReference;
    private ConstraintLayout classInfoLayout;
    private TextView classTitle;
    private ClassModel classModel;
    private ImageView ivCodeGenerator;

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
    }

    private void initView() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        materialProgress = new MaterialProgress(this);
        sessionHelper = new SessionHelper(getApplicationContext());
        ivCodeGenerator = findViewById(R.id.ivCodeGenerator);
        classInfoLayout = findViewById(R.id.classInfoLayout);
        classTitle = findViewById(R.id.classTitle);
        ivCodeGenerator.setOnClickListener(view -> {
            generateClassKey();
        });
        initFab();
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
        isGenerateClassCode();
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

    private void isGenerateClassCode() {
        databaseReference.child(AppConstants.GENERATE_KEY_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    ivCodeGenerator.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(admindashboard.this, R.color.red)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
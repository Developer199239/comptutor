package com.example.comptutor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class admindashboard extends AppCompatActivity {
    FloatingActionButton mAddFab, addNewClassFab, deleteClassFab;
    TextView addNewClassFabText, deleteClassFabText;
    Boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);
        initView();
    }

    private void initView() {
        initFab();
    }

    private void initFab(){
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
        });
        deleteClassFab.setOnClickListener(
                view -> Toast.makeText(admindashboard.this, "Delete", Toast.LENGTH_SHORT
                ).show());
        addNewClassFab.setOnClickListener(
                view -> Toast.makeText(admindashboard.this, "Add Class", Toast.LENGTH_SHORT
                ).show());
    }
}
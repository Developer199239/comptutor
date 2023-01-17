package com.example.comptutor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Member;

public class admindashboard extends AppCompatActivity {
    StorageReference storageReference;
    DatabaseReference databaseReference;
    private ImageButton guideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);
        guideButton = findViewById(R.id.guideBtn);
        guideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGuide = new Intent(getApplicationContext(),videoTutorialPage.class);
                startActivity(openGuide);
            }
        });

        }
}
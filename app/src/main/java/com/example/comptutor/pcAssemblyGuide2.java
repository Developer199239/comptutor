package com.example.comptutor;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class pcAssemblyGuide2 extends AppCompatActivity {
    Button simulationbtn2;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_assembly_guide2);
        simulationbtn2 = findViewById(R.id.simulationButton2);
        simulationbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openSimulation1 = new Intent(pcAssemblyGuide2.this,pcAssemblySimulation2.class);
                startActivity(openSimulation1);
            }
        });
    }
}
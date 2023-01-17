package com.example.comptutor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.savedstate.SavedStateRegistryOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.sql.RowId;

public class hardwarePage extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    BottomNavigationView bottomNavigationView2;
    BottomNavigationView bottomNavigationView3;
    ChasisFragment chasisFragment = new ChasisFragment();
    MotherboardFragment motherboardFragment = new MotherboardFragment();
    MotherboardComponentsFragment motherboardComponentsFragment = new MotherboardComponentsFragment();
    CPUFragment cpuFragment = new CPUFragment();
    RAMFragment ramFragment = new RAMFragment();
    GPUFragment gpuFragment = new GPUFragment();
    SoundCardFragment soundCardFragment = new SoundCardFragment();
    HDDFragment hddFragment = new HDDFragment();
    SSDFragment ssdFragment = new SSDFragment();
    PSUFragment psuFragment = new PSUFragment();
    MonitorFragment monitorFragment = new MonitorFragment();
    KeyboardFragment keyboardFragment = new KeyboardFragment();
    MouseFragment mouseFragment = new MouseFragment();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_page);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView2 = findViewById(R.id.bottomNavigationView2);
        bottomNavigationView3 = findViewById(R.id.bottomNavigationView3);
        getSupportFragmentManager().beginTransaction().replace(R.id.cont,chasisFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chasisFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,chasisFragment).commit();
                    return true;
                    case R.id.motherboardFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,motherboardFragment).commit();
                        return true;
                    case R.id.motherboardComponentsFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,motherboardComponentsFragment).commit();
                        return true;
                    case R.id.ramFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,ramFragment).commit();
                        return true;
                    case  R.id.gpuFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,gpuFragment).commit();
                        return true;
                }
                return false;
            }
        });
        bottomNavigationView2.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.soundCardFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,soundCardFragment).commit();
                    return true;
                    case R.id.hddFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,hddFragment).commit();
                        return true;
                    case R.id.ssdFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,ssdFragment).commit();
                        return true;
                    case R.id.psuFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,psuFragment).commit();
                        return true;
                    case R.id.monitorFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,monitorFragment).commit();
                        return true;
                }
                return false;
            }
        });
        bottomNavigationView3.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.keyboardFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,keyboardFragment).commit();
                    return true;
                    case R.id.mouseFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,mouseFragment).commit();
                    return true;
                    case R.id.cpuFragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cont,cpuFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

}
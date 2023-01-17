package com.example.comptutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;


public class videoTutorialPage extends AppCompatActivity {
    RecyclerView mRecycleView;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_tutorial_page);
        mRecycleView = findViewById(R.id.recycleViewShowVideo);
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        database =FirebaseDatabase.getInstance();
        databaseReference = database.getReference("video");

    }

    @Override
    protected void onStart() {
        super.onStart();
       FirebaseRecyclerOptions<model> options = new FirebaseRecyclerOptions.Builder<model>()
               .setQuery(databaseReference,model.class)
               .build();
       FirebaseRecyclerAdapter<model,ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<model,
               ViewHolder>(options) {
           @Override
           protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull model model) {
               holder.setVideo(getApplication(),
                       model.getTitle(),
                       model.getVideourl());
           }

           @NonNull
           @Override
           public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext())
                       .inflate(R.layout.row,parent,false);
               return new ViewHolder(view);
           }
       };
        firebaseRecyclerAdapter.startListening();
        mRecycleView.setAdapter(firebaseRecyclerAdapter);
    }
}
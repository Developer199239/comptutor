package com.example.comptutor;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.DragEvent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class pcAssemblySimulation extends AppCompatActivity implements View.OnDragListener, View.OnLongClickListener {
    ImageView dragCPUFan;
    ImageView dragCPU;
    ImageView dragRAM1;
    ImageView dragGPU;
    ImageView dragRAM2;
    Button page2;

    private static final String CPU_IMAGE_VIEW_TAG = "CPU";
    private static final String GPU_IMAGE_VIEW_TAG = "GPU";
    private static final String RAM1_IMAGE_VIEW_TAG = "RAM1";
    private static final String RAM2_IMAGE_VIEW_TAG = "RAM2";
    private static final String CPU_FAN_IMAGE_VIEW_TAG = "CPU FAN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_assembly_simulation);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        findImgView();
        implentsEvents();
        dragCPU.setTag(CPU_IMAGE_VIEW_TAG);
        dragGPU.setTag(GPU_IMAGE_VIEW_TAG);
        dragRAM1.setTag(RAM1_IMAGE_VIEW_TAG);
        dragRAM2.setTag(RAM2_IMAGE_VIEW_TAG);
        dragCPUFan.setTag(CPU_FAN_IMAGE_VIEW_TAG);
        page2 = findViewById(R.id.pcAS2);
        page2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openPage2 = new Intent(pcAssemblySimulation.this,pcAssemblyGuide2.class);
                startActivity(openPage2);
            }
        });
    }
      private void implentsEvents() {
          dragCPU.setOnLongClickListener(this);
          dragGPU.setOnLongClickListener(this);
          dragGPU.setOnLongClickListener(this);
          dragRAM1.setOnLongClickListener(this);
          dragRAM2.setOnLongClickListener(this);
          dragCPUFan.setOnLongClickListener(this);
          findViewById(R.id.cpuHolder).setOnDragListener(this);
          findViewById(R.id.gpuHolder).setOnDragListener(this);
          findViewById(R.id.ramHolder1).setOnDragListener(this);
          findViewById(R.id.horizontalScrollView).setOnDragListener(this);
          findViewById(R.id.ramHolder2).setOnDragListener(this);
          findViewById(R.id.gpuItem).setOnDragListener(this);
          findViewById(R.id.ramItem1).setOnDragListener(this);
          findViewById(R.id.ramItem2).setOnDragListener(this);
          findViewById(R.id.cpuFanItem).setOnDragListener(this);
          findViewById(R.id.cpuFanItem).setOnDragListener(this);
          findViewById(R.id.cpuFanRl).setOnDragListener(this);
          findViewById(R.id.rlItems).setOnDragListener(this);
          findViewById(R.id.rlMotherboard).setOnDragListener(this);

    }
    private void findImgView() {
        dragCPU = findViewById(R.id.dragCpu);
        dragGPU = findViewById(R.id.dragGpu);
        dragRAM1 = findViewById(R.id.dragRam1);
        dragRAM2 = findViewById(R.id.dragRam2);
        dragCPUFan = findViewById(R.id.dragCpuFan);
    }

    @Override
    public boolean onLongClick(View view) {
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDragAndDrop(data, shadowBuilder, view, 0);
        view.setVisibility(view.INVISIBLE);
        return true;
    }

   @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        final int action = dragEvent.getAction();
        switch (action)
        {
            case DragEvent.ACTION_DRAG_STARTED:
            if (dragEvent.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                view.invalidate();
                return true;
            }
            return false;
            case DragEvent.ACTION_DRAG_ENTERED:
                view.invalidate();
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                view.invalidate();
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                view.invalidate();
                return true;
            case DragEvent.ACTION_DROP:
                ClipData.Item item = dragEvent.getClipData().getItemAt(0);
                String dragData = item.getText().toString();
                Toast.makeText(this, "The " + dragData + " Has been placed", Toast.LENGTH_SHORT).show();
                view.invalidate();
                View imageView = (View) dragEvent.getLocalState();
                ViewGroup owner = (ViewGroup) imageView.getParent();
                owner.removeView(imageView);
                RelativeLayout destinationLayout = (RelativeLayout) view;
                destinationLayout.addView(imageView);
                imageView.setVisibility(View.VISIBLE);
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                view.invalidate();
                return true;
        }
        return false;
    }

}


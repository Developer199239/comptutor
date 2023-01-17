package com.example.comptutor;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class pcAssemblySimulation2 extends AppCompatActivity implements View.OnDragListener, View.OnLongClickListener {
    ImageView dragPsu;
    ImageView dragMotherboard;
    ImageView dragHdd;
    private static final String PSU_IMAGE_VIEW_TAG = "PSU";
    private static final String MOTHERBOARD_IMAGE_VIEW_TAG = "MOTHERBOARD";
    private static final String HDD_IMAGE_VIEW_TAG = "HDD";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_assembly_simulation2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        findImgView();
        implentsEvents();
        dragPsu.setTag(PSU_IMAGE_VIEW_TAG);
        dragMotherboard.setTag(MOTHERBOARD_IMAGE_VIEW_TAG);
        dragMotherboard.setTag(MOTHERBOARD_IMAGE_VIEW_TAG);
        dragHdd.setTag(HDD_IMAGE_VIEW_TAG);
    }

    private void findImgView() {
        dragPsu = findViewById(R.id.dragPsu);
        dragHdd = findViewById(R.id.draghdd);
        dragMotherboard = findViewById(R.id.dragMotherboard);
    }

    private void implentsEvents() {
        findViewById(R.id.rlItems2).setOnDragListener(this);
        findViewById(R.id.rlChasis).setOnDragListener(this);
        dragMotherboard.setOnLongClickListener(this);
        findViewById(R.id.motherboardHolder).setOnDragListener(this);
        findViewById(R.id.dragMotherboard).setOnDragListener(this);
        findViewById(R.id.motherboardItem).setOnDragListener(this);
        dragPsu.setOnLongClickListener(this);
        findViewById(R.id.psuHolder).setOnDragListener(this);
        findViewById(R.id.dragPsu).setOnDragListener(this);
        findViewById(R.id.psuItem).setOnDragListener(this);
        dragHdd.setOnLongClickListener(this);
        findViewById(R.id.hddHolder).setOnDragListener(this);
        findViewById(R.id.draghdd).setOnDragListener(this);
        findViewById(R.id.hddItem).setOnDragListener(this);


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
                //new AlertDialog.Builder(pcAssemblySimulation2.this).setMessage("You Drop").setPositiveButton()
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                view.invalidate();
                return true;
        }
        return false;
    }
}


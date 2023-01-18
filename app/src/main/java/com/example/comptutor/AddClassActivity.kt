package com.example.comptutor

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.comptutor.databinding.ActivityAddClassBinding
import com.example.comptutor.utils.AppConstants
import com.example.comptutor.utils.ClassModel
import com.example.comptutor.utils.SessionHelper
import com.example.comptutor.utils.toast
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.firebase.database.FirebaseDatabase

class AddClassActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddClassBinding
    private var delfaultColor = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClassBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
    }

    private fun initView() {
        val sessionHelper = SessionHelper(this)
        delfaultColor = getColor(R.color.et_bg)
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivSave.setOnClickListener {
            if(validationCheck()) {
                val databaseReference = FirebaseDatabase.getInstance().reference
                val classModel = ClassModel(
                    teacherId = sessionHelper.getStringValue(SessionHelper.USER_ID),
                    title = binding.etTitle.text.toString(),
                    code = binding.etCode.text.toString(),
                    className = binding.etClassName.text.toString(),
                    color = delfaultColor,
                    description = binding.etDescription.text.toString(),
                )
                databaseReference.child(AppConstants.CLASS_TABLE).setValue(classModel).addOnSuccessListener {
                    "Class add success".toast(this)
                    finish()
                }.addOnFailureListener {
                    "Class add failed due to: ${it.message}".toast(this)
                    finish()
                }
            }
        }

        binding.etChooseColor.setOnClickListener {
            ColorPickerDialog
                .Builder(this)
                .setTitle("Pick Theme")
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(delfaultColor)
                .setColorListener { color, colorHex ->
                    delfaultColor = color
                    binding.etChooseColor.backgroundTintList = ColorStateList.valueOf(color)
                }
                .show()
        }
    }

    private fun validationCheck() : Boolean{
        if (isEmptyEditText(binding.etTitle)) {
            "Title can not empty".toast(this)
            return false
        } else if (isEmptyEditText(binding.etCode)) {
            "Code can not empty".toast(this)
            return false
        } else if (isEmptyEditText(binding.etClassName)) {
            "ClassName can not empty".toast(this)
            return false
        } else if (isEmptyEditText(binding.etDescription)) {
            "Description can not empty".toast(this)
            return false
        }
        return true
    }

    private fun isEmptyEditText(editText: EditText): Boolean {
        return editText.text.toString().trim()
            .isEmpty()
    }
}
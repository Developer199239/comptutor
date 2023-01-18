package com.example.comptutor

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

public class MSPEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context,attrs) {

    init {
        applyFont()
    }

    private fun applyFont(){
        val typeface: Typeface = Typeface.createFromAsset(context.assets,
            "Montserrat-Regular.ttf")
        setTypeface(typeface)
    }
}
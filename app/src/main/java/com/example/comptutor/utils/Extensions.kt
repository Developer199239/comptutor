package com.example.comptutor.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.text.SimpleDateFormat
import java.util.*

fun Any.toast(context: Context, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(context, this.toString(), duration).apply { show() }
}

fun Any.getCanonicalName(fragment: Fragment): String {
    return fragment.javaClass.canonicalName
}

fun getFileExtension(activity: Activity, uri: Uri?): String? {
    return MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
}

fun removeFragmentFromBackStack(fm : FragmentManager, tagName: String){
    for (i in fm.backStackEntryCount - 1 downTo 0) {
        if (fm.getBackStackEntryAt(i).name == tagName) {
            fm.popBackStack()
            break
        }
    }
}

fun ImageView.setTint(@ColorRes colorRes: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)))
}

fun currentDataInString(): String {
    return System.currentTimeMillis().epochToDateString("yyyy/MM/dd HH:mm:ss a")!!
}

fun Long.epochToDateString(formatString: String?): String? {
    val sdf = SimpleDateFormat(formatString, Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(this)
}
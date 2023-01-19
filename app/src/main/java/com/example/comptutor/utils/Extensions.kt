package com.example.comptutor.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

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
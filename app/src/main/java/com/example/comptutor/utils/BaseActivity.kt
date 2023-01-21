package com.example.comptutor.utils

import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


open class BaseActivity : AppCompatActivity() {
    lateinit var sessionHelper: SessionHelper

    override fun onStart() {
        super.onStart()
        sessionHelper = SessionHelper(applicationContext)
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: NotificationEvent?) {
       if(sessionHelper.getLoginInfo().role == AppConstants.ROLE_STUDENT) {

       } else if(sessionHelper.getLoginInfo().role == AppConstants.ROLE_TEACHER) {

       }
    }

    private fun showAlert(){
        val alertDialog: AlertDialog = AlertDialog.Builder(this@BaseActivity).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Alert message to be shown")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        alertDialog.show()
    }
}
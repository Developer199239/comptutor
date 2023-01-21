package com.example.comptutor.utils

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.comptutor.utils.EmbeddVideoLinkPlayDialog.Companion.newInstance
import com.google.firebase.database.*
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


open class BaseActivity : AppCompatActivity() {
    lateinit var sessionHelper: SessionHelper
    private lateinit var materialProgress: MaterialProgress

    override fun onStart() {
        super.onStart()
        sessionHelper = SessionHelper(applicationContext)
        materialProgress = MaterialProgress(this)
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: NotificationEvent?) {
       if(sessionHelper.getLoginInfo().role == AppConstants.ROLE_STUDENT) {
           val pushInfoModel = Gson().fromJson(event!!.data, PushInfoModel::class.java)
           if(pushInfoModel.pushType == AppConstants.PUSH_TYPE_ASSIGN_STUDENT) {
               val model =  Gson().fromJson(pushInfoModel.pushBody, AssignClassPushModel::class.java)
               showAlertForAssignStudent(model)
           }
       } else if(sessionHelper.getLoginInfo().role == AppConstants.ROLE_TEACHER) {
           val pushInfoModel = Gson().fromJson(event!!.data, PushInfoModel::class.java)
           if(pushInfoModel.pushType == AppConstants.PUSH_TYPE_REQUEST_CODE) {
               getNotification()
           }
       }
    }

     fun getNotification() {
        val reference = FirebaseDatabase.getInstance().reference
        reference.child(AppConstants.NOTIFICATION_TABLE).child(sessionHelper.getLoginInfo().userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var pushNotificationResultSet: PushNotificationResultSet? =
                        PushNotificationResultSet()
                    if (snapshot.value != null) {
                        pushNotificationResultSet = snapshot.getValue(
                            PushNotificationResultSet::class.java
                        )
                    }
                    EventBus.getDefault().post(pushNotificationResultSet)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun showAlertForAssignStudent(assignClassPushModel: AssignClassPushModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invitation To Join Class")
        builder.setMessage("${assignClassPushModel.teacherName} invited you to join ${assignClassPushModel.className}")
        builder.setPositiveButton("Join Class") { dialog, which ->
            dialog.dismiss()
            joinClass(assignClassPushModel)
        }
        builder.setNegativeButton("Ignore") { dialog, which ->
        }
        builder.show()
    }

    private fun joinClass(assignClassPushModel: AssignClassPushModel) {
        val databaseReference = FirebaseDatabase.getInstance().reference

        materialProgress.show()
        databaseReference.child(AppConstants.ASSIGN_STUDENT_TABLE)
            .child(assignClassPushModel.teacherId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null) {
                        val selectedUsers = snapshot.getValue(AssignStudentResultSet::class.java)
                        val loginUserInfo = sessionHelper.getLoginInfo()
                        selectedUsers!!.result.forEach {
                            if(it.userId == loginUserInfo.userId) {
                                getClassEmbeddedLink(assignClassPushModel)
                                return
                            }
                        }
                        materialProgress.dismiss()
                        "You have no access to join class".toast(this@BaseActivity)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    materialProgress.dismiss()
                    Toast.makeText(
                        this@BaseActivity,
                        "Getting error, due to: " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getClassEmbeddedLink(assignClassPushModel: AssignClassPushModel){
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.VIDEO_LINK_TABLE)
            .child(assignClassPushModel.teacherId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materialProgress.dismiss()
                    if(snapshot.value != null) {
                        val videoLinkModel = snapshot.getValue(VideoLinkModel::class.java)!!
                        val assignStudentDialog = newInstance()
                        assignStudentDialog.isCancelable = false
                        assignStudentDialog.show(supportFragmentManager, "")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    materialProgress.dismiss()
                    Toast.makeText(
                        this@BaseActivity,
                        "Getting error, due to: " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

     fun showAlertForVideoWatchingFinish() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Info")
        builder.setMessage("Did you finish video?")
        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        builder.show()
    }
}
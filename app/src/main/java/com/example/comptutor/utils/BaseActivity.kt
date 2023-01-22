package com.example.comptutor.utils

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.comptutor.utils.ComptutorApplication.Companion.pubnub
import com.example.comptutor.utils.EmbeddVideoLinkPlayDialog.Companion.newInstance
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper.FCMPayload
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
           } else if(pushInfoModel.pushType == AppConstants.PUSH_TYPE_ACCESS_PERMISSION) {
               val model =  Gson().fromJson(pushInfoModel.pushBody, AssignClassPushModel::class.java)
               showAlertForVideoAccessPermission(model)
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

    private fun showAlertForVideoAccessPermission(assignClassPushModel: AssignClassPushModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Access Permission")
        builder.setMessage("${assignClassPushModel.teacherName} given video access permission")
        builder.setNeutralButton("Ok") { dialog, which ->
            dialog.dismiss()
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
                        val assignStudentDialog = newInstance(videoLinkModel.videoLink)
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
            requestCode()
        }
        builder.setNegativeButton("No") { dialog, which ->
        }
        builder.show()
    }

    private  fun requestCode() {
        materialProgress.show()
        val mFireStore = FirebaseFirestore.getInstance()
        mFireStore.collection("users").get().addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val pnTokenList = ArrayList<String>()
                var teacherModel: StudentModel? = null
                for (document in task.result) {
                    val studentModel = document.toObject(StudentModel::class.java)
                    if (studentModel.role == AppConstants.ROLE_STUDENT) {
                        pnTokenList.add(studentModel.token)
                    } else if (studentModel.role == AppConstants.ROLE_TEACHER) {
                        teacherModel = studentModel
                    }
                }
                if (teacherModel == null) {
                    materialProgress.dismiss()
                    Toast.makeText(this@BaseActivity, "Teacher not found", Toast.LENGTH_LONG).show()
                    return@OnCompleteListener
                }
                pnTokenList.add(sessionHelper.getLoginInfo().token)
                sendCodeRequestPush(pnTokenList, teacherModel)
            } else {
                materialProgress.dismiss()
                Toast.makeText(this@BaseActivity, "Failed to load user list", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun sendCodeRequestPush(
        pnTokenList: java.util.ArrayList<String>,
        teacherModel: StudentModel
    ) {
        val pushPayloadHelper = PushPayloadHelper()
        val loggedInUserModel = sessionHelper.getLoginInfo()
        val fcmPayload = FCMPayload()
        val payload: Map<String, Any> = HashMap()
        fcmPayload.setCustom(payload)
        val fcmNotification = FCMPayload.Notification()
            .setTitle("Request")
            .setBody(loggedInUserModel.firstName + " Request for code")
        fcmPayload.setNotification(fcmNotification)
        val data: MutableMap<String, Any> = HashMap()
        val pushInfoModel =
            PushInfoModel(AppConstants.PUSH_TYPE_REQUEST_CODE, Gson().toJson(loggedInUserModel), "")
        data["data"] = pushInfoModel
        fcmPayload.setData(data)
        pushPayloadHelper.setFcmPayload(fcmPayload)
        val commonPayload: MutableMap<String, Any> = HashMap()
        commonPayload["text"] = "Request"
        commonPayload["text"] = loggedInUserModel.firstName + " Request for code"
        pushPayloadHelper.setCommonPayload(commonPayload)
        val pushPayload = pushPayloadHelper.build()
        pubnub!!.publish()
            .channel(AppConstants.PUB_SUB_CHANNEL)
            .message(pushPayload)
            .async { result, status ->
                Log.d("PUBNUB", "-->PNStatus.getStatusCode = " + status.statusCode)
                if (status.statusCode == 200) {
                    getNotification(pushInfoModel, teacherModel)
                } else {
                    materialProgress.dismiss()
                    Toast.makeText(
                        this@BaseActivity,
                        "Failed to send push notification",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
    }


    private fun getNotification(pushInfoModel: PushInfoModel, teacherModel: StudentModel) {
        pushInfoModel.notificationId =
            sessionHelper.getLoginInfo().userId + "" + System.currentTimeMillis()
        val reference = FirebaseDatabase.getInstance().reference
        reference.child(AppConstants.NOTIFICATION_TABLE).child(teacherModel.userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var pushNotificationResultSet: PushNotificationResultSet? =
                        PushNotificationResultSet()
                    if (snapshot.value != null) {
                        pushNotificationResultSet = snapshot.getValue(
                            PushNotificationResultSet::class.java
                        )
                    }
                    pushNotificationResultSet!!.result.add(pushInfoModel)
                    saveNotification(pushNotificationResultSet, teacherModel)
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

    private fun saveNotification(
        pushNotificationResultSet: PushNotificationResultSet?,
        teacherModel: StudentModel
    ) {
        val reference = FirebaseDatabase.getInstance().reference
        reference.child(AppConstants.NOTIFICATION_TABLE).child(teacherModel.userId)
            .setValue(pushNotificationResultSet).addOnSuccessListener {
                materialProgress.dismiss()
                Toast.makeText(this@BaseActivity, "Send Success", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                materialProgress.dismiss()
                Toast.makeText(
                    this@BaseActivity,
                    "Getting error, due to: " + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

}
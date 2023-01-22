package com.example.comptutor.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comptutor.R
import com.example.comptutor.databinding.DialogNotificationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.models.consumer.PNPublishResult
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper

class NotificationDialog : DialogFragment() {
    private var _binding: DialogNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationListAdapter: NotificationListAdapter
    private lateinit var materialProgress: MaterialProgress
    private lateinit var sessionHelper: SessionHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNotificationBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.70).toInt()
        dialog!!.window?.setLayout(width, height)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        sessionHelper = SessionHelper(requireContext())
        materialProgress = MaterialProgress(requireActivity())
        binding.rvStudent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudent.itemAnimator = DefaultItemAnimator()
        binding.rvStudent.setHasFixedSize(true)
        binding.ivCross.setOnClickListener {
            dismiss()
        }
        getNotification()
    }

    private fun getNotification() {
        materialProgress.show()
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
                    notificationListAdapter = NotificationListAdapter(pushNotificationResultSet!!.result, object : OnItemClickListener {
                        override fun onItemClick(view: View, item: Any, position: Int) {
                            when (view.id) {
                                R.id.btnSendCode -> {
                                    getAssignStudentList(item as PushInfoModel)
                                }
                                R.id.btnRemove -> {
                                    removeNotification(item as PushInfoModel)
                                }
                                R.id.btnAccessPermission -> {
                                    getVideoAccessStudentList(item as PushInfoModel)
                                }
                            }
                        }

                    })
                    binding.rvStudent.adapter = notificationListAdapter
                    notificationListAdapter.notifyDataSetChanged()
                    materialProgress.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    materialProgress.dismiss()
                    error.message.toast(requireContext())
                }
            })
    }

    private fun getVideoAccessStudentList(pushInfoModel: PushInfoModel) {
        val studentModel = Gson().fromJson(pushInfoModel.pushBody, StudentModel::class.java)
        materialProgress.show()
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.ACCESS_PERMISSION_TABLE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        val selectedUsers = snapshot.getValue(AssignStudentResultSet::class.java)
                        val map = hashMapOf<String, String>()
                        selectedUsers!!.result.forEach {
                            map[it.userId] = it.userId
                        }
                        val  row = AssignStudentModel(
                            email = studentModel.email,
                            firstName = studentModel.firstName,
                            lastName = studentModel.lastName,
                            lrn = studentModel.lrn,
                            userId = studentModel.userId
                        )
                        if(!map.containsKey(studentModel.userId)) {
                            selectedUsers.result.add(row)
                        }
                        saveVideoAccessStudentList(selectedUsers, pushInfoModel)
                    } else {
                        val  row = AssignStudentModel(
                            email = studentModel.email,
                            firstName = studentModel.firstName,
                            lastName = studentModel.lastName,
                            lrn = studentModel.lrn,
                            userId = studentModel.userId
                        )
                        val resultSet = AssignStudentResultSet()
                        resultSet.result.add(row)
                        saveVideoAccessStudentList(resultSet, pushInfoModel)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    materialProgress.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Getting error, due to: " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun getAssignStudentList(pushInfoModel: PushInfoModel) {
        val studentModel = Gson().fromJson(pushInfoModel.pushBody, StudentModel::class.java)
        materialProgress.show()
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.ASSIGN_STUDENT_TABLE)
            .child(sessionHelper.getStringValue(SessionHelper.USER_ID))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null) {
                        val selectedUsers = snapshot.getValue(AssignStudentResultSet::class.java)
                        val map = hashMapOf<String,String>()
                        selectedUsers!!.result.forEach {
                            map[it.userId] = it.userId
                        }
                        val  row = AssignStudentModel(
                            email = studentModel.email,
                            firstName = studentModel.firstName,
                            lastName = studentModel.lastName,
                            lrn = studentModel.lrn,
                            userId = studentModel.userId
                        )
                        if(!map.containsKey(studentModel.userId)) {
                            selectedUsers.result.add(row)
                        }
                        saveAssignStudent(selectedUsers, pushInfoModel)
                    } else {
                        val  row = AssignStudentModel(
                            email = studentModel.email,
                            firstName = studentModel.firstName,
                            lastName = studentModel.lastName,
                            lrn = studentModel.lrn,
                            userId = studentModel.userId
                        )
                        val resultSet = AssignStudentResultSet()
                        resultSet.result.add(row)
                        saveAssignStudent(resultSet, pushInfoModel)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    materialProgress.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Getting error, due to: " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun saveVideoAccessStudentList(resultSet: AssignStudentResultSet, pushInfoModel: PushInfoModel) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.ACCESS_PERMISSION_TABLE).setValue(resultSet).addOnSuccessListener {
            getAllStudents(pushInfoModel, true)
        }.addOnFailureListener {
            materialProgress.dismiss()
            "Failed due to: ${it.message}".toast(requireContext())
        }
    }

    private fun saveAssignStudent(resultSet: AssignStudentResultSet, pushInfoModel: PushInfoModel) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.ASSIGN_STUDENT_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).setValue(resultSet).addOnSuccessListener {
            getAllStudents(pushInfoModel)
        }.addOnFailureListener {
            materialProgress.dismiss()
            "Failed due to: ${it.message}".toast(requireContext())
        }
    }

    private fun getAllStudents(pushInfoModel: PushInfoModel, reqForVideoAccess: Boolean = false) {
        val studentModel = Gson().fromJson(pushInfoModel.pushBody, StudentModel::class.java)
        val mFireStore = FirebaseFirestore.getInstance()
        mFireStore.collection("users")
            .get()
            .addOnSuccessListener { document ->
                val tokensList: ArrayList<String> = ArrayList()
                for (i in document.documents) {
                    val product = i.toObject(StudentModel::class.java)!!
                    if(product.userId == studentModel.userId || product.userId == sessionHelper.getLoginInfo().userId) {
                        Log.d("tag",product.firstName+""+studentModel.firstName)
                    } else {
                        tokensList.add(product.token)
                    }

                }
                sendNotification(pushInfoModel, tokensList, reqForVideoAccess)
            }
            .addOnFailureListener { e ->
                materialProgress.dismiss()
                e.message!!.toast(requireContext())
            }
    }
    private fun sendNotification(
        _pushInfoModel: PushInfoModel,
        pnExceptionsList: ArrayList<String>,
        reqForVideoAccess: Boolean
    ) {
        val studentModel = sessionHelper.getLoginInfo()
        val pushPayloadHelper = PushPayloadHelper()
        val fcmPayload = PushPayloadHelper.FCMPayload()
        val payload: MutableMap<String, Any> = HashMap()
        payload["pn_exceptions"] = pnExceptionsList
        fcmPayload.setCustom(payload)
        val fcmNotification = PushPayloadHelper.FCMPayload.Notification()
            .setTitle("Invitation To Join Class")
            .setBody(studentModel.firstName+" invited you to join class")
        if(reqForVideoAccess) {
            PushPayloadHelper.FCMPayload.Notification()
                .setTitle("Video Access")
                .setBody(studentModel.firstName+" given video access permission")
        }
        fcmPayload.setNotification(fcmNotification)
        val data: MutableMap<String, Any> = HashMap()
        val gson = Gson()
        val assignClassPushModel = AssignClassPushModel(
            classId = ComptutorApplication.classModel.classId,
            teacherId = ComptutorApplication.classModel.teacherId,
            title = ComptutorApplication.classModel.title,
            className = ComptutorApplication.classModel.className,
            teacherName = studentModel.firstName,
        )
        val pushInfoModel = PushInfoModel(AppConstants.PUSH_TYPE_ASSIGN_STUDENT, gson.toJson(assignClassPushModel))
        if(reqForVideoAccess) {
            pushInfoModel.pushType = AppConstants.PUSH_TYPE_ACCESS_PERMISSION
        }
        data["data"] = gson.toJson(pushInfoModel)
        fcmPayload.setData(data)
        pushPayloadHelper.setFcmPayload(fcmPayload)
        val commonPayload: MutableMap<String, Any> = HashMap()
        if(reqForVideoAccess) {
            commonPayload["text"] = studentModel.firstName+" given video access permission"
        }else {
            commonPayload["text"] = studentModel.firstName+" invited you to join class"
        }
        pushPayloadHelper.setCommonPayload(commonPayload)
        val pushPayload = pushPayloadHelper.build()
        ComptutorApplication.pubnub!!.publish()
            .channel(AppConstants.PUB_SUB_CHANNEL)
            .message(pushPayload)
            .async(PNCallback<PNPublishResult?> { result, status ->
                Log.d(
                    "PUBNUB",
                    "-->PNStatus.getStatusCode = " + status.statusCode
                )
                removeNotification(_pushInfoModel)
            }

            )
    }

    private fun removeNotification(pushInfoModel: PushInfoModel){
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
                    val iterator = pushNotificationResultSet!!.result.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next()
                        if(item.notificationId == pushInfoModel.notificationId) {
                            iterator.remove()
                            break
                        }
                    }
                    reference.child(AppConstants.NOTIFICATION_TABLE).child(sessionHelper.getLoginInfo().userId).setValue(pushNotificationResultSet).addOnSuccessListener {
                        materialProgress.dismiss()
                        "Success".toast(requireContext())
                        getNotification()
                        (requireActivity() as BaseActivity).getNotification()
                    }.addOnFailureListener {
                        materialProgress.dismiss()
                        "Failed".toast(requireContext())
                        getNotification()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    materialProgress.dismiss()
                }
            })
    }

    companion object {
        @JvmStatic
        fun newInstance(
        ) =
            NotificationDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
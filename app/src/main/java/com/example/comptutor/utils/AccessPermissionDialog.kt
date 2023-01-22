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
import com.example.comptutor.databinding.DialogAccessPermissionBinding
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.models.consumer.PNPublishResult
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper.FCMPayload

class AccessPermissionDialog : DialogFragment() {
    private var _binding: DialogAccessPermissionBinding? = null
    private val binding get() = _binding!!
    private lateinit var assignStudentListAdapter: AssignStudentListAdapter
    private lateinit var materialProgress: MaterialProgress
    private lateinit var sessionHelper: SessionHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAccessPermissionBinding.inflate(layoutInflater)
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
        binding.ivSave.setOnClickListener {
            val selectedStudent = assignStudentListAdapter.getSelectedUser()
            val item = AssignStudentResultSet()
            item.result = selectedStudent
            materialProgress.show()
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child(AppConstants.ACCESS_PERMISSION_TABLE).setValue(item)
                .addOnSuccessListener {
                    sendNotification(selectedStudent)
                }.addOnFailureListener {
                materialProgress.dismiss()
                "Failed due to: ${it.message}".toast(requireContext())
            }
        }
        bindStudentList()
    }

    private fun getPnExceptionsList(selectedStudent: ArrayList<AssignStudentModel>): ArrayList<String> {
        val selectStudentMap = hashMapOf<String, String>()
        selectedStudent.forEach {
            selectStudentMap[it.userId] = it.userId
        }
        val pnTokenList = arrayListOf<String>()
        ComptutorApplication.studentsList.forEach {
            if (!selectStudentMap.containsKey(it.userId)) {
                pnTokenList.add(it.token)
            }
        }
        pnTokenList.add(sessionHelper.getLoginInfo().token)
        return pnTokenList
    }

    private fun sendNotification(selectedStudent: ArrayList<AssignStudentModel>) {
        val studentModel = sessionHelper.getLoginInfo()
        val pushPayloadHelper = PushPayloadHelper()
        val fcmPayload = FCMPayload()
        val payload: MutableMap<String, Any> = HashMap()
        payload["pn_exceptions"] = getPnExceptionsList(selectedStudent)
        fcmPayload.setCustom(payload)
        val fcmNotification = FCMPayload.Notification()
            .setTitle("Access Permission")
            .setBody(studentModel.firstName + " given access permission to access video")
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
        val pushInfoModel = PushInfoModel(
            AppConstants.PUSH_TYPE_ACCESS_PERMISSION,
            gson.toJson(assignClassPushModel)
        )
        data["data"] = gson.toJson(pushInfoModel)
        fcmPayload.setData(data)
        pushPayloadHelper.setFcmPayload(fcmPayload)
        val commonPayload: MutableMap<String, Any> = HashMap()
        commonPayload["text"] = studentModel.firstName + " given access permission to access video"
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
                materialProgress.dismiss()
                "Success".toast(requireContext())
                dismiss()
            }

            )
    }

    private fun bindStudentList() {
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
                        showUserList(map)
                    } else {
                        showUserList(hashMapOf())
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

    fun showUserList(selectedUsers: HashMap<String, String>) {
        val mFireStore = FirebaseFirestore.getInstance()
        mFireStore.collection("users")
            .get()
            .addOnSuccessListener { document ->
                val studentsList: ArrayList<StudentModel> = ArrayList()
                for (i in document.documents) {
                    val product = i.toObject(StudentModel::class.java)!!
                    if (product.role == "student") {
                        product.userId = i.id
                        studentsList.add(product)
                    }
                }
                ComptutorApplication.studentsList = studentsList
                bindStudentList(selectedUsers)
            }
            .addOnFailureListener { e ->
                materialProgress.dismiss()
                e.message!!.toast(requireContext())
                ComptutorApplication.studentsList.clear()
                bindStudentList(selectedUsers)
            }
    }

    private fun bindStudentList(selectedUsers: HashMap<String, String>) {
        val studentsList: ArrayList<AssignStudentModel> = ArrayList()
        ComptutorApplication.studentsList.forEach {
            val item = AssignStudentModel(
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                lrn = it.lrn,
                userId = it.userId
            )
            if (selectedUsers.containsKey(it.userId)) {
                item.isSelected = true
            }
            studentsList.add(item)
        }
        assignStudentListAdapter = AssignStudentListAdapter(studentsList)
        binding.rvStudent.adapter = assignStudentListAdapter
        assignStudentListAdapter.notifyDataSetChanged()
        materialProgress.dismiss()
    }

    companion object {
        @JvmStatic
        fun newInstance(
        ) =
            AccessPermissionDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
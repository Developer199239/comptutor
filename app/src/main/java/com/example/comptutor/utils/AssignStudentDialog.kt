package com.example.comptutor.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comptutor.databinding.DialogAssignStudentBinding
import com.google.firebase.database.*

class AssignStudentDialog : DialogFragment() {
    private var _binding: DialogAssignStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var assignStudentListAdapter: AssignStudentListAdapter
    private lateinit var materialProgress: MaterialProgress
    private lateinit var sessionHelper: SessionHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAssignStudentBinding.inflate(layoutInflater)
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
            if(selectedStudent.isEmpty()) {
                "Select student for assign".toast(requireContext())
                return@setOnClickListener
            }
            val item = AssignStudentResultSet()
            item.result = selectedStudent
            materialProgress.show()
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child(AppConstants.ASSIGN_STUDENT_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).setValue(item).addOnSuccessListener {
                materialProgress.dismiss()
                "Success".toast(requireContext())
                dismiss()
            }.addOnFailureListener {
                materialProgress.dismiss()
                "Failed due to: ${it.message}".toast(requireContext())
            }

        }
        bindStudentList()
    }

    private fun bindStudentList() {
        materialProgress.show()
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.ASSIGN_STUDENT_TABLE)
            .child(sessionHelper.getStringValue(SessionHelper.USER_ID))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materialProgress.dismiss()
                    if(snapshot.value != null) {
                        val selectedUsers = snapshot.getValue(AssignStudentResultSet::class.java)
                        val map = hashMapOf<String,String>()
                        selectedUsers!!.result.forEach {
                            map[it.userId] = it.userId
                        }
                        showUserList(map)
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
        val studentsList: ArrayList<AssignStudentModel> = ArrayList()
        ComptutorApplication.studentsList.forEach {
            val item = AssignStudentModel(
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                lrn = it.lrn,
                userId = it.userId
            )
            if(selectedUsers.containsKey(it.userId)) {
                item.isSelected = true
            }
            studentsList.add(item)
        }
        assignStudentListAdapter = AssignStudentListAdapter(studentsList)
        binding.rvStudent.adapter = assignStudentListAdapter
        assignStudentListAdapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance(
        ) =
            AssignStudentDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
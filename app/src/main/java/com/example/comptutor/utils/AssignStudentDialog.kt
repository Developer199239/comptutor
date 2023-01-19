package com.example.comptutor.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comptutor.databinding.DialogAssignStudentBinding

class AssignStudentDialog : DialogFragment() {
    private var _binding: DialogAssignStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var assignStudentListAdapter: AssignStudentListAdapter
    private lateinit var materialProgress: MaterialProgress

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
//        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.rvStudent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudent.itemAnimator = DefaultItemAnimator()
        binding.rvStudent.setHasFixedSize(true)
        binding.ivCross.setOnClickListener {
            dismiss()
        }
        bindStudentList()
    }

    private fun bindStudentList() {
        val studentsList: ArrayList<AssignStudentModel> = ArrayList()
        ComptutorApplication.studentsList.forEach {
            val item = AssignStudentModel(
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                lrn = it.lrn,
                studentId = it.studentId,
                isSelected = false,
            )
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
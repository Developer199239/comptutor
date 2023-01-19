package com.example.comptutor.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.comptutor.R

class AssignStudentListAdapter(
    private var studentList: List<AssignStudentModel>
) :
    RecyclerView.Adapter<AssignStudentListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.assing_student_item, parent, false)
        )

    override fun getItemCount() = studentList.size

    override fun onBindViewHolder(holder: AssignStudentListAdapter.ViewHolder, position: Int) =
        holder.bind(studentList[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.tvName)
        private val tvEmail = itemView.findViewById<TextView>(R.id.tvEmail)
        private val checkBox = itemView.findViewById<TextView>(R.id.checkBox)

        @SuppressLint("SetJavaScriptEnabled")
        fun bind(studentModel: AssignStudentModel) {
            name.text = "${studentModel.firstName} ${studentModel.lastName}"
            tvEmail.text = studentModel.email
            itemView.setOnClickListener {
            }

        }
    }
}
package com.example.comptutor.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.comptutor.R


class StudentListAdapter(
    private var studentList: List<StudentModel>,
    private var activity: Activity
) :
    RecyclerView.Adapter<StudentListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.student_item, parent, false)
        )

    override fun getItemCount() = studentList.size

    override fun onBindViewHolder(holder: StudentListAdapter.ViewHolder, position: Int) =
        holder.bind(studentList[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.tvName)
        private val tvEmail = itemView.findViewById<TextView>(R.id.tvEmail)

        @SuppressLint("SetJavaScriptEnabled")
        fun bind(studentModel: StudentModel) {
            name.text = "${studentModel.firstName} ${studentModel.lastName}"
            tvEmail.text = studentModel.email
            itemView.setOnClickListener {
            }
        }
    }
}
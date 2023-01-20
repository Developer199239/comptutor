package com.example.comptutor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comptutor.databinding.ActivityAddClassBinding
import com.example.comptutor.databinding.ActivityClassHomeBinding
import com.example.comptutor.utils.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ClassHomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityClassHomeBinding
    private lateinit var studentListAdapter: StudentListAdapter
    private lateinit var materialProgress: MaterialProgress
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
    }

    override fun onResume() {
        super.onResume()
        fetchStudentList()
    }

    private fun initView(){
        databaseReference = FirebaseDatabase.getInstance().reference
        materialProgress = MaterialProgress(this)
        binding.rvStudent.layoutManager = LinearLayoutManager(this)
        binding.rvStudent.itemAnimator = DefaultItemAnimator()
        binding.rvStudent.setHasFixedSize(true)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivUser.setOnClickListener {
            val dialog = AssignStudentDialog.newInstance()
            dialog.isCancelable = false
            dialog.show(
                supportFragmentManager,
                AssignStudentDialog::class.java.canonicalName
            )
        }

        binding.ivVideo.setOnClickListener {
            val dialog = EmbeddVideoLinkDialog.newInstance()
            dialog.isCancelable = false
            dialog.show(
                supportFragmentManager,
                EmbeddVideoLinkDialog::class.java.canonicalName
            )
        }
    }

    private fun fetchStudentList(){
        materialProgress.show()
        val mFireStore = FirebaseFirestore.getInstance()
        mFireStore.collection("users")
            .get()
            .addOnSuccessListener { document ->
                val studentsList: ArrayList<StudentModel> = ArrayList()
                for (i in document.documents) {
                    val product = i.toObject(StudentModel::class.java)!!
                    if(product.role == "student") {
                        product.userId = i.id
                        studentsList.add(product)
                    }
                }
                materialProgress.dismiss()
                studentListAdapter = StudentListAdapter(studentsList,this)
                binding.rvStudent.adapter = studentListAdapter
                studentListAdapter.notifyDataSetChanged()
                ComptutorApplication.studentsList = studentsList
            }
            .addOnFailureListener { e ->
                materialProgress.dismiss()
                e.message!!.toast(this)
                studentListAdapter = StudentListAdapter(arrayListOf(),this)
                binding.rvStudent.adapter = studentListAdapter
                studentListAdapter.notifyDataSetChanged()
                ComptutorApplication.studentsList.clear()
            }
    }
}
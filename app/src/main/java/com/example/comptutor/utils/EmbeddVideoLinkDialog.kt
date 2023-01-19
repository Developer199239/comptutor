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
import com.example.comptutor.databinding.DialogEmbeddedVideoLinkBinding
import com.google.firebase.database.*

class EmbeddVideoLinkDialog : DialogFragment() {
    private var _binding: DialogEmbeddedVideoLinkBinding? = null
    private val binding get() = _binding!!
    private lateinit var materialProgress: MaterialProgress
    private lateinit var sessionHelper: SessionHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEmbeddedVideoLinkBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        sessionHelper = SessionHelper(requireContext())
        materialProgress = MaterialProgress(requireActivity())

        binding.ivCross.setOnClickListener {
            dismiss()
        }
        binding.ivSave.setOnClickListener {
            val videoLink = binding.etLink.text.toString()
            if(videoLink.isEmpty()) {
                "Insert video link".toast(requireContext())
                return@setOnClickListener
            }
            val videoLinkModel = VideoLinkModel(videoLink = videoLink)
            materialProgress.show()
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child(AppConstants.VIDEO_LINK_TABLE).child(sessionHelper.getStringValue(SessionHelper.USER_ID)).setValue(videoLinkModel).addOnSuccessListener {
                materialProgress.dismiss()
                "Success".toast(requireContext())
                dismiss()
            }.addOnFailureListener {
                materialProgress.dismiss()
                "Failed due to: ${it.message}".toast(requireContext())
            }
        }
        bindVideoLink()
    }

    private fun bindVideoLink() {
        materialProgress.show()
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(AppConstants.VIDEO_LINK_TABLE)
            .child(sessionHelper.getStringValue(SessionHelper.USER_ID))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materialProgress.dismiss()
                    if(snapshot.value != null) {
                        val videoLinkModel = snapshot.getValue(VideoLinkModel::class.java)!!
                        binding.etLink.setText(videoLinkModel.videoLink)
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

    companion object {
        @JvmStatic
        fun newInstance(
        ) =
            EmbeddVideoLinkDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
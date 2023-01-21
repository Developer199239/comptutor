package com.example.comptutor.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comptutor.R
import com.example.comptutor.databinding.DialogNotificationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                            if(view.id == R.id.btnSendCode) {

                            } else if(view.id == R.id.btnRemove) {

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
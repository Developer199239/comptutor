package com.example.comptutor.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.comptutor.R
import com.github.dhaval2404.colorpicker.util.setVisibility
import com.google.gson.Gson


class NotificationListAdapter(
    private var pushNotificationList: List<PushInfoModel>,
    private var onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        )

    override fun getItemCount() = pushNotificationList.size

    override fun onBindViewHolder(holder: NotificationListAdapter.ViewHolder, position: Int) =
        holder.bind(pushNotificationList[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotification = itemView.findViewById<TextView>(R.id.tvNotification)
        private val btnSendCode = itemView.findViewById<Button>(R.id.btnSendCode)
        private val btnRemove = itemView.findViewById<Button>(R.id.btnRemove)
        private val btnAccessPermission = itemView.findViewById<Button>(R.id.btnAccessPermission)

        @SuppressLint("SetJavaScriptEnabled")
        fun bind(pushInfoModel: PushInfoModel) {
               if(pushInfoModel.pushType == AppConstants.PUSH_TYPE_REQUEST_CODE) {
                   val model = Gson().fromJson(pushInfoModel.pushBody, StudentModel::class.java)
                   tvNotification.text = "${model.firstName} request for code"
                   btnSendCode.setOnClickListener {
                       onItemClickListener.onItemClick(it, pushInfoModel, position)
                   }
                   btnRemove.setOnClickListener {
                       onItemClickListener.onItemClick(it, pushInfoModel, position)
                   }
                   btnSendCode.visibility = View.VISIBLE
               } else if(pushInfoModel.pushType == AppConstants.PUSH_TYPE_ACCESS_PERMISSION) {
                   val model = Gson().fromJson(pushInfoModel.pushBody, StudentModel::class.java)
                   tvNotification.text = "${model.firstName} request for video access"
                   btnAccessPermission.visibility = View.VISIBLE
                   btnAccessPermission.setOnClickListener {
                       onItemClickListener.onItemClick(it, pushInfoModel, position)
                   }
                   btnRemove.setOnClickListener {
                       onItemClickListener.onItemClick(it, pushInfoModel, position)
                   }
               }

        }
    }
}
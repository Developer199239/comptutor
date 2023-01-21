package com.example.comptutor.utils

class PushInfoModel(
    var pushType: String = "",
    var pushBody: String = "",
    var notificationId: String = "",
)

class PushNotificationResultSet(
    var result : ArrayList<PushInfoModel> = arrayListOf()
)

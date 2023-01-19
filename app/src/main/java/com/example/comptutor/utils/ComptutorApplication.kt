package com.example.comptutor.utils

import android.app.Application


class ComptutorApplication : Application(){
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        var classModel: ClassModel = ClassModel()
        var studentsList: ArrayList<StudentModel> = ArrayList()
    }
}
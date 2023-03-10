package com.example.comptutor.utils

class AssignStudentModel(
    var email: String = "",
    var firstName: String = "",
    var isStudent: String = "0",
    var lastName: String = "",
    var lrn: String = "",
    var userId: String = "",
    var isSelected: Boolean = false,
)

class AssignStudentResultSet(
    var result : ArrayList<AssignStudentModel> = arrayListOf()
)

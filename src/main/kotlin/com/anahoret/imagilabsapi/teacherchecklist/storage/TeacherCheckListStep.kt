package com.anahoret.imagilabsapi.teacherchecklist.storage

enum class TeacherCheckListStep(val clevertapName: String) {
    COMPLETE_YOUR_ACCOUNT_INFORMATION("completeAccountInformation"),
    CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM("createOrJoinClassroom"),
    SHARE_STUDENT_ACCESS_CODE("shareStudentCredentials"),
    EXPLORE_YOUR_FIRST_LESSON("exploreLesson"),
    CREATE_YOUR_FIRST_PROJECT("createProject"),
    CHECK_OUT_OUR_EDUCATOR_FACEBOOK_GROUP("checkFacebookGroup"),

    CONGRATULATION_DIALOG_SHOWN("")
}

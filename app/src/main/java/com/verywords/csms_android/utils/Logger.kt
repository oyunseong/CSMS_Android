package com.verywords.csms_android.utils

import android.util.Log


fun String.log(tag: String = "fastLog") {
    Log.d(tag, this)
}

fun log(tag: String = "fastLog", message: String) {
    Log.d(tag, message)
}
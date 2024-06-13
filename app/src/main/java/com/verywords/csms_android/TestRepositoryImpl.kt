package com.verywords.csms_android

import android.util.Log
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor() : TestRepository {

    @Inject
    override fun test() {
        Log.d("TestRepositoryImpl", "test)")
    }

}
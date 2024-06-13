package com.verywords.csms_android

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    testRepository: TestRepository
) : ViewModel() {
    init {
        testRepository.test()
    }
}
package com.verywords.csms_android

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.verywords.csms_android.ui.screen.home.HomeScreen

// TODO 네비게이션 기능 필요시 추가 예정.
@Composable
fun NavHostScreen(
    modifier: Modifier = Modifier
) {
    HomeScreen(modifier=modifier)
}
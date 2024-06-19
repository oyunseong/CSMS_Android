package com.verywords.csms_android.ui.screen.home

import com.verywords.csms_android.feat.model.DeviceInfo


sealed class HomeUiEvent {
    data class ToggleConnectDevice(val deviceInfo: DeviceInfo) : HomeUiEvent()
    data class ChangeDeviceInfoState(val deviceInfo: DeviceInfo) : HomeUiEvent()
    data class SendMessage(val deviceInfo: DeviceInfo) : HomeUiEvent()
    data object Refresh : HomeUiEvent()
}

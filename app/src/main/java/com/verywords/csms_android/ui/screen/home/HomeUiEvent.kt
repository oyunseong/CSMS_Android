package com.verywords.csms_android.ui.screen.home

import com.verywords.csms_android.feat.model.SerialDevice


sealed interface HomeUiEvent {
    data class ToggleConnectDevice(val serialDevice: SerialDevice) : HomeUiEvent
    data class ChangeDeviceInfoState(val serialDevice: SerialDevice) : HomeUiEvent
    data class SendMessage(val serialDevice: SerialDevice) : HomeUiEvent
    data object Refresh : HomeUiEvent
}

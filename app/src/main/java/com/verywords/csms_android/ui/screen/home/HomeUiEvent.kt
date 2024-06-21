package com.verywords.csms_android.ui.screen.home

import com.verywords.csms_android.core.serial.model.SerialDevice


sealed interface HomeUiEvent {
    data class ToggleConnectDevice(val serialDevice: SerialDevice) : HomeUiEvent
    data class UpdateDeviceState(val serialDevice: SerialDevice) : HomeUiEvent
    data class SendMessage(val serialDevice: SerialDevice) : HomeUiEvent
    data object Refresh : HomeUiEvent
    data class Navigation(val route: String) : HomeUiEvent
}

package com.verywords.csms_android.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialPort.Parity
import com.verywords.csms_android.core.serial.model.SerialDevice
import com.verywords.csms_android.ui.common.ChatTextField
import com.verywords.csms_android.ui.common.HorizontalSpacer
import com.verywords.csms_android.ui.common.VerticalSpacer
import com.verywords.csms_android.ui.theme.ableButton
import com.verywords.csms_android.ui.theme.disableButton
import com.verywords.csms_android.ui.theme.disableColor
import com.verywords.csms_android.ui.theme.mainColor

sealed interface DeviceItemUiEvent {
    data object ToggleConnectEvent : DeviceItemUiEvent
    data object OnUiParity : DeviceItemUiEvent
    data class OnUiBaudRate(val baudRate: Int) : DeviceItemUiEvent
    data object OnUiSendMessage : DeviceItemUiEvent
    data class ChangeInputText(val text: String) : DeviceItemUiEvent
}

@Composable
fun ConnectableDeviceList(
    serialDeviceList: List<SerialDevice>,
    onClick: (serialDevice: SerialDevice) -> Unit,
    onBaudRateSelected: (SerialDevice) -> Unit,
    sendMessage: (SerialDevice) -> Unit,
    onClickParity: (SerialDevice) -> Unit,
    onChangeInputText: (SerialDevice) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { VerticalSpacer(dp = 8.dp) }
        items(serialDeviceList.size) { index ->
            val currentDevice = serialDeviceList[index]
            val serialNumber =
                if (currentDevice.isConnected) currentDevice.device.serialNumber else ""

            DeviceItem(
                name = "${currentDevice.device.deviceName} : $serialNumber",
                isConnected = currentDevice.isConnected,
                baundRate = currentDevice.baudRate,
                inputText = currentDevice.inputText,
                parity = currentDevice.parity,
                uiEvent = {
                    when (it) {
                        is DeviceItemUiEvent.OnUiBaudRate -> {
                            onBaudRateSelected.invoke(
                                currentDevice.copy(
                                    baudRate = it.baudRate
                                )
                            )
                        }

                        DeviceItemUiEvent.ToggleConnectEvent -> {
                            onClick.invoke(currentDevice)
                        }

                        DeviceItemUiEvent.OnUiParity -> {
                            onClickParity.invoke(
                                currentDevice.copy(
                                    parity = if (currentDevice.parity == UsbSerialPort.PARITY_NONE) {
                                        UsbSerialPort.PARITY_EVEN
                                    } else {
                                        UsbSerialPort.PARITY_NONE
                                    }
                                )
                            )
                        }

                        is DeviceItemUiEvent.OnUiSendMessage -> {
                            sendMessage.invoke(currentDevice)
                        }

                        is DeviceItemUiEvent.ChangeInputText -> {
                            onChangeInputText.invoke(
                                currentDevice.copy(
                                    inputText = it.text
                                )
                            )
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            VerticalSpacer(dp = 100.dp)
        }
    }
}

@Composable
fun DeviceItem(
    name: String,
    isConnected: Boolean,
    @Parity parity: Int,
    baundRate: Int = 9600,
    inputText: String = "",
    uiEvent: (DeviceItemUiEvent) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) mainColor else disableColor
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = name,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ableButton,
                ),
                shape = RoundedCornerShape(6.dp),
                onClick = { uiEvent.invoke(DeviceItemUiEvent.ToggleConnectEvent) }) {
                Text(
                    text = "connect : $isConnected",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChatTextField(
                        modifier = Modifier.weight(1f),
                        value = inputText,
                        hintText = "Enter Ascii Char",
                        onValueChange = {
                            uiEvent.invoke(DeviceItemUiEvent.ChangeInputText(text = it))
                        }
                    )
                    HorizontalSpacer(dp = 8.dp)
                    Button(
                        onClick = {
                            uiEvent.invoke(DeviceItemUiEvent.OnUiSendMessage)
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ableButton,
                        ),
                    ) {
                        Text(text = "Send")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
            ) {
                listOf(9600, 19200, 115200).forEach { baudRate ->
                    Button(
                        modifier = Modifier,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (baundRate == baudRate) ableButton else disableButton,
                        ),
                        shape = RoundedCornerShape(6.dp),
                        onClick = {
                            if (!isConnected) {
                                uiEvent.invoke(DeviceItemUiEvent.OnUiBaudRate(baudRate))
                            }
                        },
                    ) {
                        Text(text = baudRate.toString(), color = Color.White)
                    }
                    HorizontalSpacer(dp = 8.dp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = Color.Black)
                )
                HorizontalSpacer(dp = 8.dp)
                Button(
                    modifier = Modifier,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isConnected) ableButton else disableButton,
                    ),
                    shape = RoundedCornerShape(6.dp),
                    onClick = {
                        if (!isConnected) {
                            uiEvent.invoke(DeviceItemUiEvent.OnUiParity)
                        }
                    },
                ) {
                    Text(
                        text = when (parity) {
                            UsbSerialPort.PARITY_NONE -> "None"
                            UsbSerialPort.PARITY_EVEN -> "Even"
                            else -> "unKnown"
                        }, color = Color.White

                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDeviceItem() {
    Column {
        DeviceItem(
            name = "이름",
            isConnected = false,
            parity = UsbSerialPort.PARITY_NONE
        )
        VerticalSpacer(dp = 12.dp)
        DeviceItem(
            name = "이름",
            isConnected = true,
            parity = UsbSerialPort.PARITY_EVEN
        )
    }
}
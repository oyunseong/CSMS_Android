package com.verywords.csms_android.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialPort.Parity
import com.verywords.csms_android.feat.model.DeviceInfo
import com.verywords.csms_android.ui.common.HorizontalSpacer
import com.verywords.csms_android.ui.common.VerticalSpacer
import com.verywords.csms_android.ui.theme.ableButton
import com.verywords.csms_android.ui.theme.disableButton
import com.verywords.csms_android.ui.theme.disableColor
import com.verywords.csms_android.ui.theme.mainColor

@Composable
fun ConnectableDeviceList(
    deviceInfoList: List<DeviceInfo>,
    onClick: (deviceInfo: DeviceInfo) -> Unit,
    onBaudRateSelected: (DeviceInfo) -> Unit,
    sendMessage: (DeviceInfo) -> Unit,
    onClickParity: (DeviceInfo) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { VerticalSpacer(dp = 8.dp) }
        items(deviceInfoList.size) { index ->
            val currentDevice = deviceInfoList[index]
            DeviceItem(
                name = currentDevice.device.deviceName,
                isConnected = currentDevice.isConnected,
                onClick = {
                    onClick.invoke(currentDevice)
                },
                onBaudRateSelected = {
                    onBaudRateSelected.invoke(
                        currentDevice.copy(
                            baudRate = it
                        )
                    )
                },
                sendMessage = {
                    sendMessage.invoke(deviceInfoList[index])
                },
                onClickParity = {
                    onClickParity.invoke(
                        currentDevice.copy(
                            parity = if (currentDevice.parity == UsbSerialPort.PARITY_NONE) {
                                UsbSerialPort.PARITY_EVEN
                            } else {
                                UsbSerialPort.PARITY_NONE
                            }

                        )
                    )
                },
                parity = currentDevice.parity
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DeviceItem(
    name: String,
    isConnected: Boolean,
    @Parity parity: Int,
    onClick: () -> Unit = {},
    onBaudRateSelected: (Int) -> Unit = {},
    sendMessage: () -> Unit = {},
    onClickParity: () -> Unit = {},
) {
    var selectedBaudRate by remember { mutableStateOf(9600) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick.invoke()
            },
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
            Text(
                text = "connect : $isConnected",
                fontSize = 16.sp,
                color = if (isConnected) Color.Black else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isConnected) {
                Button(
                    onClick = { sendMessage.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ableButton,
                    ),
                ) {
                    Text(text = "메시지 전송")
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
                            containerColor = if (selectedBaudRate == baudRate) ableButton else disableButton,
                        ),
                        shape = RoundedCornerShape(6.dp),
                        onClick = {
                            if (!isConnected) {
                                selectedBaudRate = baudRate
                                onBaudRateSelected(baudRate)
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
                            onClickParity.invoke()
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
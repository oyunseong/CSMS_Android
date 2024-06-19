package com.verywords.csms_android.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.verywords.csms_android.R
import com.verywords.csms_android.feat.model.DeviceInfo

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.serialManager.deviceState.collectAsState()
    var isVisibleMessageScreen by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()

    Box(modifier = modifier) {
        HomeScreenContent(
            modifier = Modifier,
            deviceInfoList = uiState,
            uiEvent = {
                when (it) {
                    is HomeUiEvent.ToggleConnectDevice -> {
                        viewModel.toggleConnectDevice(it.deviceInfo)
                    }

                    is HomeUiEvent.ChangeDeviceInfoState -> {
                        viewModel.updateDeviceInfo(it.deviceInfo)
                    }

                    is HomeUiEvent.SendMessage -> {
                        viewModel.sendMessage(it.deviceInfo)
                    }

                    HomeUiEvent.Refresh -> {
                        viewModel.searchDevice()
                    }
                }
            },
        )

        if (isVisibleMessageScreen) {
            MessageScreen(
                modifier = Modifier,
                messages = messages,
                onClearMessages = {
                    viewModel.onClearMessages()
                }
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .size(80.dp),
            onClick = {
                isVisibleMessageScreen = !isVisibleMessageScreen
            }) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(45.dp),
                painter = painterResource(
                    id = if (isVisibleMessageScreen) R.drawable.baseline_usb_24
                    else R.drawable.baseline_message_24
                ),
                contentDescription = ""
            )
        }
    }
}



@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    deviceInfoList: List<DeviceInfo> = emptyList(),
    uiEvent: (HomeUiEvent) -> Unit = {},
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (deviceInfoList.isEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                text = "연결 가능한 기기 없음"
            )
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "기기 목록",
                    fontSize = 24.sp
                )
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = { uiEvent.invoke(HomeUiEvent.Refresh) }) {
                    Text(text = "refresh")
                }
            }

            Box(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(Color.Gray)
            )
            ConnectableDeviceList(
                deviceInfoList = deviceInfoList,
                onClick = {
                    uiEvent.invoke(HomeUiEvent.ToggleConnectDevice(it))
                },
                onBaudRateSelected = {
                    uiEvent.invoke(HomeUiEvent.ChangeDeviceInfoState(it))
                },
                sendMessage = {
                    uiEvent.invoke(HomeUiEvent.SendMessage(it))
                },
                onChangeInputText = {
                    uiEvent.invoke(HomeUiEvent.ChangeDeviceInfoState(it))
                },
                onClickParity = {
                    uiEvent.invoke(HomeUiEvent.ChangeDeviceInfoState(it))
                },
            )
        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        deviceInfoList = listOf()
    )
}
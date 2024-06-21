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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.verywords.csms_android.Navigation
import com.verywords.csms_android.R
import com.verywords.csms_android.core.serial.model.SerialDevice
import com.verywords.csms_android.ui.common.VerticalSpacer

@Composable
fun HomeScreen(
    modifier: Modifier,
    navigate: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.serialManager.deviceState.collectAsState()
    var isVisibleMessageScreen by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()

    Box(modifier = modifier) {
        HomeScreenContent(
            modifier = Modifier.background(color = Color.White),
            serialDeviceList = uiState,
            uiEvent = {
                when (it) {
                    is HomeUiEvent.ToggleConnectDevice -> {
                        viewModel.toggleDeviceConnection(it.serialDevice)
                    }

                    is HomeUiEvent.UpdateDeviceState -> {
                        viewModel.updateDeviceState(it.serialDevice)
                    }

                    is HomeUiEvent.SendMessage -> {
                        viewModel.sendAsciiMessage(it.serialDevice)
                    }

                    HomeUiEvent.Refresh -> {
                        viewModel.searchDevice()
                    }

                    is HomeUiEvent.Navigation -> {
                        navigate.invoke(it.route)
                    }
                }
            },
        )

        if (isVisibleMessageScreen) {
            MessageScreen(
                modifier = Modifier,
                messages = messages,
                onClearMessages = {
                    viewModel.clearReceivedMessages()
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
    serialDeviceList: List<SerialDevice> = emptyList(),
    uiEvent: (HomeUiEvent) -> Unit = {},
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (serialDeviceList.isEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                text = "Not found device"
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
                    text = "Device List",
                    fontSize = 24.sp
                )
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Button(
                        onClick = { uiEvent.invoke(HomeUiEvent.Refresh) }) {
                        Text(text = "refresh")
                    }
                    VerticalSpacer(dp = 2.dp)
                    Button(onClick = {
                        uiEvent.invoke(HomeUiEvent.Navigation(Navigation.Routes.DatabaseScreen))
                    }) {
                        Text(text = "이동")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .height(2.dp)
                    .background(Color.Gray)
            )
            ConnectableDeviceList(
                serialDeviceList = serialDeviceList,
                onClick = {
                    uiEvent.invoke(HomeUiEvent.ToggleConnectDevice(it))
                },
                onBaudRateSelected = {
                    uiEvent.invoke(HomeUiEvent.UpdateDeviceState(it))
                },
                sendMessage = {
                    uiEvent.invoke(HomeUiEvent.SendMessage(it))
                },
                onChangeInputText = {
                    uiEvent.invoke(HomeUiEvent.UpdateDeviceState(it))
                },
                onClickParity = {
                    uiEvent.invoke(HomeUiEvent.UpdateDeviceState(it))
                },
            )
        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        serialDeviceList = listOf()
    )
}
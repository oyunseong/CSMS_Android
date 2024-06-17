package com.verywords.csms_android.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.verywords.csms_android.feat.model.DeviceInfo
import com.verywords.csms_android.utils.log

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.serialManager.deviceState.collectAsState()
    val context = LocalContext.current
    var isVisibleMessageScreen by remember { mutableStateOf(false) }
    val messages = remember { mutableStateOf(uiState.flatMap { it.message }) }

    val message = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        viewModel.searchDevice(context = context)
    }

    LaunchedEffect(key1 = uiState.flatMap { it.message }) {
        messages.value = uiState.flatMap { it.message }
    }

    Box(modifier = modifier) {
        HomeScreenContent(
            modifier = Modifier,
            uiState = uiState,
            onClick = {
                log(message = "it.isConnected: ${it.isConnected}")
                if (it.isConnected) {   // 연결 돼 있으면 해제.
                    viewModel.disconnectDevice(it)
                } else {
                    viewModel.connectDevice(context = context, deviceInfo = it)
                }
            },
        )

        if (isVisibleMessageScreen) {
            MessageScreen(
                modifier = modifier,
                messages = messages.value
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .size(45.dp),
            onClick = {
                isVisibleMessageScreen = !isVisibleMessageScreen
            }) {}
    }

}


@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: List<DeviceInfo> = emptyList(),
    onClick: (deviceInfo: DeviceInfo) -> Unit = {},
    onBaudRateSelected: (DeviceInfo) -> Unit = {}
) {
    Box(modifier = modifier) {
        Box(
            modifier = modifier, contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(uiState.size) { index ->
                    DeviceItem(deviceInfo = uiState[index], onClick = {
                        onClick.invoke(uiState[index])
                    }, onBaudRateSelected = {
                        onBaudRateSelected.invoke(
                            uiState[index].copy(
                                baudRate = it
                            )
                        )
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    deviceInfo: DeviceInfo, onClick: () -> Unit, onBaudRateSelected: (Int) -> Unit
) {
    var selectedBaudRate by remember { mutableStateOf(9600) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(color = if (deviceInfo.isConnected) Color.Yellow else Color.Gray)
                .clickable {
                    onClick.invoke()
                },
        ) {
            Text(
                text = deviceInfo.device.deviceName,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "connect : ${deviceInfo.isConnected}", fontSize = 16.sp, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()
            ) {
                listOf(9600, 19200, 115200).forEach { baudRate ->
                    Button(
                        modifier = Modifier.background(
                            if (selectedBaudRate == baudRate) Color.Blue else Color.Gray
                        ),
                        onClick = {
                            selectedBaudRate = baudRate
                            onBaudRateSelected(baudRate)
                        },
                    ) {
                        Text(text = baudRate.toString(), color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreenContent()
}
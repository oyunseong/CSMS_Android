package com.verywords.csms_android.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verywords.csms_android.feat.model.ReceiveData
import com.verywords.csms_android.ui.common.VerticalSpacer
import com.verywords.csms_android.ui.theme.mainColor
import com.verywords.csms_android.utils.convertMillisToDateTime


@Composable
fun MessageScreen(
    modifier: Modifier = Modifier,
    messages: List<ReceiveData>,
    onClearMessages: () -> Unit = {}
) {
    var isAutoScroll by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = messages.size) {
        if (messages.isNotEmpty() && isAutoScroll) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Receive Messages",
                    fontSize = 20.sp,
                )
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = "(${messages.size})",
                    fontSize = 20.sp,
                    color = mainColor
                )
            }
            Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                Button(
                    modifier = Modifier,
                    onClick = { isAutoScroll = !isAutoScroll }) {
                    Text(text = "Auto Scroll ${if (isAutoScroll) "ON" else "OFF"}")
                }
                Button(
                    modifier = Modifier,
                    onClick = { onClearMessages.invoke() }) {
                    Text(text = "clear")
                }
            }
        }
        VerticalSpacer(dp = 8.dp)
        Box(modifier = Modifier.fillMaxSize()) {
            if (messages.isEmpty()) {
                Text(
                    text = "No messages",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                items(messages.size) {
                    val message = messages[it]
                    MessageItem(
                        message = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (it % 2 == 0) Color.LightGray else Color.White
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: ReceiveData
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Time: ${convertMillisToDateTime(message.time)}",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = "Size: ${message.size}",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = message.data,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

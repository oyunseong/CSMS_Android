package com.verywords.csms_android.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verywords.csms_android.feat.model.ReceiveData
import com.verywords.csms_android.ui.common.VerticalSpacer
import com.verywords.csms_android.utils.convertMillisToDateTime


@Composable
fun MessageScreen(
    modifier: Modifier = Modifier,
    messages: List<ReceiveData>
) {
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "Receive Messages",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
        VerticalSpacer(dp = 8.dp)
        LazyColumn(
            modifier = Modifier.weight(1f),
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

@Composable
fun MessageItem(
    message: ReceiveData,
    modifier: Modifier = Modifier
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

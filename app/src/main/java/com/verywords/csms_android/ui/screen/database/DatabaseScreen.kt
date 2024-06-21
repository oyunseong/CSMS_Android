package com.verywords.csms_android.ui.screen.database

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.verywords.csms_android.ui.common.VerticalSpacer

@Composable
fun DatabaseScreen(
    modifier: Modifier = Modifier,
    viewModel: DatabaseScreenViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Yellow)
    ) {
        Column {
            messages.forEach {
                Text(
                    modifier = Modifier.clickable {
                        viewModel.sendEventMessage(it)
                    },
                    text = "id : ${it.id} / name : ${it.message} / error : ${it.errorMessage}\ntime : ${it.createAt}"
                )
                VerticalSpacer(dp = 2.dp)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Button(onClick = { viewModel.insertMessageToDatabase() }) {
                Text(text = "Save Message in Room")
            }
            VerticalSpacer(dp = 4.dp)
            Button(onClick = { viewModel.clearMessages() }) {
                Text(text = "clear")
            }
        }
    }
}
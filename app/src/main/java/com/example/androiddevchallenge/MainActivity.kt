/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    Surface(modifier = Modifier.fillMaxSize(1f), color = MaterialTheme.colors.background) {
        Counter()
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}

@Composable
fun Counter(mainViewModel: MainViewModel = viewModel()) {
    val minutes: Int by mainViewModel.minutes.observeAsState(0)
    val seconds: Int by mainViewModel.seconds.observeAsState(0)
    val count: Int by mainViewModel.time.observeAsState(0)
    val state: MainViewModel.TimerProgress by mainViewModel.timerProgress.observeAsState(
        MainViewModel.TimerProgress.SETTING
    )

    val transition = updateTransition(state)
    val angleOffset by transition.animateFloat(
        transitionSpec = {
            tween(
                delayMillis = 0,
                durationMillis = mainViewModel.maxTime * 1000,
                easing = LinearEasing
            )
        }
    ) { progress ->
        if (progress == MainViewModel.TimerProgress.SETTING) {
            360f
        } else {
            0f
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            NumberSetting(
                modifier = Modifier.weight(0.5f),
                number = minutes,
                onIncrement = { mainViewModel.incrementMinutes() },
                onDecrement = { mainViewModel.decrementMinutes() }
            )
            NumberSetting(
                modifier = Modifier.weight(0.5f),
                number = seconds,
                onIncrement = { mainViewModel.incrementSeconds() },
                onDecrement = { mainViewModel.decrementSeconds() }
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Button(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(32.dp),
                onClick = {
                    mainViewModel.startTimer()
                }
            ) {
                Text(text = "start")
            }
            Button(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(32.dp),
                onClick = {
                    mainViewModel.stopTimer()
                }
            ) {
                Text(text = "stop")
            }

            Button(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(32.dp),
                onClick = {
                    mainViewModel.clearTimer()
                }
            ) {
                Text(text = "clear")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,

        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                val middle =
                    Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = Color.Blue,
                    center = middle,
                    radius = size.minDimension / 2,
                    style = Stroke(8.dp.toPx()),
                )
                drawArc(
                    Color.Red,
                    270f,
                    angleOffset,
                    useCenter = true,
                    size = Size(size.minDimension, size.minDimension),
                    topLeft = Offset(
                        abs((size.minDimension / 2) - (size.width / 2)),
                        abs((size.minDimension / 2) - (size.height / 2))
                    ),
                    style = Stroke(8.dp.toPx())
                )
            }
            val timeText = String.format("%02d:%02d", count / 60, count % 60)
            Text(
                text = timeText,
                modifier = Modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 16.dp),
                style = TextStyle(fontSize = 80.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NumberSetting(
    modifier: Modifier,
//    enabled: Boolean,
    number: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val time = String.format("%02d", number)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        IconButton(
            onClick = { onIncrement() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExpandLess, "less")
        }

        Text(
            text = time,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 64.sp)
        )
        IconButton(
            onClick = { onDecrement() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExpandMore, "more")
        }
    }
}

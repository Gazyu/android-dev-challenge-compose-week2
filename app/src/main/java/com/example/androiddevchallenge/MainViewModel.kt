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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    enum class TimerProgress { SETTING, COUNTDOWN }

    private val _timerProgress = MutableLiveData(TimerProgress.SETTING)
    val timerProgress: LiveData<TimerProgress> = _timerProgress

    private val sharedFlow = MutableSharedFlow<Int>()

    val time: LiveData<Int> = sharedFlow.asLiveData()

    var maxTime: Int = 0

    private val _minutes = MutableLiveData(0)
    val minutes: LiveData<Int> = _minutes

    private val _seconds = MutableLiveData(0)
    val seconds: LiveData<Int> = _seconds

    init {

        viewModelScope.launch {
            sharedFlow.sample(1000L).collect {
                if (it == 0) {
                    stopTimer()
                    job?.cancel()
                    return@collect
                }
                sharedFlow.emit(it - 1)
            }
        }
    }

    fun incrementMinutes() {
        if (_timerProgress.value == TimerProgress.COUNTDOWN) return
        _minutes.value = _minutes.value?.plus(1)
    }

    fun decrementMinutes() {
        if (_timerProgress.value == TimerProgress.COUNTDOWN) return
        if (_minutes.value!! == 0) {
            return
        }
        _minutes.value = _minutes.value?.minus(1)
    }

    fun incrementSeconds() {
        if (_timerProgress.value == TimerProgress.COUNTDOWN) return
        if (_seconds.value == 59) {
            _seconds.value = 0
            incrementMinutes()
            return
        }
        _seconds.value = _seconds.value?.plus(1)
    }

    fun decrementSeconds() {
        if (_timerProgress.value == TimerProgress.COUNTDOWN) return
        if (_seconds.value!! == 0) {
            return
        }
        _seconds.value = _seconds.value?.minus(1)
    }

    private var job: Job? = null

    fun startTimer() {
        if (_timerProgress.value == TimerProgress.COUNTDOWN) return
        val count = _minutes.value!! * 60 + _seconds.value!!
        if (count == 0) {
            return
        }
        _timerProgress.value = TimerProgress.COUNTDOWN
        maxTime = count
        job = viewModelScope.launch(Dispatchers.IO) {
            sharedFlow.emit(count)
        }
    }

    fun stopTimer() {
        maxTime = 0
        _timerProgress.value = TimerProgress.SETTING
        job?.cancel()
        viewModelScope.launch {
            sharedFlow.emit(0)
        }
    }

    fun clearTimer() {
        stopTimer()
        _seconds.value = 0
        _minutes.value = 0
    }
}

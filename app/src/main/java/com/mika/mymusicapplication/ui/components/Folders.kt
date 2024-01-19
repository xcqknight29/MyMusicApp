package com.mika.mymusicapplication.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

val testViewModel: TestViewModel = TestViewModel()

@Composable
fun Folders(modifier: Modifier = Modifier) {
    val testStr: String by testViewModel.testStr.collectAsState()
    val textList: List<String> by testViewModel.testList.collectAsState()
    LazyColumn {
        item {
            Row {
                Button(
                    onClick = {
                        testViewModel.setTestList(textList.plus("new str"))
                    }
                ) {
                    Text(text = "add str")
                }
                Button(onClick = {
                    testViewModel.setTestStr("one str")
                }) {
                    Text(text = "change to other str")
                }
            }
        }
        items(textList) {
            Text(text = it)
        }
        item {
            Text(text = testStr)
        }
    }
}

class TestViewModel : ViewModel() {
    private val _testStr = MutableStateFlow("")
    val testStr: StateFlow<String> = _testStr.asStateFlow()
    private val _testList = MutableStateFlow(listOf<String>())
    val testList: StateFlow<List<String>> = _testList.asStateFlow()
    fun setTestStr(testStr: String) {
        _testStr.update { testStr }
    }
    fun setTestList(testList: List<String>) {
        _testList.update { testList }
    }
}
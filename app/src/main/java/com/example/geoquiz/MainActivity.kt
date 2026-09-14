package com.example.geoquiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Question(val text: String, val isTrue: Boolean)
val questionBank = listOf(
    Question("Canberra is the capital of Australia.", true),
    Question("The Pacific Ocean is larger than the Atlantic Ocean.", true),
    Question("The Suez Canal connects the Red Sea and the Indian Ocean.", false),
    Question("The source of the Nile River is in Egypt.", false),
    Question("The Amazon River is the longest river in the Americas.", true),
    Question("Lake Baikal is the world's oldest and deepest freshwater lake.", true)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GeoQuizApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GeoQuizApp(modifier: Modifier = Modifier) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableIntStateOf(0) } // Счетчик правильных ответов
    var showResultDialog by remember { mutableStateOf(false) } // Показывать ли финальную панель

    val context = LocalContext.current
    val isLastQuestion = currentIndex == questionBank.size - 1

    fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = questionBank[currentIndex].isTrue

        if (userAnswer == correctAnswer) {
            correctAnswersCount++
            Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Incorrect!", Toast.LENGTH_SHORT).show()
        }

        isAnswered = true

        // Если вопрос последний — показываем панель с результатом
        if (isLastQuestion) {
            showResultDialog = true
        }
    }

    // Всплывающая панель результатов (Требование 4)
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { /* Не даем закрыть кликом мимо */ },
            title = { Text("Результат теста") },
            text = { Text("Вы ответили правильно на $correctAnswersCount из ${questionBank.size} вопросов!") },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = questionBank[currentIndex].text, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 40.dp))

        if (!isAnswered) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { checkAnswer(true) }) { Text("TRUE") }
                Button(onClick = { checkAnswer(false) }) { Text("FALSE") }
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!(isLastQuestion && isAnswered)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = {
                    if (!isLastQuestion) {
                        currentIndex++
                        isAnswered = false
                    }
                }) { Text("NEXT >") }
            }
        }
    }
}
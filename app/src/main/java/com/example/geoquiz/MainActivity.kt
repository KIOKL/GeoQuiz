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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    @OptIn(ExperimentalMaterial3Api::class) // Нужно для TopAppBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // Добавляем красивую верхнюю панель из дизайна
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("GeoQuiz", color = Color.White, fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF6200EE) // Фиолетовый цвет как на скриншоте
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
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
    var correctAnswersCount by remember { mutableIntStateOf(0) }
    var showResultDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isLastQuestion = currentIndex == questionBank.size - 1

    // Настраиваем цвет кнопок под дизайн
    val buttonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))

    fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = questionBank[currentIndex].isTrue

        if (userAnswer == correctAnswer) {
            correctAnswersCount++
            Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Incorrect!", Toast.LENGTH_SHORT).show()
        }

        isAnswered = true

        if (isLastQuestion) {
            showResultDialog = true
        }
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Результат теста", fontWeight = FontWeight.Bold) },
            text = { Text("Правильных ответов: $correctAnswersCount из ${questionBank.size} 🏆") },
            confirmButton = {
                Button(onClick = { showResultDialog = false }, colors = buttonColors) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Сдвигаем всё чуть выше, как на макете
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = questionBank[currentIndex].text,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 60.dp)
        )

        if (!isAnswered) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { checkAnswer(true) }, colors = buttonColors) {
                    Text("TRUE", color = Color.White)
                }
                Button(onClick = { checkAnswer(false) }, colors = buttonColors) {
                    Text("FALSE", color = Color.White)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp)) // Сохраняем высоту кнопок
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (!(isLastQuestion && isAnswered)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = {
                    if (!isLastQuestion) {
                        currentIndex++
                        isAnswered = false
                    }
                }, colors = buttonColors) {
                    Text("NEXT >", color = Color.White)
                }
            }
        }
    }
}
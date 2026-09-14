package com.example.geoquiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("GeoQuiz", color = Color.White, fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EE))
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

    // Храним ответы: true (верно), false (ошибка), null (пропущено)
    val answers = remember { mutableStateListOf<Boolean?>().apply {
        repeat(questionBank.size) { add(null) }
    } }

    var showMissingDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val buttonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))

    // Получаем номера пропущенных вопросов (прибавляем 1 для удобного отображения пользователю)
    val skippedQuestions = answers.mapIndexedNotNull { index, answer ->
        if (answer == null) index + 1 else null
    }

    fun checkAnswer(userAnswer: Boolean) {
        val isCorrect = userAnswer == questionBank[currentIndex].isTrue
        answers[currentIndex] = isCorrect // Записываем результат

        Toast.makeText(context, if (isCorrect) "Correct!" else "Incorrect!", Toast.LENGTH_SHORT).show()
    }

    // --- ДИАЛОГ ПРОПУЩЕННЫХ ВОПРОСОВ ---
    if (showMissingDialog) {
        AlertDialog(
            onDismissRequest = { showMissingDialog = false },
            title = { Text("Есть пропущенные вопросы!", fontWeight = FontWeight.Bold) },
            text = {
                Text("Вы не дали ответ на вопросы: ${skippedQuestions.joinToString(", ")}.\n\nЗавершить тест (они будут засчитаны как неверные) или вернуться?")
            },
            confirmButton = {
                Button(onClick = {
                    showMissingDialog = false
                    // Перекидываем пользователя на первый пропущенный вопрос
                    currentIndex = skippedQuestions.first() - 1
                }, colors = buttonColors) {
                    Text("Вернуться", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMissingDialog = false
                    showResultDialog = true // Идем к результатам несмотря на пропуски
                }) {
                    Text("Завершить", color = Color(0xFF6200EE))
                }
            }
        )
    }

    // --- ДИАЛОГ ДЕТАЛЬНЫХ РЕЗУЛЬТАТОВ ---
    if (showResultDialog) {
        val correctCount = answers.count { it == true }
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Результат: $correctCount / ${questionBank.size} 🏆", fontWeight = FontWeight.Bold) },
            text = {
                // Scrollable колонка для подробной статистики
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    questionBank.indices.forEach { i ->
                        val status = when (answers[i]) {
                            true -> "✅ Правильно"
                            false -> "❌ Ошибка"
                            null -> "⚪ Пропущен"
                        }
                        Text("Вопрос ${i + 1}: $status", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Сброс теста для новой попытки
                    answers.fill(null)
                    currentIndex = 0
                    showResultDialog = false
                }, colors = buttonColors) {
                    Text("Начать заново", color = Color.White)
                }
            }
        )
    }

    // --- ОСНОВНОЙ ЭКРАН ---
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Вопрос ${currentIndex + 1} из ${questionBank.size}",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = questionBank[currentIndex].text,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 60.dp)
        )

        // Показываем кнопки TRUE/FALSE, только если на этот вопрос еще нет ответа (answers[currentIndex] == null)
        if (answers[currentIndex] == null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { checkAnswer(true) }, colors = buttonColors) {
                    Text("TRUE", color = Color.White)
                }
                Button(onClick = { checkAnswer(false) }, colors = buttonColors) {
                    Text("FALSE", color = Color.White)
                }
            }
        } else {
            // Если ответ уже дан, показываем заглушку, чтобы интерфейс не дергался
            Spacer(modifier = Modifier.height(48.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- ПАНЕЛЬ НАВИГАЦИИ (PREV / NEXT / FINISH) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Кнопка PREV (прячем на первом вопросе)
            if (currentIndex > 0) {
                OutlinedButton(onClick = { currentIndex-- }) {
                    Text("< PREV", color = Color(0xFF6200EE))
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp)) // Пустое место для симметрии
            }

            // Кнопка NEXT или FINISH (в зависимости от того, последний ли это вопрос)
            if (currentIndex < questionBank.size - 1) {
                Button(onClick = { currentIndex++ }, colors = buttonColors) {
                    Text("NEXT >", color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        if (skippedQuestions.isNotEmpty()) {
                            showMissingDialog = true // Есть пропуски - предупреждаем
                        } else {
                            showResultDialog = true  // Всё решено - показываем итог
                        }
                    },
                    colors = buttonColors
                ) {
                    Text("FINISH", color = Color.White)
                }
            }
        }
    }
}
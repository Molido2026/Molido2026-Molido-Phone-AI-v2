package ir.molido.phoneai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import ir.molido.phoneai.ai.LocalBrain
import ir.molido.phoneai.data.LocalStore
import ir.molido.phoneai.model.AppLanguage
import ir.molido.phoneai.model.AppTheme
import ir.molido.phoneai.model.BrainAction
import ir.molido.phoneai.model.ChatMessage
import ir.molido.phoneai.model.Screen
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = LocalStore(applicationContext)

        setContent {
            MolidoApp(store)
        }
    }
}

@Composable
private fun MolidoApp(store: LocalStore) {
    var language by rememberSaveable {
        mutableStateOf(
            if (store.language() == "EN") AppLanguage.EN else AppLanguage.FA
        )
    }
    var theme by rememberSaveable {
        mutableStateOf(
            when (store.theme()) {
                "LIGHT" -> AppTheme.LIGHT
                "DARK" -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        )
    }
    var screen by rememberSaveable { mutableStateOf(Screen.CHAT) }
    val isDark = when (theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        MaterialTheme(
            colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = screen == Screen.CHAT,
                                onClick = { screen = Screen.CHAT },
                                icon = { Icon(Icons.Default.Chat, null) },
                                label = { Text(if (language == AppLanguage.FA) "گفتگو" else "Chat") }
                            )
                            NavigationBarItem(
                                selected = screen == Screen.MEMORY,
                                onClick = { screen = Screen.MEMORY },
                                icon = { Icon(Icons.Default.Lightbulb, null) },
                                label = { Text(if (language == AppLanguage.FA) "حافظه" else "Memory") }
                            )
                            NavigationBarItem(
                                selected = screen == Screen.NOTES,
                                onClick = { screen = Screen.NOTES },
                                icon = { Icon(Icons.Default.Note, null) },
                                label = { Text(if (language == AppLanguage.FA) "یادداشت" else "Notes") }
                            )
                            NavigationBarItem(
                                selected = screen == Screen.SETTINGS,
                                onClick = { screen = Screen.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, null) },
                                label = { Text(if (language == AppLanguage.FA) "تنظیمات" else "Settings") }
                            )
                        }
                    }
                ) { inner ->
                    when (screen) {
                        Screen.CHAT -> ChatScreen(
                            language = language,
                            store = store,
                            modifier = Modifier.padding(inner)
                        )
                        Screen.MEMORY -> ListScreen(
                            title = if (language == AppLanguage.FA) "حافظه محلی" else "Local Memory",
                            items = store.memories(),
                            empty = if (language == AppLanguage.FA) "هنوز چیزی ذخیره نشده است." else "Nothing saved yet.",
                            onClear = { store.clearMemories() },
                            language = language,
                            modifier = Modifier.padding(inner)
                        )
                        Screen.NOTES -> ListScreen(
                            title = if (language == AppLanguage.FA) "یادداشت‌های محلی" else "Local Notes",
                            items = store.notes(),
                            empty = if (language == AppLanguage.FA) "هنوز یادداشتی وجود ندارد." else "No notes yet.",
                            onClear = { store.clearNotes() },
                            language = language,
                            modifier = Modifier.padding(inner)
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            language = language,
                            theme = theme,
                            onLanguage = {
                                language = it
                                store.setLanguage(it.name)
                            },
                            onTheme = {
                                theme = it
                                store.setTheme(it.name)
                            },
                            modifier = Modifier.padding(inner)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    language: AppLanguage,
    store: LocalStore,
    modifier: Modifier = Modifier
) {
    val brain = remember { LocalBrain() }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                user = false,
                text = if (language == AppLanguage.FA)
                    "سلام! من دستیار محلی مولیدو هستم."
                else
                    "Hello! I am Molido's local assistant."
            )
        )
    }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "MOLIDO PHONE AI",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = if (language == AppLanguage.FA)
                "آفلاین • خصوصی • سبک"
            else
                "Offline • Private • Lightweight",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = input,
                onValueChange = { input = it },
                label = { Text(if (language == AppLanguage.FA) "پیام" else "Message") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (input.isNotBlank()) {
                            sendMessage(input, language, brain, store, messages)
                            input = ""
                        }
                    }
                )
            )
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        sendMessage(input, language, brain, store, messages)
                        input = ""
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text(if (language == AppLanguage.FA) "ارسال" else "Send")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { input = if (language == AppLanguage.FA) "کمک" else "help" },
                label = { Text(if (language == AppLanguage.FA) "کمک" else "Help") }
            )
            AssistChip(
                onClick = { input = if (language == AppLanguage.FA) "آفلاین" else "offline" },
                label = { Text(if (language == AppLanguage.FA) "آفلاین" else "Offline") }
            )
        }
    }
}

private fun sendMessage(
    input: String,
    language: AppLanguage,
    brain: LocalBrain,
    store: LocalStore,
    messages: MutableList<ChatMessage>
) {
    messages.add(ChatMessage(user = true, text = input))
    val reply = brain.reply(input, language)
    when (val action = reply.action) {
        is BrainAction.SaveMemory -> store.addMemory(action.value)
        is BrainAction.SaveNote -> store.addNote(action.value)
        BrainAction.None -> Unit
    }
    messages.add(ChatMessage(user = false, text = reply.text))
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.user) Arrangement.End else Arrangement.Start
    ) {
        Card {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ListScreen(
    title: String,
    items: List<String>,
    empty: String,
    onClear: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var visibleItems by remember(items) {
        mutableStateOf(items)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        if (visibleItems.isEmpty()) {
            Text(empty)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visibleItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(item, modifier = Modifier.padding(14.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = {
                onClear()
                visibleItems = emptyList()
            }
        ) {
            Text(if (language == AppLanguage.FA) "پاک کردن همه" else "Clear all")
        }
    }
}

@Composable
private fun SettingsScreen(
    language: AppLanguage,
    theme: AppTheme,
    onLanguage: (AppLanguage) -> Unit,
    onTheme: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (language == AppLanguage.FA) "تنظیمات" else "Settings",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(20.dp))

        Text(if (language == AppLanguage.FA) "زبان" else "Language")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = language == AppLanguage.FA,
                onClick = { onLanguage(AppLanguage.FA) },
                label = { Text("فارسی") }
            )
            FilterChip(
                selected = language == AppLanguage.EN,
                onClick = { onLanguage(AppLanguage.EN) },
                label = { Text("English") }
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(if (language == AppLanguage.FA) "پوسته" else "Theme")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = theme == AppTheme.SYSTEM,
                onClick = { onTheme(AppTheme.SYSTEM) },
                label = { Text(if (language == AppLanguage.FA) "سیستم" else "System") }
            )
            FilterChip(
                selected = theme == AppTheme.LIGHT,
                onClick = { onTheme(AppTheme.LIGHT) },
                label = { Text(if (language == AppLanguage.FA) "روشن" else "Light") }
            )
            FilterChip(
                selected = theme == AppTheme.DARK,
                onClick = { onTheme(AppTheme.DARK) },
                label = { Text(if (language == AppLanguage.FA) "تیره" else "Dark") }
            )
        }

        Spacer(Modifier.height(28.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "MOLIDO PHONE AI 2.0.0",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.FA)
                        "نسخه پایه هیچ اتصال اینترنتی یا API خارجی ندارد."
                    else
                        "The base release has no Internet access and no external API."
                )
            }
        }
    }
}

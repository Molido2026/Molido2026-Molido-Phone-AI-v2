package ir.molido.phoneai.model

enum class AppLanguage {
    FA,
    EN
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class Screen {
    CHAT,
    MEMORY,
    NOTES,
    SETTINGS
}

sealed interface BrainAction {
    data class SaveMemory(val value: String) : BrainAction
    data class SaveNote(val value: String) : BrainAction
    data object None : BrainAction
}

data class BrainReply(
    val text: String,
    val action: BrainAction = BrainAction.None
)

data class ChatMessage(
    val user: Boolean,
    val text: String
)

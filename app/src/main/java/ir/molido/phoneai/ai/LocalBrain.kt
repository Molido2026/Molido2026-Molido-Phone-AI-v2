package ir.molido.phoneai.ai

import ir.molido.phoneai.model.AppLanguage
import ir.molido.phoneai.model.BrainAction
import ir.molido.phoneai.model.BrainReply
import java.util.Locale

class LocalBrain {

    fun reply(input: String, language: AppLanguage): BrainReply {
        val normalized = input.trim()
        if (normalized.isBlank()) {
            return BrainReply(
                if (language == AppLanguage.FA) "پیامت خالی است." else "Your message is empty."
            )
        }

        val lower = normalized.lowercase(Locale.ROOT)

        val rememberPrefix = listOf(
            "یادآوری کن",
            "یادآور",
            "remember ",
            "remember:"
        ).firstOrNull { lower.startsWith(it.lowercase(Locale.ROOT)) }

        if (rememberPrefix != null) {
            val value = normalized.drop(rememberPrefix.length).trim().trim(':', '-', ' ')
            if (value.isNotBlank()) {
                return BrainReply(
                    if (language == AppLanguage.FA) "ذخیره شد: $value" else "Saved to memory: $value",
                    BrainAction.SaveMemory(value)
                )
            }
        }

        val notePrefix = listOf(
            "یادداشت ",
            "یادداشت:",
            "note ",
            "note:"
        ).firstOrNull { lower.startsWith(it.lowercase(Locale.ROOT)) }

        if (notePrefix != null) {
            val value = normalized.drop(notePrefix.length).trim().trim(':', '-', ' ')
            if (value.isNotBlank()) {
                return BrainReply(
                    if (language == AppLanguage.FA) "یادداشت ذخیره شد." else "Note saved.",
                    BrainAction.SaveNote(value)
                )
            }
        }

        return when {
            matches(lower, "سلام", "hello", "hi", "درود") ->
                if (language == AppLanguage.FA)
                    BrainReply("سلام 🌱 من مغز محلی مولیدو هستم. بدون اینترنت کار می‌کنم.")
                else
                    BrainReply("Hello 🌱 I am Molido's local brain. I work offline.")

            matches(lower, "کمک", "help", "?") ->
                BrainReply(
                    if (language == AppLanguage.FA)
                        "نمونه‌ها:\n• «یادآوری کن جلسه فردا ساعت ۹»\n• «یادداشت خرید نان»\n• «یادآوری‌ها»\n• «یادداشت‌ها»"
                    else
                        "Examples:\n• \"remember meeting tomorrow at 9\"\n• \"note buy bread\"\n• \"memory\"\n• \"notes\""
                )

            matches(lower, "یادآوری‌ها", "یادآوری ها", "memory", "memories") ->
                BrainReply(
                    if (language == AppLanguage.FA)
                        "بخش «حافظه» را باز کن تا موارد ذخیره‌شده را ببینی."
                    else
                        "Open the Memory tab to see saved items."
                )

            matches(lower, "یادداشت‌ها", "یادداشت ها", "notes") ->
                BrainReply(
                    if (language == AppLanguage.FA)
                        "بخش «یادداشت‌ها» را باز کن تا یادداشت‌ها را ببینی."
                    else
                        "Open the Notes tab to see your notes."
                )

            matches(lower, "حالت آفلاین", "offline") ->
                BrainReply(
                    if (language == AppLanguage.FA)
                        "حالت پایه آفلاین است و متن به سرویس خارجی ارسال نمی‌شود."
                    else
                        "The base mode is offline and text is not sent to an external service."
                )

            else ->
                BrainReply(
                    if (language == AppLanguage.FA)
                        "من هنوز یک مغز محلی سبک هستم. برای فرمان‌های ذخیره‌سازی از «یادآوری کن ...» یا «یادداشت ...» استفاده کن."
                    else
                        "I am still a lightweight local brain. Use \"remember ...\" or \"note ...\" for storage commands."
                )
        }
    }

    private fun matches(value: String, vararg candidates: String): Boolean =
        candidates.any { value == it.lowercase(Locale.ROOT) }
}

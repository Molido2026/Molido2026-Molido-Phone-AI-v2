package ir.molido.phoneai.ai

import ir.molido.phoneai.model.AppLanguage
import ir.molido.phoneai.model.BrainAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalBrainTest {

    private val brain = LocalBrain()

    @Test
    fun helloInPersian() {
        val result = brain.reply("سلام", AppLanguage.FA)
        assertTrue(result.text.contains("سلام"))
    }

    @Test
    fun rememberCreatesSaveMemoryAction() {
        val result = brain.reply("یادآوری کن جلسه ساعت ۹", AppLanguage.FA)
        assertTrue(result.action is BrainAction.SaveMemory)
        assertEquals("جلسه ساعت ۹", (result.action as BrainAction.SaveMemory).value)
    }

    @Test
    fun noteCreatesSaveNoteAction() {
        val result = brain.reply("note buy bread", AppLanguage.EN)
        assertTrue(result.action is BrainAction.SaveNote)
        assertEquals("buy bread", (result.action as BrainAction.SaveNote).value)
    }

    @Test
    fun emptyInputIsHandled() {
        val result = brain.reply("   ", AppLanguage.EN)
        assertTrue(result.text.isNotBlank())
    }
}

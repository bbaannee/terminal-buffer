package terminal

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ResizeTest {

    @Test
    fun `resize to larger width preserves existing content`() {
        val buf = TerminalBuffer(width = 5, height = 3)
        buf.writeText("hello")
        buf.resize(10, 3)
        assertEquals('h', buf.getScreenChar(0, 0))
        assertEquals('o', buf.getScreenChar(0, 4))
    }

    @Test
    fun `resize to smaller width truncates lines`() {
        val buf = TerminalBuffer(width = 10, height = 3)
        buf.writeText("hello")
        buf.resize(3, 3)
        assertEquals(3, buf.width)
        assertEquals('h', buf.getScreenChar(0, 0))
        assertEquals('e', buf.getScreenChar(0, 1))
        assertEquals('l', buf.getScreenChar(0, 2))
    }

    @Test
    fun `resize to larger height adds blank rows at bottom`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.setCursor(0, 1); buf.writeText("row1")
        buf.resize(5, 4)
        assertEquals(4, buf.height)
        assertEquals("row1 ", buf.getScreenLineAsString(1))
        assertEquals("     ", buf.getScreenLineAsString(2))
        assertEquals("     ", buf.getScreenLineAsString(3))
    }

    @Test
    fun `resize to smaller height drops bottom rows`() {
        val buf = TerminalBuffer(width = 5, height = 4)
        buf.setCursor(0, 0); buf.writeText("row0")
        buf.setCursor(0, 3); buf.writeText("row3")
        buf.resize(5, 2)
        assertEquals(2, buf.height)
        assertEquals("row0 ", buf.getScreenLineAsString(0))
    }

    @Test
    fun `resize clamps cursor to new bounds`() {
        val buf = TerminalBuffer(width = 10, height = 10)
        buf.setCursor(9, 9)
        buf.resize(3, 3)
        assertEquals(2, buf.cursorCol)
        assertEquals(2, buf.cursorRow)
    }

    @Test
    fun `resize also reflowed scrollback lines`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.writeText("hello")
        buf.insertLineAtBottom()
        buf.resize(3, 2)
        assertEquals('h', buf.getChar(0, 0))
        assertEquals('e', buf.getChar(0, 1))
    }

    @Test
    fun `resize rejects zero or negative dimensions`() {
        val buf = TerminalBuffer(width = 5, height = 5)
        assertThrows<IllegalArgumentException> { buf.resize(0, 5) }
        assertThrows<IllegalArgumentException> { buf.resize(5, 0) }
        assertThrows<IllegalArgumentException> { buf.resize(-1, 5) }
    }
}
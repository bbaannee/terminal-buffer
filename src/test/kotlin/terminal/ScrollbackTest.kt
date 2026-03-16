package terminal

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScrollbackTest {

    @Test
    fun `insertLineAtBottom pushes top line to scrollback`() {
        val buf = TerminalBuffer(width = 5, height = 3)
        buf.writeText("hello")
        buf.insertLineAtBottom()
        assertEquals("hello", buf.getLineAsString(0).trimEnd())
    }

    @Test
    fun `insertLineAtBottom adds blank line at bottom of screen`() {
        val buf = TerminalBuffer(width = 5, height = 3)
        buf.writeText("top")
        buf.insertLineAtBottom()
        assertEquals("     ", buf.getScreenLineAsString(2))
    }

    @Test
    fun `insertLineAtBottom respects maxScrollback limit`() {
        val buf = TerminalBuffer(width = 5, height = 2, maxScrollback = 2)
        repeat(5) { buf.insertLineAtBottom() }
        assertTrue(buf.totalLines - buf.height <= 2)
    }

    @Test
    fun `clearScreen empties all screen rows`() {
        val buf = TerminalBuffer(width = 5, height = 3)
        buf.writeText("hello")
        buf.clearScreen()
        for (r in 0 until 3) assertEquals("     ", buf.getScreenLineAsString(r))
    }

    @Test
    fun `clearScreen preserves scrollback`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.writeText("line1")
        buf.insertLineAtBottom()
        buf.clearScreen()
        assertEquals(1, buf.totalLines - buf.height)
        assertEquals("line1", buf.getLineAsString(0).trimEnd())
    }

    @Test
    fun `clearAll removes screen and scrollback`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.writeText("line1")
        buf.insertLineAtBottom()
        buf.clearAll()
        assertEquals(buf.height, buf.totalLines)
        for (r in 0 until 2) assertEquals("     ", buf.getScreenLineAsString(r))
    }

    @Test
    fun `scrollback lines are immutable snapshots`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.writeText("hello")
        buf.insertLineAtBottom()
        buf.setCursor(0, 0)
        buf.writeText("world")
        assertEquals("hello", buf.getLineAsString(0).trimEnd())
    }

    @Test
    fun `getCell works across scrollback and screen`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.writeText("AB")
        buf.insertLineAtBottom()
        assertEquals('A', buf.getChar(0, 0))
        assertEquals('B', buf.getChar(0, 1))
    }

    @Test
    fun `getCell throws for invalid line index`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        assertThrows<IllegalArgumentException> { buf.getCell(99, 0) }
    }

    @Test
    fun `getScreenContent returns all screen rows joined by newline`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.setCursor(0, 0); buf.writeText("hello")
        buf.setCursor(0, 1); buf.writeText("world")
        val content = buf.getScreenContent()
        assertEquals("hello\nworld", content)
    }

    @Test
    fun `getAllContent includes scrollback then screen`() {
        val buf = TerminalBuffer(width = 5, height = 2)
        buf.setCursor(0, 0); buf.writeText("first")
        buf.insertLineAtBottom()
        buf.setCursor(0, 0); buf.writeText("secnd")
        val all = buf.getAllContent()
        val lines = all.split("\n")
        assertEquals(3, lines.size)
        assertEquals("first", lines[0])
        assertEquals("secnd", lines[1])
    }
}
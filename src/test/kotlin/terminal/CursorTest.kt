package terminal

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CursorTest {

    private lateinit var buf: TerminalBuffer

    @BeforeEach
    fun setup() {
        buf = TerminalBuffer(width = 10, height = 5)
    }

    @Test
    fun `initial cursor is at 0,0`() {
        assertEquals(0, buf.cursorCol)
        assertEquals(0, buf.cursorRow)
    }

    @Test
    fun `setCursor moves to exact position`() {
        buf.setCursor(3, 2)
        assertEquals(3, buf.cursorCol)
        assertEquals(2, buf.cursorRow)
    }

    @Test
    fun `setCursor clamps column to width minus 1`() {
        buf.setCursor(999, 0)
        assertEquals(9, buf.cursorCol)
    }

    @Test
    fun `setCursor clamps row to height minus 1`() {
        buf.setCursor(0, 999)
        assertEquals(4, buf.cursorRow)
    }

    @Test
    fun `setCursor clamps negative column to 0`() {
        buf.setCursor(-5, 0)
        assertEquals(0, buf.cursorCol)
    }

    @Test
    fun `setCursor clamps negative row to 0`() {
        buf.setCursor(0, -5)
        assertEquals(0, buf.cursorRow)
    }

    @Test
    fun `moveCursorRight advances column`() {
        buf.setCursor(2, 0)
        buf.moveCursorRight(3)
        assertEquals(5, buf.cursorCol)
    }

    @Test
    fun `moveCursorRight clamps at right edge`() {
        buf.setCursor(8, 0)
        buf.moveCursorRight(100)
        assertEquals(9, buf.cursorCol)
    }

    @Test
    fun `moveCursorLeft retreats column`() {
        buf.setCursor(5, 0)
        buf.moveCursorLeft(3)
        assertEquals(2, buf.cursorCol)
    }

    @Test
    fun `moveCursorLeft clamps at left edge`() {
        buf.setCursor(1, 0)
        buf.moveCursorLeft(100)
        assertEquals(0, buf.cursorCol)
    }

    @Test
    fun `moveCursorDown advances row`() {
        buf.setCursor(0, 1)
        buf.moveCursorDown(2)
        assertEquals(3, buf.cursorRow)
    }

    @Test
    fun `moveCursorDown clamps at bottom edge`() {
        buf.setCursor(0, 3)
        buf.moveCursorDown(100)
        assertEquals(4, buf.cursorRow)
    }

    @Test
    fun `moveCursorUp retreats row`() {
        buf.setCursor(0, 3)
        buf.moveCursorUp(2)
        assertEquals(1, buf.cursorRow)
    }

    @Test
    fun `moveCursorUp clamps at top edge`() {
        buf.setCursor(0, 1)
        buf.moveCursorUp(100)
        assertEquals(0, buf.cursorRow)
    }
}
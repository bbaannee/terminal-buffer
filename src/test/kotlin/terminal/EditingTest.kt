package terminal

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EditingTest {

    private lateinit var buf: TerminalBuffer

    @BeforeEach
    fun setup() {
        buf = TerminalBuffer(width = 10, height = 5)
    }

    @Test
    fun `writeText places characters on current row`() {
        buf.setCursor(0, 0)
        buf.writeText("hello")
        assertEquals("hello     ", buf.getScreenLineAsString(0))
    }

    @Test
    fun `writeText advances cursor`() {
        buf.setCursor(0, 0)
        buf.writeText("hi")
        assertEquals(2, buf.cursorCol)
        assertEquals(0, buf.cursorRow)
    }

    @Test
    fun `writeText overwrites existing content`() {
        buf.setCursor(0, 0)
        buf.writeText("hello")
        buf.setCursor(0, 0)
        buf.writeText("world")
        assertEquals("world     ", buf.getScreenLineAsString(0))
    }

    @Test
    fun `writeText stops at right edge`() {
        buf.setCursor(8, 0)
        buf.writeText("abc")
        assertEquals('a', buf.getScreenChar(0, 8))
        assertEquals('b', buf.getScreenChar(0, 9))
        assertEquals(10, buf.cursorCol)
    }

    @Test
    fun `writeText stores attributes on cells`() {
        val red = CellAttributes(foreground = TerminalColor.Ansi(1))
        buf.currentAttributes = red
        buf.setCursor(0, 0)
        buf.writeText("X")
        assertEquals(red, buf.getScreenAttributes(0, 0))
    }

    @Test
    fun `writeText uses cursor row`() {
        buf.setCursor(0, 2)
        buf.writeText("row2")
        assertEquals("row2      ", buf.getScreenLineAsString(2))
        assertEquals("          ", buf.getScreenLineAsString(0))
    }

    @Test
    fun `insertText wraps to next line`() {
        buf.setCursor(9, 0)
        buf.insertText("ab")
        assertEquals('a', buf.getScreenChar(0, 9))
        assertEquals('b', buf.getScreenChar(1, 0))
    }

    @Test
    fun `insertText scrolls when reaching last row`() {
        buf.setCursor(9, 4)
        buf.insertText("X")
        buf.insertText("Y")
        assertEquals('Y', buf.getScreenChar(4, 0))
        assertEquals(1, buf.totalLines - buf.height)
    }

    @Test
    fun `insertText cursor position after wrap`() {
        buf.setCursor(9, 0)
        buf.insertText("AB")
        assertEquals(1, buf.cursorCol)
        assertEquals(1, buf.cursorRow)
    }

    @Test
    fun `fillLine fills row with given char`() {
        buf.fillLine(0, '-')
        assertEquals("----------", buf.getScreenLineAsString(0))
    }

    @Test
    fun `fillLine with null fills row with spaces`() {
        buf.setCursor(0, 0)
        buf.writeText("hello")
        buf.fillLine(0, null)
        assertEquals("          ", buf.getScreenLineAsString(0))
    }

    @Test
    fun `fillLine does not move cursor`() {
        buf.setCursor(3, 2)
        buf.fillLine(0, 'x')
        assertEquals(3, buf.cursorCol)
        assertEquals(2, buf.cursorRow)
    }

    @Test
    fun `fillLine uses current attributes`() {
        val bold = CellAttributes(style = TextStyle(bold = true))
        buf.currentAttributes = bold
        buf.fillLine(1, 'A')
        assertEquals(bold, buf.getScreenAttributes(1, 0))
        assertEquals(bold, buf.getScreenAttributes(1, 9))
    }
}
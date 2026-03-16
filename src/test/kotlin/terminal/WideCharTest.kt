package terminal

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WideCharTest {

    @Test
    fun `CJK character has visual width 2`() {
        assertEquals(2, '中'.visualWidth())
        assertEquals(2, '日'.visualWidth())
        assertEquals(2, '한'.visualWidth())
    }

    @Test
    fun `ASCII character has visual width 1`() {
        assertEquals(1, 'A'.visualWidth())
        assertEquals(1, '0'.visualWidth())
        assertEquals(1, ' '.visualWidth())
    }

    @Test
    fun `writing wide char places Wide cell and Placeholder`() {
        val buf = TerminalBuffer(width = 10, height = 3)
        buf.setCursor(0, 0)
        buf.writeText("中")
        val left = buf.getScreenCell(0, 0)
        val right = buf.getScreenCell(0, 1)
        assertEquals(CellContent.Wide('中'), left.content)
        assertEquals(CellContent.Placeholder, right.content)
    }

    @Test
    fun `wide char advances cursor by 2`() {
        val buf = TerminalBuffer(width = 10, height = 3)
        buf.setCursor(0, 0)
        buf.writeText("中")
        assertEquals(2, buf.cursorCol)
    }

    @Test
    fun `wide char at last column becomes space`() {
        val buf = TerminalBuffer(width = 5, height = 3)
        buf.setCursor(4, 0)
        buf.writeText("中")
        assertEquals(CellContent.Empty, buf.getScreenCell(0, 4).content)
        assertEquals(5, buf.cursorCol)
    }

    @Test
    fun `wide char at second-to-last column fits`() {
        val buf = TerminalBuffer(width = 6, height = 3)
        buf.setCursor(4, 0)
        buf.writeText("字")
        assertEquals(CellContent.Wide('字'), buf.getScreenCell(0, 4).content)
        assertEquals(CellContent.Placeholder, buf.getScreenCell(0, 5).content)
        assertEquals(6, buf.cursorCol)
    }

    @Test
    fun `getLineAsString skips placeholders`() {
        val buf = TerminalBuffer(width = 6, height = 3)
        buf.setCursor(0, 0)
        buf.writeText("AB中CD")
        val line = buf.getScreenLineAsString(0)
        assertEquals("AB中CD", line)
    }

    @Test
    fun `mixed ASCII and wide chars write correctly`() {
        val buf = TerminalBuffer(width = 10, height = 3)
        buf.writeText("A中B")
        assertEquals('A', buf.getScreenChar(0, 0))
        assertEquals('中', buf.getScreenChar(0, 1))
        assertEquals(' ', buf.getScreenChar(0, 2))
        assertEquals('B', buf.getScreenChar(0, 3))
        assertEquals(4, buf.cursorCol)
    }
}
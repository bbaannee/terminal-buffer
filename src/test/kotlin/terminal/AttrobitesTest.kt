package terminal

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AttributesTest {

    @Test
    fun `ANSI color valid indices`() {
        for (i in 0..15) {
            assertEquals(i, (TerminalColor.Ansi(i)).index)
        }
    }

    @Test
    fun `ANSI color rejects index below 0`() {
        assertThrows<IllegalArgumentException> { TerminalColor.Ansi(-1) }
    }

    @Test
    fun `ANSI color rejects index above 15`() {
        assertThrows<IllegalArgumentException> { TerminalColor.Ansi(16) }
    }

    @Test
    fun `CellAttributes default has no style flags`() {
        val attr = CellAttributes.DEFAULT
        assertEquals(false, attr.style.bold)
        assertEquals(false, attr.style.italic)
        assertEquals(false, attr.style.underline)
        assertEquals(TerminalColor.Default, attr.foreground)
        assertEquals(TerminalColor.Default, attr.background)
    }

    @Test
    fun `CellAttributes equality`() {
        val a = CellAttributes(TerminalColor.Ansi(1), TerminalColor.Ansi(2), TextStyle(bold = true))
        val b = CellAttributes(TerminalColor.Ansi(1), TerminalColor.Ansi(2), TextStyle(bold = true))
        assertEquals(a, b)
    }

    @Test
    fun `CellAttributes inequality on different foreground`() {
        val a = CellAttributes(foreground = TerminalColor.Ansi(0))
        val b = CellAttributes(foreground = TerminalColor.Ansi(1))
        assertNotEquals(a, b)
    }
}
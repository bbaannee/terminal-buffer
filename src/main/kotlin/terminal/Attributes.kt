package terminal

sealed class TerminalColor {
    object Default : TerminalColor()
    data class Ansi(val index: Int) : TerminalColor() {
        init {
            require(index in 0..15) { "ANSI color index must be 0–15, got $index" }
        }
    }
}

data class TextStyle(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
) {
    companion object {
        val DEFAULT = TextStyle()
    }
}

data class CellAttributes(
    val foreground: TerminalColor = TerminalColor.Default,
    val background: TerminalColor = TerminalColor.Default,
    val style: TextStyle = TextStyle.DEFAULT,
) {
    companion object {
        val DEFAULT = CellAttributes()
    }
}
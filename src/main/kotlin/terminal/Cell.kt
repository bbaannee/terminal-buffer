package terminal

sealed class CellContent {
    data class Char(val char: kotlin.Char) : CellContent()
    data class Wide(val char: kotlin.Char) : CellContent()
    object Placeholder : CellContent()
    object Empty : CellContent()

    fun displayChar(): kotlin.Char = when (this) {
        is Char -> char
        is Wide -> char
        is Placeholder, Empty -> ' '
    }
}

data class Cell(
    val content: CellContent = CellContent.Empty,
    val attributes: CellAttributes = CellAttributes.DEFAULT,
) {
    companion object {
        val EMPTY = Cell()
    }
}
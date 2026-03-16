package terminal

class TerminalLine(val width: Int) {
    private val cells: MutableList<Cell> = MutableList(width) {Cell.EMPTY}

    operator fun get(col: Int):Cell{
        requreValidColumn(col)
        return cells[col]
    }

    operator fun set(col: Int, cell: Cell){
        requreValidColumn(col)
        cells[col] = cell
    }

    fun clearRange(fromCol : Int = 0, toCol: Int = width - 1 ){
        for (c in fromCol..toCol) cells[c] = Cell.EMPTY
    }

    fun fill(char : Char?, attributes: CellAttributes){
        val content = if (char == null) CellContent.Empty else CellContent.Char(char )
        val cell = Cell(content, attributes)
        for (c in 0 until width) cells[c] = cell
    }

    fun snapshot(): TerminalLine {
        val copy = TerminalLine(width)
        for (c in 0 until width) copy.cells[c] = cells[c]
        return copy
    }

    fun asString() : String = buildString(width){
        for(c in 0 until width){
            val content = cells[c].content
            if(content is CellContent.Placeholder) continue
            append(content.displayChar())
        }
    }

    private fun requreValidColumn(col : Int){
        require(col in 0 until width) {
            "Column $col out of bounds [0, $width)"
        }
    }


}
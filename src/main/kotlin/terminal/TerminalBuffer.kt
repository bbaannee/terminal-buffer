package terminal

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollback: Int = 1000,
){
    init{
        require(width > 0) {"width must be > 0"}
        require(height > 0) {"height must be > 0"}
        require(maxScrollback >= 0) {"maxScrollBack must be >= 0"}
    }

    private val scrollback : ArrayDeque<TerminalLine> = ArrayDeque()

    private val screen: MutableList<TerminalLine> = MutableList(height){
        TerminalLine(width)
    }

    var cursorCol: Int = 0
        private set

    var cursorRow: Int = 0
        private set

    var currentAttributes: CellAttributes = CellAttributes.DEFAULT

    fun setForeground(color: TerminalColor){
        currentAttributes = currentAttributes.copy(foreground = color)
    }
    fun setBackground(color : TerminalColor){
        currentAttributes = currentAttributes.copy(background = color)
    }

    fun setStyle(newStyle: TextStyle) {
        currentAttributes = currentAttributes.copy(style = newStyle)
    }

    fun resetAttributes() {
        currentAttributes = CellAttributes.DEFAULT
    }

    fun setCursor(col: Int, row: Int) {
        cursorCol = col.coerceIn(0, width - 1)
        cursorRow = row.coerceIn(0, height - 1)
    }

    fun moveCursorUp(n: Int = 1) {
        cursorRow = (cursorRow - n).coerceAtLeast(0)
    }

    fun moveCursorDown(n: Int = 1) {
        cursorRow = (cursorRow + n).coerceAtMost(height - 1)
    }

    fun moveCursorLeft(n: Int = 1) {
        cursorCol = (cursorCol - n).coerceAtLeast(0)
    }

    fun moveCursorRight(n: Int = 1) {
        cursorCol = (cursorCol + n).coerceAtMost(width - 1)
    }

    fun writeText(text : String){
        for(ch in text){
            if(cursorCol >= width) break
            val visualWidth = ch.visualWidth()
            when{
                visualWidth == 2 && cursorCol + 1 < width ->{
                    screen[cursorRow][cursorCol] = Cell(CellContent.Wide(ch), currentAttributes)
                    screen[cursorRow][cursorCol + 1] = Cell(CellContent.Placeholder, currentAttributes)
                    cursorCol += 2
                }
                visualWidth == 2 -> {
                    screen[cursorRow][cursorCol] = Cell(CellContent.Empty, currentAttributes)
                    cursorCol += 1
                }
                else -> {
                    screen[cursorRow][cursorCol] = Cell(CellContent.Char(ch), currentAttributes)
                    cursorCol += 1
                }
            }

        }
    }

    fun fillLine(row: Int, char: Char? = null) {
        requireScreenRow(row)
        screen[row].fill(char, currentAttributes)
    }

    fun insertLineAtBottom() {
        val evicted = screen.removeAt(0).snapshot()
        screen.add(TerminalLine(width))

        scrollback.addLast(evicted)
        while (scrollback.size > maxScrollback) scrollback.removeFirst()

        if (cursorRow > 0) cursorRow--
    }

    fun clearScreen() {
        for (row in 0 until height) screen[row] = TerminalLine(width)
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    val totalLines: Int get() = scrollback.size + height

    fun getCell(line: Int, col: Int): Cell {
        requireValidLine(line)
        requireValidColumn(col)
        return lineAt(line)[col]
    }

    fun getChar(line: Int, col: Int): Char = getCell(line, col).content.displayChar()

    fun getAttributes(line: Int, col: Int): CellAttributes = getCell(line, col).attributes

    fun getLineAsString(line: Int): String {
        requireValidLine(line)
        return lineAt(line).asString()
    }

    fun getScreenContent(): String =
        screen.joinToString("\n") { it.asString() }

    fun getAllContent(): String =
        (scrollback.asSequence() + screen.asSequence())
            .joinToString("\n") { it.asString() }

    fun getScreenCell(row: Int, col: Int): Cell {
        requireScreenRow(row)
        requireValidColumn(col)
        return screen[row][col]
    }

    fun getScreenChar(row: Int, col: Int): Char = getScreenCell(row, col).content.displayChar()

    fun getScreenAttributes(row: Int, col: Int): CellAttributes = getScreenCell(row, col).attributes

    fun getScreenLineAsString(row: Int): String {
        requireScreenRow(row)
        return screen[row].asString()
    }

    fun resize(newWidth: Int, newHeight: Int) {
        require(newWidth > 0) { "newWidth must be > 0" }
        require(newHeight > 0) { "newHeight must be > 0" }

        fun TerminalLine.reflow(w: Int): TerminalLine {
            val newLine = TerminalLine(w)
            for (c in 0 until minOf(width, w)) newLine[c] = this[c]
            return newLine
        }

        for (i in screen.indices) screen[i] = screen[i].reflow(newWidth)

        while (screen.size < newHeight) screen.add(TerminalLine(newWidth))
        while (screen.size > newHeight) screen.removeLast()

        for (i in scrollback.indices) {
            val old = scrollback[i]
            val newLine = TerminalLine(newWidth)
            for (c in 0 until minOf(old.width, newWidth)) newLine[c] = old[c]
            scrollback[i] = newLine
        }

        cursorCol = cursorCol.coerceAtMost(newWidth - 1)
        cursorRow = cursorRow.coerceAtMost(newHeight - 1)
    }

    private fun screenLine(row: Int): TerminalLine = screen[row]

    private fun lineAt(line: Int): TerminalLine =
        if (line < scrollback.size) scrollback[line]
        else screen[line - scrollback.size]

    private fun requireScreenRow(row: Int) =
        require(row in 0 until height) { "Screen row $row out of bounds [0, $height)" }

    private fun requireValidLine(line: Int) =
        require(line in 0 until totalLines) { "Line $line out of bounds [0, $totalLines)" }

    private fun requireValidColumn(col: Int) =
        require(col in 0 until width) { "Column $col out of bounds [0, $width)" }
}

fun Char.visualWidth(): Int {
    val cp = this.code
    return if (
        cp in 0x1100..0x115F ||
        cp in 0x2E80..0x303E ||
        cp in 0x3040..0x33FF ||
        cp in 0x3400..0x4DBF ||
        cp in 0x4E00..0x9FFF ||
        cp in 0xA000..0xA4CF ||
        cp in 0xAC00..0xD7AF ||
        cp in 0xF900..0xFAFF ||
        cp in 0xFE10..0xFE1F ||
        cp in 0xFE30..0xFE6F ||
        cp in 0xFF00..0xFF60 ||
        cp in 0xFFE0..0xFFE6
    ) 2 else 1
}
# Terminal Buffer

A terminal text buffer implementation in Kotlin — the core data structure that
terminal emulators use to store and manipulate displayed text.

## Building & running tests
```bash
./gradlew test
```

Requires JDK 17+. Gradle wrapper will download Gradle automatically.

## Design decisions and trade-offs

### Data model

The buffer is split into three layers:

- **Cell** — atomic unit: one character column + visual attributes
- **TerminalLine** — a fixed-width row of cells
- **TerminalBuffer** — screen grid + scrollback history + cursor

`CellAttributes` is an immutable `data class`, making it safe to share
between cells without defensive copying.

### Screen vs scrollback

The screen is a `MutableList<TerminalLine>` of exactly `height` entries.
The scrollback is an `ArrayDeque<TerminalLine>` capped at `maxScrollback`.

When `insertLineAtBottom()` is called, the top screen line is snapshot-copied
before being pushed to the deque. This is the key invariant: scrollback lines
are frozen — screen edits can never retroactively corrupt history.

### writeText vs insertText

Two distinct write operations match real terminal emulator semantics:

- **writeText** — overwrite mode: writes in place, stops at the right edge.
- **insertText** — insert mode: wraps to the next line, scrolls if needed.

### Wide character support

Unicode wide characters occupy 2 terminal columns. The model uses two variants:

- `CellContent.Wide(char)` — the left cell, holds the actual glyph.
- `CellContent.Placeholder` — the right cell, renders as a space.

`getLineAsString()` skips placeholders so the string has one character per
glyph regardless of display width. Width detection uses hardcoded Unicode
block ranges since external libraries are not allowed.

### Resize strategy

On resize, lines are truncated or zero-padded to the new width. Rows beyond
the new height are dropped, matching the behaviour of xterm and alacritty.
The cursor is clamped to the new bounds.

## What I would add with more time

- **Full emoji support** — proper handling of supplementary-plane characters
  using `String.codePoints()` instead of `Char` iteration.
- **Line dirty flags** — mark modified lines so a renderer can skip
  redrawing unchanged rows.
- **Alternate screen buffer** — terminals maintain two buffers; apps like
  `vim` switch to the alternate buffer on launch.
- **Selection model** — a range of positions representing the current text
  selection, needed for copy-paste.

## Project structure
```
src/
  main/kotlin/terminal/
    Attributes.kt      — TerminalColor, TextStyle, CellAttributes
    Cell.kt            — CellContent, Cell
    TerminalLine.kt    — TerminalLine (one row)
    TerminalBuffer.kt  — TerminalBuffer + Char.visualWidth()
  test/kotlin/terminal/
    AttributesTest.kt  — color and style model tests
    CursorTest.kt      — cursor movement and clamping
    EditingTest.kt     — writeText, insertText, fillLine
    ScrollbackTest.kt  — scrolling, clear, content access
    WideCharTest.kt    — wide character handling
    ResizeTest.kt      — resize feature
```
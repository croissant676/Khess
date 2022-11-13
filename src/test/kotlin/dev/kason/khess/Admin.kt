package dev.kason.khess

import java.awt.*
import javax.swing.*

private val frame = JFrame("Khess")
private val panel = JPanel()
private val boardPanel = JPanel()
fun initializeDevelopmentGameUI() {
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.add(boardPanel)
    val bottomPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        val sideLenLabel = JLabel()
        val playerCountLabel = JLabel()
        val button = JButton("Refresh")
        button.addActionListener {
            sideLenLabel.text = "Side Length: ${Game.boardSize}"
            playerCountLabel.text = "Player Count: ${Game.activePlayers.size}"
            updateBoardUI()
        }
        add(sideLenLabel)
        add(playerCountLabel)
        add(button)
    }
    panel.add(bottomPanel)
    frame.add(panel)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.pack()
    frame.isVisible = true
}

val Piece.testingRepresentation: String
    get() = when (this) {
        is Pawn -> "P"
        is Knight -> "N"
        is Bishop -> "B"
        is Rook -> "R"
        is Queen -> "Q"
        is King -> "K"
        else -> "X"
    }

fun updateBoardUI() {
    if (Game.boardSize == 0) return // not initialized yet
    boardPanel.removeAll()
    boardPanel.layout = GridLayout(Game.boardSize, Game.boardSize)
    for ((x, y) in Game) {
        val square = JPanel()
        square.background = if ((x + y) % 2 == 0) Color.WHITE else Color.BLACK
        val piece = Game.pieces[Position(x, y)]
        if (piece != null) {
            val label = JLabel(piece.testingRepresentation)
            label.foreground = Color.decode(piece.player.color)
            square.add(label)
        }
        boardPanel.add(square)
    }
    frame.pack()
}

package dev.kason.khess

import java.awt.*
import javax.swing.*

// swing ui for chess game
// display the board
class GameUI(override val game: Game): Game.Entity {
    val frame = JFrame("Chess")
    val board = BoardUI(game.board)

    class BoardUI(board: Board) {
        val panel = JPanel()


        init {

        }

        fun display() {

        }
        class SquareUI(square: Square) {
            val panel = JPanel()
            val label = JLabel()
            init {
                panel.add(label)
            }
        }
    }
}
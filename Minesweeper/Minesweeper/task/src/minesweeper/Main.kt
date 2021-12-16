package minesweeper

import kotlin.random.Random


const val MINEFIELD_LENGTH = 9
const val MINEFIELD_BREADTH = 9
const val MINE_CELL = "X"
const val UNEXPLORED_CELL = "."
const val MARKED_CELL = "*"
const val EXPLORED_FREE_CELL = "/"

class MineLocation(val row: Int, val column: Int)

class Minesweeper() {
    var totalMines: Int = 0
    val mineField: MutableList<MutableList<String>> = mutableListOf()
    var isGameInProgress: Boolean = false
    var mineLocations: MutableList<MineLocation> = mutableListOf()
    val predictedIndices: MutableList<MineLocation> = mutableListOf()
    val markFreeSpots: MutableSet<MineLocation> = mutableSetOf()

    constructor(totalMines: Int) : this() {
        this.totalMines = totalMines
        setupMineField()
    }

    private fun claimFreeCell(loc: MineLocation) {
        when (this.mineField[loc.column][loc.row]) {
            MINE_CELL -> {
                this.printCurrentState(true)
                this.isGameInProgress = false
                println("You stepped on a mine and failed!")
            }
            UNEXPLORED_CELL -> {
                floodFill(loc.column, loc.row, EXPLORED_FREE_CELL)
                if (markFreeSpots.size > 0) {
                    markFreeSpots.forEach {
//                        println("mark free spot row ${it.row} column ${it.column}")
                        if (this.mineField[it.column][it.row] != EXPLORED_FREE_CELL) {
                            val minesForTheBlock = getNoOfMines(it.column, it.row)
                            if (minesForTheBlock > 0) {
//                                println("setting number")
                                this.mineField[it.column][it.row] = minesForTheBlock.toString()
                            } else {
//                                println("A setting explored free cell row ${it.column} column ${it.row} -> ${this.mineField[it.column][it.row]}" )
//                                println("B setting explored free cell row ${it.row} column ${it.column} -> ${this.mineField[it.row][it.column]}" )
                                this.mineField[it.column][it.row] = EXPLORED_FREE_CELL
                                removeLocationFromPredictedIndices(it)
//                                println(predictedIndices.remove(MineLocation(it.row, it.column)))
//                                println("After setting explored free cell row ${it.column} column ${it.row} -> ${this.mineField[it.column][it.row]}" )
                            }
                        }
                    }
                }
                this.printCurrentState()
            }
        }
    }

    private fun removeLocationFromPredictedIndices(position: MineLocation) {
        this.predictedIndices.removeIf{ it.row == position.row && it.column == position.column }
//        this.predictedIndices.forEach {
//            if (it.row == position.row && it.column == position.column) {
//                println(this.predictedIndices.remove(it))
//            }
//        }
    }

    private fun setUnsetMineCell(loc: MineLocation) {
        when (this.mineField[loc.column][loc.row]) {
            UNEXPLORED_CELL -> {
                this.predictedIndices.add(loc)
                this.printCurrentState()
                return
            }
            MINE_CELL -> {
                this.predictedIndices.add(loc)
                this.printCurrentState()
            }
            MARKED_CELL -> {
                val objectFound: MineLocation? = this.predictedIndices.find { it.row == loc.row && it.column == loc.column }
                val minedFound: MineLocation? = this.mineLocations.find { it.row == loc.row && it.column == loc.column }

                if (objectFound != null) {
                    if (minedFound == null) {
                        print("olalala")
                        this.mineField[objectFound.column][objectFound.row] = UNEXPLORED_CELL
                    } else {
                        this.mineField[objectFound.column][objectFound.row] = MINE_CELL
                    }
                    this.predictedIndices.remove(objectFound)
                }
                this.printCurrentState()
            }
            else -> {
                println("There is a number here!")
                return
            }
        }

        if (isGameComplete()) {
            println("Congratulations! You found all the mines!")
            this.isGameInProgress = false;
        }
    }

    fun startGame() {
        isGameInProgress = true

        while (this.isGameInProgress) {
            print("Set/unset mine marks or claim a cell as free: > ")
            val (row, column, command) = readLine()!!.trim().split("\\s+".toRegex()) //.map (String::toInt)
            val loc = MineLocation(row.toInt()-1, column.toInt()-1)

            when (command) {
                "free" -> claimFreeCell(loc)
                "mine" -> setUnsetMineCell(loc)
                else -> println("Invalid")
            }
        }
    }

    private fun setupMineField() {

        // Randomly generate locations for mines
        while (this.mineLocations.lastIndex < this.totalMines-1) {
            val randomLoc = MineLocation(Random.nextInt(0, 9), Random.nextInt(0, 9))
            if (!this.mineLocations.contains(randomLoc)) this.mineLocations.add(randomLoc)
        }

        // Create the minefield
        for (i in 0..MINEFIELD_LENGTH) {
            val row: MutableList<String> = mutableListOf()

            for (j in 0..MINEFIELD_BREADTH) {
                row.add(UNEXPLORED_CELL)
            }
            this.mineField.add(row)
        }

        // Add mines
        for (i in 0..mineLocations.lastIndex) {
            this.mineField[this.mineLocations[i].column][this.mineLocations[i].row] = MINE_CELL
        }

        // Compute values around mine cells
        for (i in 0 until MINEFIELD_LENGTH) {
            for (j in 0 until MINEFIELD_BREADTH) {
                if (this.mineField[i][j] != MINE_CELL) {
                    val noOfMines = getNoOfMines(i, j)
                    if (noOfMines > 0) {
                        this.mineField[i][j] = noOfMines.toString()
                    }
                }
            }
        }
    }

    fun printMineField() {
        println(" |123456789|")
        println("-|---------|")
        for (i in 0 until MINEFIELD_LENGTH) {
            print("${(i+1)}|")
            for (j in 0 until MINEFIELD_BREADTH) {
                if (this.mineField[i][j] == MINE_CELL) {
                    print(UNEXPLORED_CELL)
                } else {
                    print(this.mineField[i][j])
                }
            }
            print("|")
            println()
        }
        println("-|---------|")
        println()
    }

    private fun printCurrentState(showMines: Boolean = false) {
        val copyMineField = this.mineField.toMutableList() // shallow copy - refer to same elems in the list
        if (this.predictedIndices.size > 0) {
            for (i in 0..predictedIndices.lastIndex) {
                copyMineField[this.predictedIndices[i].column][this.predictedIndices[i].row] = MARKED_CELL
            }
        }

        println(" |123456789|")
        println("-|---------|")
        for (i in 0 until MINEFIELD_LENGTH) {
            print("${(i+1)}|")
            for (j in 0 until MINEFIELD_BREADTH) {
                if (showMines) {
                    print(this.mineField[i][j])
                } else {
                    if (this.mineField[i][j] == MINE_CELL) {
                        print(UNEXPLORED_CELL)
                    } else {
                        print(this.mineField[i][j])
                    }
                }
            }
            print("|")
            println()
        }
        println("-|---------|")
        println()
    }

    private fun getNoOfMines(x: Int, y: Int):Int {
        var mines = when (this.mineField[x][y]) {
            UNEXPLORED_CELL -> 0
            MINE_CELL -> 0
            MARKED_CELL -> 0
            EXPLORED_FREE_CELL -> 0
            else -> this.mineField[x][y].toInt()
        }

        // Top Left Cell
        if (x > 0 && y > 0) {
            if (this.mineField[x-1][y-1] == MINE_CELL) mines++
        }

        // Top Cell
        if (x > 0) {
            if (this.mineField[x-1][y] == MINE_CELL) mines++
        }

        // Top Right Cell
        if (x < MINEFIELD_LENGTH - 1 && y > 0) {
            if (this.mineField[x+1][y-1] == MINE_CELL) mines++
        }

        // Left Cell
        if (y > 0) {
            if (this.mineField[x][y-1] == MINE_CELL) mines++
        }

        // Right Cell
        if (x < MINEFIELD_LENGTH - 1) {
            if (this.mineField[x+1][y] == MINE_CELL) mines++
        }

        // Bottom Left Cell
        if (x > 0 && y < MINEFIELD_BREADTH - 1) {
            if (this.mineField[x-1][y+1] == MINE_CELL) mines++
        }

        // Bottom Cell
        if (y < MINEFIELD_BREADTH - 1) {
            if (this.mineField[x][y+1] == MINE_CELL) mines++
        }

        // Bottom Right Cell
        if (x < MINEFIELD_LENGTH && y < MINEFIELD_BREADTH) {
            if (this.mineField[x+1][y+1] == MINE_CELL) mines++
        }

        return mines
    }

    private fun floodFillUtil(x: Int, y: Int, prevC: String, newC: String) {
        // Base cases
        if (x < 0 || x >= MINEFIELD_BREADTH || y < 0 || y >= MINEFIELD_LENGTH) return

        if (this.mineField[x][y] == MARKED_CELL) {
            if (!markFreeSpots.contains(MineLocation(y, x))) {
                markFreeSpots.add(MineLocation(y, x))
//                println("row $y column $x" )
            }
//            print("Yo")
//            val minesForTheBlock = getNoOfMines(x, y)
//            println(minesForTheBlock)
//            if (minesForTheBlock > 0) {
//                println("ba")
//                this.mineField[x][y] = minesForTheBlock.toString()
//            } else {
//                println("la")
//                this.mineField[x][y] = EXPLORED_FREE_CELL
//                println(this.mineField[x][y])
//                println("XXXXXX")
//                this.printCurrentState()
//                println("XXXXXX")
//            }
        }

//        println(this.mineField[x][y])

        if (this.mineField[x][y] != prevC) return

        // Replace the color at (x, y)
        this.mineField[x][y] = newC

        // Recur for north, east, south and west
        floodFillUtil(x - 1, y - 1, prevC, newC) // top left
        floodFillUtil(x, y - 1, prevC, newC) // top
        floodFillUtil(x + 1, y - 1, prevC, newC) // top right
        floodFillUtil(x - 1, y, prevC, newC) // left
        floodFillUtil(x + 1, y, prevC, newC) // right
        floodFillUtil(x - 1, y + 1, prevC, newC) // bottom left
        floodFillUtil(x, y + 1, prevC, newC) // bottom
        floodFillUtil(x + 1, y + 1, prevC, newC) // bottom right



//        floodFillUtil(x + 1, y, prevC, newC)
//        floodFillUtil(x - 1, y, prevC, newC)
//        floodFillUtil(x, y + 1, prevC, newC)
//        floodFillUtil(x, y - 1, prevC, newC)
    }

    private fun floodFill(x: Int, y: Int, newC: String) {
        val prevC = this.mineField[x][y]
        if (prevC == newC) return
        floodFillUtil(x, y, prevC, newC)
    }

    private fun isGameComplete(): Boolean {
//        println("predictedIndices.size ${predictedIndices.size}")
//        println("mineLocations.size ${mineLocations.size}")

        if (predictedIndices.size != mineLocations.size) return false

        predictedIndices.sortBy { it.row }
        mineLocations.sortBy { it.row }

//        predictedIndices.forEach {
//            println("predictedIndex ${it.row} ${it.column}")
//        }

//        mineLocations.forEach {
//            println("mineLocationsIndex ${it.row} ${it.column}")
//        }

        for (i in 0 until predictedIndices.size) {
            if (predictedIndices.get(i).row != mineLocations.get(i).row &&
                predictedIndices.get(i).column != mineLocations.get(i).column) {
                return false;
            }
        }

        return true
    }
}

fun main() {
    print("How many mines do you want on the field? > ")
    var mines = readLine()!!.toInt()
    val minesweeperGame = Minesweeper(mines)
    minesweeperGame.printMineField()
    minesweeperGame.startGame()
}

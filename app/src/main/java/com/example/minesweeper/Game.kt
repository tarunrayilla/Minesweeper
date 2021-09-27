package com.example.minesweeper

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.ViewGroup
import android.widget.*
import android.widget.TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import java.lang.Integer.parseInt
import java.util.*

lateinit var timer: TextView
const val BEST_GAME_TIME_KEY = "BEST_GAME_TIME_KEY"
const val LAST_GAME_TIME_KEY = "LAST_GAME_TIME_KEY"

class Game : AppCompatActivity() {

    private lateinit var board: LinearLayout
    private var rows: Int = 0
    private var cols: Int = 0
    private var mines: Int = 0
    private lateinit var arr: Array<Array<MineCell>>
    private val xDir = intArrayOf(-1, -1, 0, 1, 1, 1, 0, -1)
    private val yDir = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
    private var count = 0
    private lateinit var countText: TextView
    //private lateinit var timer: TextView
    private var mark = 0
    private lateinit var obj: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val actionBar = supportActionBar
//        actionBar?.setDisplayHomeAsUpEnabled(true)


        //to set colour for action bar
        val colorDrawable =  ColorDrawable(Color.parseColor("#0B6500"))
        actionBar?.setBackgroundDrawable(colorDrawable)

        rows = intent.getIntExtra("rows", 0)
        cols = intent.getIntExtra("cols", 0)
        mines = intent.getIntExtra("mines", 0)

        arr = Array(rows) { Array(cols) { MineCell() }}
        board = findViewById(R.id.board)
        val restart: Button = findViewById(R.id.restart)

        setUpArray()
        setUpBoard()

        count = mines

        countText = findViewById(R.id.count)
        countText.text = count.toString()

        timer = findViewById(R.id.timer)
        timer.text = (0).toString()

        //restart the game
        restart.setOnClickListener {
            restartGame()
            setUpArray()
            //setUpBoard()
        }


    }

    // to set the board with the required number of cells
    private fun setUpBoard(){
        var counter = 1

        val params1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
        val params2 = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for(i in 0..rows-1){
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            params1.weight  = 1.0F

            for(j in 0..cols-1){
                val button = Button(this)
                button.id = counter
                button.setAutoSizeTextTypeWithDefaults(AUTO_SIZE_TEXT_TYPE_UNIFORM)
                button.setTextColor(ContextCompat.getColor(this, R.color.purple_700))
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
                button.layoutParams = params2

//                val param = button.layoutParams as ViewGroup.MarginLayoutParams
//                param.setMargins(1, 1, 1, 1)
//                button.layoutParams = param

                params2.weight = 1.0F

                val gradientDrawable = GradientDrawable()
                gradientDrawable.setStroke(2, ContextCompat.getColor(this, R.color.black))
                button.background = gradientDrawable

                button.setOnClickListener {
                    //button.text = arr[i][j].value.toString()
                    if(!arr[i][j].isMarked) {
                        recordMove(i, j, button)
                    }
                }
                //to set a flag on click of cell
                button.setOnLongClickListener{
                    recordMoveFlag(i, j, button)
                    true
                }

                linearLayout.addView(button)
                counter++
            }
            board.addView(linearLayout)
        }
    }

    //to open a cell
    //if the cell has a mine then we show lost message and reveal the mines
    // if the user completes the game we show win message
    private fun recordMove(x: Int, y: Int, button: Button) {
        if(mark == 0) {
            obj = object : CountDownTimer(600000, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    timer.text = (600-(millisUntilFinished/1000)).toString()
                }

                override fun onFinish() {
                    timer.setText("-")
                }
            }.start()

            mark = 1
        }
        if(arr[x][y].value == -1) {
            //button.text = arr[x][y].value.toString()
            button.setBackgroundResource(R.drawable.red_mine)
            showLostMessage()
            disableGame()
            revealMines()
        } else if(arr[x][y].value > 0) {
            var value = arr[x][y].value
            button.text = value.toString()

            setColor(button, value)
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.wood))

        } else {
            handleZero(x, y)
            //button.text = arr[x][y].value.toString()
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.wood))

        }

        arr[x][y].isRevealed = true
        button.isEnabled = false
        checkWin()
    }

    //to change the number of flags available when user paces or removes a flag
    private fun recordMoveFlag(x: Int, y: Int, button: Button) {
        if(arr[x][y].isMarked) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            arr[x][y].isMarked = false
            count++
        } else {
            button.setBackgroundResource(R.drawable.flag)
            arr[x][y].isMarked = true
            count--
        }
        countText.text = count.toString()
        checkWin()
    }

    //to set an array equivalent to board
    private fun setUpArray() {
        var t = 0
//        for (k in 1..mines) {
//            var x = Random().nextInt(rows)
//            var y = Random().nextInt(cols)
//            arr[x][y].value = -1
//        }
        while(t != mines) {
            var x = Random().nextInt(rows)
            var y = Random().nextInt(cols)
            if(arr[x][y].value != -1) {
                arr[x][y].value = -1
                t++
            }
        }


        for(i in 0..rows-1) {
            for(j in 0..cols-1) {
                if(arr[i][j].value == -1) {
                    updateNeighbours(i, j)
                }
            }
        }
    }

    //to update each cell in array that doesn't have a mine with the number of mines adjacent to it
    private fun updateNeighbours(x: Int, y: Int) {
        var nextX = 0
        var nextY = 0
        for(i in 0..7) {
            nextX = x+xDir[i]
            nextY = y+yDir[i]
            if(isValid(nextX, nextY) && arr[nextX][nextY].value != -1) {
                arr[nextX][nextY].value++
            }
        }
    }

    private fun isValid(x: Int, y: Int): Boolean {
        if(x>=0 && x<rows && y>=0 && y<cols) {
            return true
        }
        return false
    }

    //to restart the game
    private fun restartGame() {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setStroke(2, ContextCompat.getColor(this, R.color.black))

        for(i in 0..rows-1) {
            for(j in 0..cols-1) {
                arr[i][j].value = 0
                arr[i][j].isMarked = false
                arr[i][j].isRevealed = false
            }
        }
        for(i in 1..(rows*cols)) {
            val bn = resources.getIdentifier("$i", "id", packageName)
            val b: Button = findViewById(bn)
            b.text = ""
            b.isEnabled = true
            b.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            countText.text = mines.toString()
            b.background = gradientDrawable
            mark = 0
            if(this::obj.isInitialized) {
                obj.cancel()
            }
            timer.text = (0).toString()
        }
        count = mines
    }

    //when a user opens an empty cell, we reveal all the cells adjacent to it that has a number
    //if the the adjacent cell is also an empty cell, we reveal its adjacent cell as well which have a number
    private fun handleZero(x: Int, y: Int) {
        arr[x][y].isRevealed = true

        for(i in 0..7) {
            var xStep = x + xDir[i]
            var yStep = y + yDir[i]
            if(!isValid(xStep, yStep)) {
                continue
            }
            val bn = resources.getIdentifier("${xStep*cols+yStep+1}", "id", packageName)
            val b: Button = findViewById(bn)
            if(arr[xStep][yStep].value> 0 && !arr[xStep][yStep].isMarked) {
                arr[xStep][yStep].isRevealed = true
                var value = arr[xStep][yStep].value
                b.text = value.toString()
                setColor(b, value)
                b.setBackgroundColor(ContextCompat.getColor(this, R.color.wood))
                b.isEnabled = false
            } else if(arr[xStep][yStep].value == 0 && !arr[xStep][yStep].isMarked && !arr[xStep][yStep].isRevealed) {
                b.setBackgroundColor(ContextCompat.getColor(this, R.color.wood))
                handleZero(xStep, yStep)
            }
        }
        //countText.text = count.toString()
    }

    //to show lost message when user opens a cell that has mine
    private fun showLostMessage() {
        obj.cancel()
        //Toast.makeText(this, "You Lost the game!", Toast.LENGTH_SHORT).show()
        val lostMes = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        lostMes.setTitle("GAME OVER")
        lostMes.setMessage("You lost the game!")
        lostMes.setNegativeButton("Inspect") {dialog, which -> }
        lostMes.setPositiveButton("Play Again") {dialog, which ->
            restartGame()
            setUpArray()
        }
        lostMes.show()
    }

    //to show win message when user completes the game
    private fun showWinMessage() {
        lastGameTime.text = timer.text
        if(bestTime.text == "--") {
            bestTime.text = lastGameTime.text
        }
        else {
            if(parseInt(bestTime.text.toString()) > parseInt(lastGameTime.text.toString())) {
                bestTime.text = timer.text
            }
        }
        saveTimeTaken()
        //Toast.makeText(this, "Congratulations! you won the game!", Toast.LENGTH_SHORT).show()
        val winMes = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        winMes.setTitle("GAME OVER")
        winMes.setMessage("Congratulations! you won the game!")
        winMes.setNegativeButton("Inspect") {dialog, which -> }
        winMes.setPositiveButton("Play Again") {dialog, which ->
            restartGame()
            setUpArray()
        }
        winMes.show()

    }

    private fun checkWin() {
        var check = 0
        for(i in 0..rows-1) {
            for(j in 0..cols-1) {
                if(arr[i][j].value == -1 && !arr[i][j].isMarked) {
                    check = 1
                    break
                } else if(arr[i][j].value != -1 && !arr[i][j].isRevealed) {
                    check = 1
                    break
                }
            }
        }
        if(check == 0) {
            obj.cancel()
            showWinMessage()
        }
        check = 0
    }

    //to disable the cells when user loses the game allowing him only the restart button if he/she wishes to play again
    private fun disableGame() {
        for(i in 1..(rows*cols)) {
            val bn = resources.getIdentifier("$i", "id", packageName)
            val b: Button = findViewById(bn)
            b.isEnabled = false
        }
    }

    //to set the colour of number in cell
    private fun setColor(button: Button, value: Int) {
        if(value == 1) {
            button.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else if(value == 2) {
            button.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else if(value == 3) {
            button.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            button.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    //to reveal all the mines when user loses
    private fun revealMines() {
        for(i in 0..rows-1) {
            for(j in 0..cols-1) {
                if(arr[i][j].value == -1) {
                    val bn = resources.getIdentifier("${i*cols+j+1}", "id", packageName)
                    val b: Button = findViewById(bn)
                    b.setBackgroundResource(R.drawable.red_mine)
                }
            }
        }
    }

    //to stop the clock when user clicks on the back button
    override fun onBackPressed() {
        super.onBackPressed()
        if(this::obj.isInitialized) {
            obj.cancel()
        }
    }

//    private fun saveTimeTaken() {
//        val sharedPref = getPreferences(Context.MODE_PRIVATE)
//        with(sharedPref.edit()) {
//            putString(BEST_GAME_TIME_KEY, bestTime.text.toString())
//            putString(LAST_GAME_TIME_KEY, lastGameTime.text.toString())
//            apply()
//        }
//    }

    //to store the best game time and last game time of the user using shared preferences
    private fun saveTimeTaken() {
        val sharedPref = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(BEST_GAME_TIME_KEY, bestTime.text.toString())
            putString(LAST_GAME_TIME_KEY, lastGameTime.text.toString())
            apply()
        }
    }
}
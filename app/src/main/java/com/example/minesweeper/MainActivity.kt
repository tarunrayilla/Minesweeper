package com.example.minesweeper

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

lateinit var bestTime: TextView
lateinit var lastGameTime: TextView

class MainActivity : AppCompatActivity() {

    private var rows = 0
    private var cols = 0
    private var mines = 0

    private lateinit var no_of_rows: EditText
    private lateinit var no_of_columns: EditText
    private lateinit var no_of_mines: EditText
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val help = findViewById<ImageView>(R.id.help)
        help.setOnClickListener {
            val ins = AlertDialog.Builder(this, R.style.AlertDialogStyle)
            ins.setTitle("Instructions")
            ins.setMessage("The purpose of the game is to open all the cells of the board which do not contain a mine. " +
                    "You lose if you open a mine cell. " +
                    "Every non-mine cell you open will tell you the total number of mines in the eight neighboring cells. " +
                    "Once you are sure that a cell contains a mine, you can long-press to put a flag on it as a reminder. " +
                    "You win when you place a flag on all the mines and open all non-mine cells")
            ins.setPositiveButton("OK") {dialog, which -> }
            ins.show()

//            val builder = AlertDialog.Builder(this)
//            val inflater = this.layoutInflater
//            val dialogView: View = inflater.inflate(R.layout.instructions, null)
//            with(builder) {
//                setView(dialogView)
//
//            }
//            val alertDialog = builder.create()
//            alertDialog.show()
//            val button = findViewById<Button>(R.id.button)
//            button.setOnClickListener {
//                onBackPressed()
//            }
        }

        bestTime = findViewById<TextView>(R.id.best_time)
        lastGameTime = findViewById<TextView>(R.id.last_game_time)

        //to load the best game time and last game time when user played prior to this
        loadTimeTaken()

        no_of_rows = findViewById(R.id.no_of_rows)
        no_of_columns = findViewById(R.id.no_of_columns)
        no_of_mines = findViewById(R.id.no_of_mines)

        val customBoard: Button = findViewById(R.id.customBoard)
        customBoard.setOnClickListener {
            no_of_rows.visibility = View.VISIBLE
            no_of_columns.visibility = View.VISIBLE
            no_of_mines.visibility = View.VISIBLE

        }

        val start: Button = findViewById(R.id.start)
        start.setOnClickListener {
            setRowsCols()
            val intent = Intent(this, Game::class.java)
            intent.putExtra("rows", rows)
            intent.putExtra("cols", cols)
            intent.putExtra("mines", mines)
            if(rows != 0 && cols != 0) {
                rows = 0
                cols = 0
                mines = 0
                no_of_rows.text.clear()
                no_of_columns.text.clear()
                no_of_mines.text.clear()

                startActivity(intent)
            }
        }
    }

    //to set the number of ros and columns of the board
    private fun setRowsCols() {
        radioGroup = findViewById(R.id.radioGroup)
        val radioId = radioGroup.checkedRadioButtonId
        if(radioId != -1) {
            val level:RadioButton = findViewById(radioId)
            when (level.text) {
                "Easy" -> {
                    rows = 6
                    cols = 5
                    mines = 5
                }
                "Medium" -> {
                    rows = 8
                    cols = 6
                    mines = 9
                }
                else -> {
                    rows = 12
                    cols = 10
                    mines = 29
                }
            }
        }
        if(no_of_rows.text.toString() != "" && no_of_columns.text.toString() != "" && no_of_mines.text.toString() != "") {
            var r = Integer.parseInt(no_of_rows.text.toString())
            var c = Integer.parseInt(no_of_columns.text.toString())
            var m = Integer.parseInt(no_of_mines.text.toString())

            if(m > (r*c)/4) {
                val d = AlertDialog.Builder(this)
                d.setTitle("Error")
                d.setMessage("Number of mines should not be more than one-forth of board size")
                d.setPositiveButton("OK") {dialog, which -> }
                d.show()
            }
            else {
                rows = r
                cols = c
                mines = m
            }
        }
        radioGroup.clearCheck()
    }

    //to load the best game time and last game time using shared preferences
    private fun loadTimeTaken() {
        val sharedPref = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE)
        var bt = sharedPref.getString(BEST_GAME_TIME_KEY, "--")
        var lt = sharedPref.getString(LAST_GAME_TIME_KEY, "--")
        bestTime.text = bt
        lastGameTime.text = lt
    }

}
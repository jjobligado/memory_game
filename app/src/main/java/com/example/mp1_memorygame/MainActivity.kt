package com.example.mp1_memorygame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var timerText: TextView
    private lateinit var instructionText: TextView
    private lateinit var studentInfoText: TextView
    private lateinit var cardGrid: GridLayout
    private lateinit var restartButton: Button

    // Game Logic
    private val cards = mutableListOf<Int>()
    private val cardButtons = mutableListOf<Button>()
    private var selectedCards = mutableListOf<Int>()
    private var matchedPairs = 0
    private var timer: CountDownTimer? = null
    private var isProcessing = false // To allow canceling in winGame

    // Card contents
    private val cardValues = listOf(1, 1, 2, 2, 3, 3, 4, 4, 5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        timerText = findViewById(R.id.tvTimer)
        instructionText = findViewById(R.id.tvInstructions)
        studentInfoText = findViewById(R.id.tvStudent_info)
        cardGrid = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.btnRestart)

        instructionText.text = getString(R.string.instructions)
        studentInfoText.text = getString(R.string.student_info)
        restartButton.setOnClickListener { restartGame() }
        restartButton.visibility = android.view.View.GONE
        setupCards()

        //60-second countdown
        timer = object : CountDownTimer(60000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerText.text = "$secondsRemaining seconds"
            }
            override fun onFinish() {
                timerText.text = getString(R.string.times_up)
                loseGame()
            }
        }.start()
    }

    private fun setupCards() {
        isProcessing = false  // Reset processing flag
        cards.clear()
        cards.addAll(cardValues.shuffled())
        cardButtons.clear()
        cardButtons.add(findViewById(R.id.btn1))
        cardButtons.add(findViewById(R.id.btn2))
        cardButtons.add(findViewById(R.id.btn3))
        cardButtons.add(findViewById(R.id.btn4))
        cardButtons.add(findViewById(R.id.btn5))
        cardButtons.add(findViewById(R.id.btn6))
        cardButtons.add(findViewById(R.id.btn7))
        cardButtons.add(findViewById(R.id.btn8))
        cardButtons.add(findViewById(R.id.btn9))

        // Reset and attach click listeners to each button, setting background to yellow
        for (i in 0 until 9) {
            val button = cardButtons[i]
            button.text = ""
            button.setBackgroundResource(R.drawable.button)  // Reset to default yellow
            button.setOnClickListener { onCardClick(i) }
        }
    }

    private fun onCardClick(index: Int) {
        if (isProcessing || selectedCards.size >= 2 || cards[index] == 0) return  // Block if processing, max selected, or matched
        val button = cardButtons[index]
        button.text = cards[index].toString()  // Show number on click
        button.setBackgroundResource(R.drawable.button)  // Or your reveal style if needed
        selectedCards.add(index)
        if (selectedCards.size == 2) {
            isProcessing = true  // Block further clicks
            checkMatch()
        }
    }

    private fun checkMatch() {
        val first = selectedCards[0]
        val second = selectedCards[1]
        if (cards[first] == cards[second]) {
            cardButtons[first].setBackgroundResource(R.drawable.green_match)  // Keep open if match
            cardButtons[second].setBackgroundResource(R.drawable.green_match)
            cards[first] = 0  // Mark as matched
            cards[second] = 0
            matchedPairs++
            instructionText.text = getString(R.string.pair_correct)
            if (matchedPairs == 4) {
                winGame()
            }
            selectedCards.clear()
            isProcessing = false  // Allow new clicks
        } else {
            cardButtons[first].setBackgroundResource(R.drawable.red_mismatch)  // Show mismatch
            cardButtons[second].setBackgroundResource(R.drawable.red_mismatch)
            instructionText.text = getString(R.string.no_match)
            Handler(Looper.getMainLooper()).postDelayed({
                cardButtons[first].apply {
                    text = ""  // Hide content
                    setBackgroundResource(R.drawable.button)  // Back to yellow
                }
                cardButtons[second].apply {
                    text = ""
                    setBackgroundResource(R.drawable.button)
                }
                instructionText.text = getString(R.string.instructions)
                selectedCards.clear()
                isProcessing = false  // Allow new clicks after delay
            }, 500)  // 0.5 seconds delay
        }
    }

    private fun winGame() {
        try {
            timer?.cancel()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error canceling timer: ${e.message}")
        }
        instructionText.text = getString(R.string.you_win)
        restartButton.visibility = android.view.View.VISIBLE
        for (i in cards.indices) {
            if (cards[i] != 0) {
                cardButtons[i].apply {
                    text = cards[i].toString()
                    setBackgroundResource(R.drawable.button)  // Reset to yellow if needed
                }
                break
            }
        }
    }

    private fun loseGame() {
        try {
            timer?.cancel()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error canceling timer: ${e.message}")
        }
        instructionText.text = getString(R.string.time_up_lose)
        restartButton.visibility = android.view.View.VISIBLE
        for (i in cards.indices) {
            if (cards[i] != 0) {
                cardButtons[i].apply {
                    text = ""
                    setBackgroundResource(R.drawable.button)  // Reset to yellow
                }
            }
        }
    }

    private fun restartGame() {
        matchedPairs = 0
        selectedCards.clear()
        setupCards()  // Resets everything, including to yellow
        instructionText.text = getString(R.string.instructions)
        restartButton.visibility = android.view.View.GONE
        try {
            timer?.cancel()
            timer = object : CountDownTimer(60000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    timerText.text = "$secondsRemaining seconds"
                }
                override fun onFinish() {
                    timerText.text = getString(R.string.times_up)
                    loseGame()
                }
            }.start()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error restarting timer: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
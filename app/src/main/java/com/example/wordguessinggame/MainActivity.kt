package com.example.wordguessinggameE2248295

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.wordguessinggameE2248295.api.DreamloApi
import com.example.wordguessinggameE2248295.api.FallbackWordApi
import com.example.wordguessinggameE2248295.api.LeaderboardResponse
import com.example.wordguessinggameE2248295.api.WordApi
import com.example.wordguessinggameE2248295.api.WordResponse
import com.example.wordguessinggameE2248295.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var score = 100
    private var attempts = 0
    private val MAX_ATTEMPTS = 10
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var wordApi: WordApi
    private lateinit var dreamloApi: DreamloApi
    private var secretWord: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeElapsed = 0
    private var timerRunnable: Runnable = Runnable {}
    private val executor = Executors.newSingleThreadExecutor()
    private var lastGuessTime = 0L // For debouncing

    private val API_KEY = "coa26RLMvDtos7P2t1odIw==I5lOXm8nDqmTZefS"
    private val DREAMLO_PRIVATE_CODE = "IcB3EfCa_E26m2OyUvfmoABwdj3hsJe02bfmux_Ncd4g"
    private val DREAMLO_PUBLIC_CODE = "67d54a148f40bbc224911418"

    private companion object {
        const val PREF_NAME = "UserPrefs"
        const val KEY_NAME = "name"
        const val KEY_HIGH_SCORE = "highScore"
        const val TAG = "MainActivity"
        const val DEBOUNCE_DELAY = 500L // 500ms debounce
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        Log.i(TAG, "Traversal start")
        setContentView(binding.root)
        Log.i(TAG, "Traversal end")

        // Initialize SharedPreferences and APIs asynchronously
        executor.execute {
            prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            initializeApis()
            val savedName = prefs.getString(KEY_NAME, null)
            handler.post {
                if (savedName == null) {
                    binding.nameInput.visibility = View.VISIBLE
                    binding.submitNameButton.visibility = View.VISIBLE
                    binding.messageText.text = "Enter your name to start!"
                } else {
                    binding.nameInput.visibility = View.GONE
                    binding.submitNameButton.visibility = View.GONE
                    startGame(savedName)
                }
            }
        }

        binding.submitNameButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                executor.execute {
                    prefs.edit().putString(KEY_NAME, name).apply()
                    handler.post {
                        binding.nameInput.visibility = View.GONE
                        binding.submitNameButton.visibility = View.GONE
                        startGame(name)
                    }
                }
            } else {
                handler.post { binding.messageText.text = "Please enter a name!" }
            }
        }
    }

    private fun initializeApis() {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Api-Key", API_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        wordApi = retrofit.create(WordApi::class.java)

        val dreamloRetrofit = Retrofit.Builder()
            .baseUrl("http://dreamlo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        dreamloApi = dreamloRetrofit.create(DreamloApi::class.java)
    }

    private fun startGame(userName: String) {
        handler.post {
            binding.messageText.text = "Welcome, $userName! Game starting soon..."
            updateScoreText()
            binding.timerText.text = "Time: 0s"
        }
        fetchRandomWord()

        binding.guessButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastGuessTime < DEBOUNCE_DELAY) return@setOnClickListener
            lastGuessTime = currentTime
            Log.i(TAG, "Guess button clicked")

            val guess = binding.guessInput.text.toString().trim().toLowerCase()
            if (guess.isEmpty()) {
                handler.post { binding.messageText.text = "Please enter a guess!" }
                return@setOnClickListener
            }

            attempts++
            if (guess == secretWord?.toLowerCase()) {
                stopTimer()
                val currentScore = score
                updateHighScore(currentScore)
                handler.post {
                    val message = "Correct! Score: $currentScore, Time: ${timeElapsed}s\nYour score ($currentScore) added!"
                    binding.messageText.text = message
                    Log.i(TAG, "Correct guess! Adding to leaderboard: $userName, $currentScore, $timeElapsed")
                    addScoreToLeaderboard(userName, currentScore, timeElapsed)
                    score = 100
                    attempts = 0
                    updateScoreText()
                    fetchRandomWord()
                }
            } else {
                score -= 10
                Log.i(TAG, "Wrong guess, new score: $score, attempts: $attempts")
                handler.post {
                    updateScoreText()
                    binding.messageText.text = "Wrong guess! Attempts: $attempts"
                    if (score <= 0 || attempts >= MAX_ATTEMPTS) {
                        stopTimer()
                        binding.messageText.text = "Game over! Starting new game..."
                        score = 100
                        attempts = 0
                        updateScoreText()
                        fetchRandomWord()
                    }
                }
            }
            handler.post { binding.guessInput.text.clear() }
        }

        binding.clueLettersButton.setOnClickListener {
            if (score >= 5) {
                score -= 5
                Log.i(TAG, "Clue (Letter Count) used, new score: $score")
                handler.post {
                    updateScoreText()
                    binding.messageText.text = "Word has ${secretWord?.length ?: 0} letters."
                }
            } else {
                handler.post { binding.messageText.text = "Not enough points!" }
            }
        }

        binding.clueOccurrenceButton.setOnClickListener {
            if (score >= 5) {
                val guess = binding.guessInput.text.toString().trim()
                if (guess.length != 1) {
                    handler.post { binding.messageText.text = "Enter one letter in the guess field!" }
                    return@setOnClickListener
                }
                score -= 5
                Log.i(TAG, "Clue (Occurrence) used, new score: $score")
                handler.post {
                    updateScoreText()
                    val letter = guess[0]
                    val count = secretWord?.count { it == letter } ?: 0
                    binding.messageText.text = "Letter '$letter' appears $count times."
                }
            } else {
                handler.post { binding.messageText.text = "Not enough points!" }
            }
        }

        binding.clueTipButton.setOnClickListener {
            if (attempts < 5) {
                handler.post { binding.messageText.text = "Tip available after 5 attempts!" }
            } else if (score >= 5) {
                score -= 5
                Log.i(TAG, "Clue (Rhyme Tip) used, new score: $score")
                handler.post {
                    updateScoreText()
                    fetchRhymingWord(secretWord ?: "word")
                }
            } else {
                handler.post { binding.messageText.text = "Not enough points!" }
            }
        }
    }

    private fun updateScoreText() {
        binding.scoreText.text = "Score: $score"
    }

    private fun fetchRandomWord() {
        executor.execute {
            val call = wordApi.getRandomWord()
            call.enqueue(object : Callback<WordResponse> {
                override fun onResponse(call: Call<WordResponse>, response: Response<WordResponse>) {
                    Log.i(TAG, "Random word response: Code=${response.code()}")
                    if (response.isSuccessful && response.body() != null) {
                        val words = response.body()!!.word
                        secretWord = words.firstOrNull() ?: "default"
                        Log.i(TAG, "Random word fetched: $secretWord")
                        handler.post {
                            binding.messageText.text = "Word fetched! Start guessing."
                            binding.timerText.text = "Time: 0s"
                            startTimer()
                        }
                    } else {
                        Log.e(TAG, "Random word fetch failed: ${response.code()}")
                        fetchFallbackWord()
                    }
                }

                override fun onFailure(call: Call<WordResponse>, t: Throwable) {
                    Log.e(TAG, "Network failure fetching random word: ${t.message}", t)
                    fetchFallbackWord()
                }
            })
        }
    }

    private fun fetchFallbackWord() {
        val fallbackRetrofit = Retrofit.Builder()
            .baseUrl("https://random-word-api.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val fallbackApi = fallbackRetrofit.create(FallbackWordApi::class.java)
        executor.execute {
            val call = fallbackApi.getWord()
            call.enqueue(object : Callback<List<String>> {
                override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    Log.i(TAG, "Fallback word response: Code=${response.code()}")
                    if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                        secretWord = response.body()!![0]
                        Log.i(TAG, "Fallback word fetched: $secretWord")
                        handler.post {
                            binding.messageText.text = "Fallback word fetched! Start guessing."
                            binding.timerText.text = "Time: 0s"
                            startTimer()
                        }
                    } else {
                        Log.e(TAG, "Fallback fetch failed: ${response.code()}")
                        handler.post { binding.messageText.text = "Fallback failed: ${response.code()}" }
                    }
                }

                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    Log.e(TAG, "Network failure fetching fallback word: ${t.message}", t)
                    handler.post { binding.messageText.text = "Fallback error: ${t.message ?: "Unknown"}" }
                }
            })
        }
    }

    private fun fetchRhymingWord(word: String) {
        executor.execute {
            val call = wordApi.getRhymingWord(word)
            call.enqueue(object : Callback<List<String>> {
                override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    Log.i(TAG, "Rhyme response: Code=${response.code()}")
                    if (response.isSuccessful && response.body() != null) {
                        val rhymes = response.body()!!
                        Log.i(TAG, "Raw rhyme response: $rhymes")
                        val rhyme = rhymes.firstOrNull() ?: getFallbackRhyme(word)
                        handler.post { binding.messageText.text = "Tip: The word rhymes with '$rhyme'." }
                    } else {
                        Log.e(TAG, "Rhyme fetch failed: ${response.code()}")
                        handler.post { binding.messageText.text = "Tip: The word rhymes with 'something'." }
                    }
                }

                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    Log.e(TAG, "Network failure fetching rhyme: ${t.message}", t)
                    handler.post { binding.messageText.text = "Tip: The word rhymes with 'something'." }
                }
            })
        }
    }

    private fun getFallbackRhyme(word: String): String {
        val endings = mapOf(
            "ing" to "sing",
            "ed" to "bed",
            "er" to "river",
            "ae" to "day"
        )
        return endings.entries.find { word.endsWith(it.key) }?.value ?: "something"
    }

    private fun addScoreToLeaderboard(playerName: String, score: Int, timeInSeconds: Int) {
        val entry = "$playerName-${System.currentTimeMillis()}"
        executor.execute {
            val call = dreamloApi.addScore(DREAMLO_PRIVATE_CODE, entry, score, timeInSeconds)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.i(TAG, "Leaderboard add response: Code=${response.code()}")
                    if (response.isSuccessful) {
                        Log.i(TAG, "Score added to leaderboard successfully")
                        handler.post {
                            val currentText = binding.messageText.text.toString()
                            binding.messageText.text = "$currentText\nYour score ($score) added!"
                            fetchLeaderboard(playerName, entry, score, timeInSeconds)
                        }
                    } else {
                        Log.e(TAG, "Leaderboard add failed: ${response.code()}")
                        handler.post { binding.messageText.text = "${binding.messageText.text}\nFailed to add score: ${response.code()}" }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "Leaderboard add error: ${t.message}", t)
                    handler.post { binding.messageText.text = "${binding.messageText.text}\nLeaderboard error: ${t.message}" }
                }
            })
        }
    }

    private fun fetchLeaderboard(playerName: String, recentEntry: String, recentScore: Int, recentTime: Int) {
        executor.execute {
            val call = dreamloApi.getLeaderboard(DREAMLO_PUBLIC_CODE, "json", "all")
            call.enqueue(object : Callback<LeaderboardResponse> {
                override fun onResponse(call: Call<LeaderboardResponse>, response: Response<LeaderboardResponse>) {
                    Log.i(TAG, "Raw leaderboard response: ${response.body()}")
                    if (response.isSuccessful && response.body() != null) {
                        val entries = response.body()!!.dreamlo.leaderboard.entry
                        val latestEntry = entries.find { it.name == recentEntry && it.score == recentScore && it.seconds == recentTime }
                            ?: entries.filter { it.name.startsWith(playerName) }.maxByOrNull { it.score }
                        handler.post {
                            if (latestEntry != null) {
                                Log.i(TAG, "Leaderboard fetched: ${latestEntry.name}, ${latestEntry.score}, ${latestEntry.seconds}")
                                val currentText = binding.messageText.text.toString()
                                binding.messageText.text = "$currentText\nYour Latest Score: ${latestEntry.name} - ${latestEntry.score}"
                            } else {
                                Log.i(TAG, "No matching leaderboard entries for $playerName")
                                binding.messageText.text = "${binding.messageText.text}\nNo scores found for $playerName"
                            }
                        }
                    } else {
                        Log.e(TAG, "Leaderboard fetch failed: ${response.code()}")
                        handler.post { binding.messageText.text = "Failed to fetch leaderboard: ${response.code()}" }
                    }
                }

                override fun onFailure(call: Call<LeaderboardResponse>, t: Throwable) {
                    Log.e(TAG, "Leaderboard fetch error: ${t.message}", t)
                    handler.post { binding.messageText.text = "Leaderboard error: ${t.message}" }
                }
            })
        }
    }

    private fun startTimer() {
        timeElapsed = 0
        timerRunnable = object : Runnable {
            override fun run() {
                timeElapsed++
                if (timeElapsed % 2 == 0) { // Update every 2 seconds to reduce UI load
                    handler.post { binding.timerText.text = "Time: ${timeElapsed}s" }
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    private fun updateHighScore(currentScore: Int) {
        executor.execute {
            val highScore = prefs.getInt(KEY_HIGH_SCORE, 0)
            if (currentScore > highScore) {
                prefs.edit().putInt(KEY_HIGH_SCORE, currentScore).apply()
                handler.post {
                    val currentText = binding.messageText.text.toString()
                    binding.messageText.text = "$currentText\nNew High Score: $currentScore!"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        executor.shutdown()
    }
}
package com.example.wordguessinggameE2248295.api

data class WordResponse(val word: List<String>)
typealias RhymeResponse = List<String>
data class LeaderboardResponse(val dreamlo: Dreamlo)
data class Dreamlo(val leaderboard: Leaderboard)
data class Leaderboard(
    val entry: List<Entry> // Changed to List<Entry> to match the array
)
data class Entry(val name: String, val score: Int, val seconds: Int)
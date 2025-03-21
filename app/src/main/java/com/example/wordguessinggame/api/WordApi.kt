package com.example.wordguessinggameE2248295.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WordApi {
    @GET("randomword")
    fun getRandomWord(): Call<WordResponse>

    @GET("rhyme")
    fun getRhymingWord(@Query("word") word: String): Call<List<String>> // Matches typealias RhymeResponse
}
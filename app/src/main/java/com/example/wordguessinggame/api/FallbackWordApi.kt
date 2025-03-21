package com.example.wordguessinggameE2248295.api

import retrofit2.Call
import retrofit2.http.GET

interface FallbackWordApi {
    @GET("word")
    fun getWord(): Call<List<String>>
}
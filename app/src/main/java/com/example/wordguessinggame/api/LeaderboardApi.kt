package com.example.wordguessinggameE2248295.api

import retrofit2.Call
import retrofit2.http.GET

interface LeaderboardApi {
    @GET("lb/<YOUR_API_KEY>/json")
    fun getLeaderboard(): Call<LeaderboardResponse>
}
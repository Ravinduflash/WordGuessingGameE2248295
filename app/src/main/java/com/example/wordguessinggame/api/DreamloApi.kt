package com.example.wordguessinggameE2248295.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DreamloApi {
    @GET("lb/{privateCode}/add/{entry}/{score}/{seconds}")
    fun addScore(
        @Path("privateCode") privateCode: String,
        @Path("entry") entry: String,
        @Path("score") score: Int,
        @Path("seconds") seconds: Int
    ): Call<Void>

    @GET("lb/{publicCode}/{format}/{mode}")
    fun getLeaderboard(
        @Path("publicCode") publicCode: String,
        @Path("format") format: String = "json",
        @Path("mode") mode: String = "all"
    ): Call<LeaderboardResponse>
}
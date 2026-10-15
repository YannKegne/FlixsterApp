package com.example.flixster

import org.json.JSONArray
import org.json.JSONObject

/** One movie from TMDB's now_playing endpoint. */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val rating: Double,
    val releaseDate: String
) {
    /** Portrait poster image (used in portrait mode). */
    val posterUrl: String?
        get() = posterPath?.let { "$IMAGE_BASE_URL/w342$it" }

    /** Wide backdrop image (used in landscape mode). Falls back to the poster. */
    val backdropUrl: String?
        get() = backdropPath?.let { "$IMAGE_BASE_URL/w780$it" } ?: posterUrl

    companion object {
        private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p"

        /** Parses the "results" array of the now_playing JSON response. */
        fun fromJsonArray(results: JSONArray): List<Movie> =
            (0 until results.length()).map { i -> fromJson(results.getJSONObject(i)) }

        private fun fromJson(json: JSONObject) = Movie(
            id = json.getInt("id"),
            title = json.optString("title"),
            overview = json.optString("overview"),
            posterPath = json.optString("poster_path").takeIf { it.isNotBlank() && it != "null" },
            backdropPath = json.optString("backdrop_path").takeIf { it.isNotBlank() && it != "null" },
            rating = json.optDouble("vote_average", 0.0),
            releaseDate = json.optString("release_date")
        )
    }
}

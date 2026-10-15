package com.example.flixster

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

private const val TAG = "MainActivity"
private const val NOW_PLAYING_URL = "https://api.themoviedb.org/3/movie/now_playing"

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val movies = mutableListOf<Movie>()
    private val adapter = MovieAdapter(movies)

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.moviesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        retryButton.setOnClickListener { fetchNowPlaying() }

        fetchNowPlaying()
    }

    /** Requests TMDB's now_playing endpoint, parses the JSON and fills the RecyclerView. */
    private fun fetchNowPlaying() {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            showError(getString(R.string.error_missing_key))
            return
        }

        showLoading()
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("$NOW_PLAYING_URL?api_key=${BuildConfig.TMDB_API_KEY}&language=en-US&page=1")
                        .build()
                    client.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            throw IllegalStateException("HTTP ${response.code}: $body")
                        }
                        // Parse JSON: { "results": [ {movie}, {movie}, ... ] }
                        Movie.fromJsonArray(JSONObject(body).getJSONArray("results"))
                    }
                }
                Log.i(TAG, "Loaded ${result.size} movies")
                movies.clear()
                movies.addAll(result)
                adapter.notifyDataSetChanged()
                showList()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load movies", e)
                showError(getString(R.string.error_loading))
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
    }

    private fun showList() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorText.text = message
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
    }
}

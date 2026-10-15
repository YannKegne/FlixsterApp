package com.example.flixster

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.util.Locale

/** Binds Movie objects to item_movie.xml rows. */
class MovieAdapter(private val movies: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val poster: ImageView = view.findViewById(R.id.moviePoster)
        val title: TextView = view.findViewById(R.id.movieTitle)
        val rating: TextView = view.findViewById(R.id.movieRating)
        val overview: TextView = view.findViewById(R.id.movieOverview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        val context = holder.itemView.context

        holder.title.text = movie.title
        holder.overview.text = movie.overview
        holder.rating.text = context.getString(
            R.string.rating_format,
            String.format(Locale.getDefault(), "%.1f", movie.rating),
            movie.releaseDate.take(4)
        )

        // Portrait shows the tall poster; landscape shows the wide backdrop.
        val isLandscape = context.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        val imageUrl = if (isLandscape) movie.backdropUrl else movie.posterUrl

        // Glide: load the image, show a placeholder while loading, and an error image if it fails.
        val radius = (12 * context.resources.displayMetrics.density).toInt()
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.poster_placeholder)
            .error(R.drawable.poster_error)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(holder.poster)
    }

    override fun getItemCount() = movies.size
}

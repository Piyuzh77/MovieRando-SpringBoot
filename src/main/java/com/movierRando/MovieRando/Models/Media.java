package com.movierRando.MovieRando.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data 
public class Media {
    private Long id;

    @JsonProperty("title")
    private String title;          // For movies

    @JsonProperty("name")
    private String name;           // For TV

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("original_name")
    private String originalName;

    private String overview;

    @JsonProperty("original_language")
    private String language;

    @JsonProperty("vote_average")
    private Double rating;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    private Double popularity;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    @JsonProperty("media_type")
    private String mediaType;      // "movie" or "tv"

    private String imdbId;
    private String stremioLink;

    public String getDisplayTitle() {
        return title != null ? title : name;
    }

    public String getDisplayOriginalTitle() {
        return originalTitle != null ? originalTitle : originalName;
    }

    public String getDisplayReleaseDate() {
        return releaseDate != null ? releaseDate : firstAirDate;
    }

}

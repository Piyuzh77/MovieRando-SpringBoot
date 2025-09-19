package com.movierRando.MovieRando.Services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.movierRando.MovieRando.Models.Media;
import com.movierRando.MovieRando.Models.MediaResponse;


@Service
public class MediaService {

    // @Value("${ TMDB_API_KEY}")
    private final String apiKey;
    private final String BASE_URL = "https://api.themoviedb.org/3";
    @Autowired
    private final APIConfigService APIConfigService;

    public MediaService(@Value("${TMDB_API_KEY}") String apiKey, APIConfigService APIConfigService) {
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("TMDB_API_KEY is missing in environment variables.");
        }
        this.APIConfigService = APIConfigService;
    }

    // Utility Methods
    private MediaResponse emptyResponse() {
        MediaResponse empty = new MediaResponse();
        empty.setResults(Collections.emptyList());
        empty.setTotalPages(0);
        empty.setPage(0);
        return empty;
    }

    // Random Media
    public Media getRandomMedia(String mediaType, String language, String genre) {
        Random random = new Random();

        
        String url = BASE_URL + "/discover/" + mediaType + "?api_key=" + apiKey
                + (genre != null ? "&with_genres=" + genre : "")
                + (language != null ? "&with_original_language=" + language : "");

        try {
            MediaResponse totalPagesResponse = APIConfigService.fetchData(url, MediaResponse.class);
            if (totalPagesResponse == null || totalPagesResponse.getTotalPages() == 0) {
                return null;
            }

        
            int maxPage = Math.min(totalPagesResponse.getTotalPages(), 500);
            int randomPage = 1 + random.nextInt(maxPage);
            String pageUrl = url + "&page=" + randomPage;

            MediaResponse response = APIConfigService.fetchData(pageUrl, MediaResponse.class);
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                return response.getResults().get(random.nextInt(response.getResults().size()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Cacheable(value = "searchMediaCache", key = "#mediaType + '-' + #name + '-' + #genre + '-' + #minRating + '-' + #language + '-' + #year + '-' + #page")
    public MediaResponse searchMedia(String mediaType, String name, String genre, String minRating,
            String language, String year, Integer page) {

        if (!"movie".equalsIgnoreCase(mediaType) && !"tv".equalsIgnoreCase(mediaType)) {
            return emptyResponse();
        }

        // Base URL
        StringBuilder url = new StringBuilder(BASE_URL);

        if (name != null && !name.isBlank()) {
            url.append("/search/").append(mediaType)
                    .append("?api_key=").append(apiKey)
                    .append("&query=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        } else {
            url.append("/discover/").append(mediaType)
                    .append("?api_key=").append(apiKey);
        }

        // Optional Filters
        if (genre != null && !genre.isBlank())
            url.append("&with_genres=").append(genre);
        if (minRating != null && !minRating.isBlank())
            url.append("&vote_average.gte=").append(minRating);
        if (language != null && !language.isBlank())
            url.append("&with_original_language=").append(language);
        if (year != null && !year.isBlank()) {
            if ("movie".equalsIgnoreCase(mediaType))
                url.append("&year=").append(year);
            else
                url.append("&first_air_date_year=").append(year);
        }

        // Pagination
        if (page != null && page > 0)
            url.append("&page=").append(page);
        else
            url.append("&page=1"); // default to page 1

        try {
            MediaResponse response = APIConfigService.fetchData(url.toString(), MediaResponse.class);
            // System.out.println("Constructed URL: " + url.toString());

            if (response != null)
                return response;
        } catch (Exception e) {
            e.printStackTrace();
            return emptyResponse();
        }
        return emptyResponse();
    }

}

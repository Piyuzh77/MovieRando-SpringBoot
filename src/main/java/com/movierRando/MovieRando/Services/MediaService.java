package com.movierRando.MovieRando.Services;

import java.util.concurrent.CompletableFuture;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.movierRando.MovieRando.Models.Media;
import com.movierRando.MovieRando.Models.MediaResponse;

@Service
public class MediaService {

    private final String apiKey;
    private final String BASE_URL = "https://api.themoviedb.org/3";
    private final APIConfigService apiConfigService;
    private final StremioLinkService stremioLinkService;
    private final RestTemplate restTemplate;

    public MediaService(@Value("${TMDB_API_KEY}") String apiKey,
            APIConfigService apiConfigService,
            StremioLinkService stremioLinkService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("TMDB_API_KEY is missing in environment variables.");
        }
        this.apiKey = apiKey;
        this.apiConfigService = apiConfigService;
        this.stremioLinkService = stremioLinkService;
    }

    private MediaResponse emptyResponse() {
        MediaResponse empty = new MediaResponse();
        empty.setResults(Collections.emptyList());
        empty.setTotalPages(0);
        empty.setPage(0);
        return empty;
    }

    //get random media, movie or tv

    public Media getRandomMedia(String mediaType, String language, String genre) {
        Random random = new Random();
        String url = BASE_URL + "/discover/" + mediaType + "?api_key=" + apiKey
                + (genre != null ? "&with_genres=" + genre : "")
                + (language != null ? "&with_original_language=" + language : "");

        try {
            MediaResponse totalPagesResponse = apiConfigService.fetchData(url, MediaResponse.class);
            if (totalPagesResponse == null || totalPagesResponse.getTotalPages() == 0)
                return null;

            int maxPage = Math.min(totalPagesResponse.getTotalPages(), 500);
            int randomPage = 1 + random.nextInt(maxPage);
            MediaResponse response = apiConfigService.fetchData(url + "&page=" + randomPage, MediaResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                Media media = response.getResults().get(random.nextInt(response.getResults().size()));
                attachImdbAndStremio(media, mediaType);
                return media;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //everything related to searching based on the parameters

    @Cacheable(value = "searchMediaCache", key = "#mediaType + '-' + #name + '-' + #genre + '-' + #minRating + '-' + #language + '-' + #year + '-' + #page")
    public MediaResponse searchMedia(String mediaType, String name, String genre, String minRating,
            String language, String year, Integer page) {

        if (!"movie".equalsIgnoreCase(mediaType) && !"tv".equalsIgnoreCase(mediaType)) {
            return emptyResponse();
        }

        StringBuilder url = new StringBuilder(BASE_URL);
        if (name != null && !name.isBlank()) {
            url.append("/search/").append(mediaType)
                    .append("?api_key=").append(apiKey)
                    .append("&query=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        } else {
            url.append("/discover/").append(mediaType).append("?api_key=").append(apiKey);
        }

        if (genre != null && !genre.isBlank())
            url.append("&with_genres=").append(genre);
        if (minRating != null && !minRating.isBlank())
            url.append("&vote_average.gte=").append(minRating);
        if (language != null && !language.isBlank())
            url.append("&with_original_language=").append(language);
        if (year != null && !year.isBlank()) {
            url.append("movie".equalsIgnoreCase(mediaType) ? "&year=" + year : "&first_air_date_year=" + year);
        }

        url.append("&page=").append(page != null && page > 0 ? page : 1);

        try {
            MediaResponse response = apiConfigService.fetchData(url.toString(), MediaResponse.class);

            if (response != null && response.getResults() != null) {
                List<CompletableFuture<Void>> futures = response.getResults().stream()
                        .map(media -> CompletableFuture.runAsync(() -> {
                            if (media.getMediaType() == null || media.getMediaType().isBlank()) {
                                media.setMediaType(mediaType);
                            }
                            attachImdbAndStremio(media, media.getMediaType());
                        }))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            return response != null ? response : emptyResponse();

        } catch (Exception e) {
            e.printStackTrace();
            return emptyResponse();
        }
    }

    // service to fetch the imdb id from all that json mess

    @SuppressWarnings("unchecked")
    public Optional<String> getImdbId(String mediaType, Long id) {
        String url;
        if ("movie".equalsIgnoreCase(mediaType)) {
            url = String.format("%s/movie/%d/external_ids?api_key=%s", BASE_URL, id, apiKey);
        } else if ("tv".equalsIgnoreCase(mediaType)) {
            url = String.format("%s/tv/%d/external_ids?api_key=%s", BASE_URL, id, apiKey);
        } else {
            throw new IllegalArgumentException("Unknown media type: " + mediaType);
        }
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("imdb_id") != null) {
                return Optional.of(response.get("imdb_id").toString());
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch IMDb ID for " + mediaType + " id " + id + ": " + e.getMessage());
        }

        return Optional.empty();
    }


    //utilities
    private void attachImdbAndStremio(Media media, String mediaType) {
        String imdbId = apiConfigService.fetchImdbId(media.getId(), mediaType, apiKey);
        media.setImdbId(imdbId);
        media.setStremioLink(stremioLinkService.generateLink(media));
    }
    
}

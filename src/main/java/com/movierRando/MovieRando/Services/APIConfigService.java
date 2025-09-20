package com.movierRando.MovieRando.Services;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Configuration
public class APIConfigService {

    private final RestTemplate restTemplate;

    // Simple in-memory cache for TMDB ID -> IMDb ID
    private final Map<Long, String> imdbCache = new ConcurrentHashMap<>();

    // Inject the global RestTemplate from AppConfig
    public APIConfigService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
        value = { ResourceAccessException.class },
        maxAttempts = 5,
        backoff = @Backoff(delay = 2000)
    )
    public <T> T fetchData(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching data from API: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public String fetchImdbId(Long tmdbId, String mediaType, String apiKey) {
        if (tmdbId == null) return null;

        if (imdbCache.containsKey(tmdbId)) return imdbCache.get(tmdbId);

        String url = "https://api.themoviedb.org/3/" + mediaType + "/" + tmdbId + "/external_ids?api_key=" + apiKey;
        try {
            Map<String, Object> ids = fetchData(url, Map.class);
            if (ids != null && ids.get("imdb_id") != null) {
                String imdbId = ids.get("imdb_id").toString();
                imdbCache.put(tmdbId, imdbId);
                return imdbId;
            }
        } catch (Exception e) {
            // Fail silently for a single media item
        }

        return null;
    }
}
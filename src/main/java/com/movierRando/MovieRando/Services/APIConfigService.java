package com.movierRando.MovieRando.Services;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Configuration
public class APIConfigService {
    

    private final RestTemplate restTemplate;

    public APIConfigService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
        value = { ResourceAccessException.class }, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000)
        )
    // basically wraps the api call for retries and error handling
    // <T> means that the method will handle any type of response (media or mediaService)
    // Class<T> in parameter means that it will get the class info of the response and use it to map the JSON response to that class type
    public <T> T fetchData(String url, Class<T> responseType) {
        try {
            // getForObject gets the data and maps the JSON Object to the DTO
            // similarly getForEntity returns a ResponseEntity which contains more info like status code, headers, etc.
            // internally getForObject also works similarly as our fetchData method, <T> T means to work on any type of response and return that type  of response.
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from API: " + e.getMessage(), e);
        }
    }
}

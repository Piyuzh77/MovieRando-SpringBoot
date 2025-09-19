package com.movierRando.MovieRando.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.movierRando.MovieRando.Models.Media;
import com.movierRando.MovieRando.Models.MediaResponse;
import com.movierRando.MovieRando.Services.MediaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @GetMapping("/random")
    public ResponseEntity<Media> getRandomMedia(@RequestParam (defaultValue = "movie") String type, @RequestParam(required = false) String genre,@RequestParam(required = false) String language) {

        Media result= mediaService.getRandomMedia(type, language, genre);
        if(result==null){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

     @GetMapping("/search")
    public ResponseEntity<MediaResponse> searchMedia(
            @RequestParam(defaultValue = "movie") String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String minRating,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Integer page
    ) {
        MediaResponse response = mediaService.searchMedia(type, name, genre, minRating, language, year,page);
        if (response == null || response.getResults().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
    
    
}

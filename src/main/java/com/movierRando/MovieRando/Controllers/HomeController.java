package com.movierRando.MovieRando.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "🎬 Welcome to 🎥MovieRando📽️!!! Your gateway to SERIOUS!! movie recommendations. \n \n TRY /media/random to get a random MOVIE recommendation or use /media/random/?type=tv to get a random TV SHOW recommendation.!! to know more visit https://github.com/Piyuzh77/MovieRando-SpringBoot ";
    }
}

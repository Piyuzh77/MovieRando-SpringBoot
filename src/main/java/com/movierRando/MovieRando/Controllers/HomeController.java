package com.movierRando.MovieRando.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
   @GetMapping(value = "/", produces = "text/html")
public String home() {
    return """
           🎬 Welcome to 🎥MovieRando📽️!!! Your gateway to SERIOUS!! movie recommendations.
           <br><br>
           TRY /media/random to get a random MOVIE recommendation or use /media/random/?type=tv to get a random TV SHOW recommendation.!!
           <br><br>
           To know more visit <a href="https://github.com/Piyuzh77/MovieRando-SpringBoot">Here!!</a>
           """;
    }
}

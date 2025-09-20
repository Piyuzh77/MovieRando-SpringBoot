package com.movierRando.MovieRando.Services;

import org.springframework.stereotype.Service;
import com.movierRando.MovieRando.Models.Media;

@Service
public class StremioLinkService {

   public String generateLink(Media media) {
    if (media.getImdbId() == null || media.getImdbId().isBlank()) {
        return null;
    }
    String type;
    if ("movie".equalsIgnoreCase(media.getMediaType())) {
        type = "movie";
    } else if ("tv".equalsIgnoreCase(media.getMediaType())) {
        type = "series";
    } else {
        type = "movie"; 
    }
    return "stremio:///detail/" + type + "/" + media.getImdbId();
}
}

package app.project.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import app.project.model.Media;
import app.project.repository.MediaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaRepository mediaRepository;

    private static final Logger logger = Logger.getLogger(MediaController.class.getName());

    @GetMapping
    public Flux<Media> getAllMedia() {
        logger.info("Accessed all media items");
        return mediaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Media> getMediaById(@PathVariable Long id) {
        logger.info("Accessed media item with ID: " + id);
        return mediaRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Media> createMedia(@RequestBody Media media) {
        logger.info("Created new media item: " + media.getTitle());
        return mediaRepository.save(media);
    }

    @PutMapping("/{id}")
    public Mono<Media> updateMedia(@PathVariable Long id, @RequestBody Media updatedMedia) {
        logger.info("Updating media item with ID: " + id);
        return mediaRepository.findById(id)
            .flatMap(existingMedia -> {
                existingMedia.setTitle(updatedMedia.getTitle());
                existingMedia.setReleaseDate(updatedMedia.getReleaseDate());
                existingMedia.setAverageRating(updatedMedia.getAverageRating());
                existingMedia.setType(updatedMedia.getType());
                logger.info("Updated media item with ID: " + id);
                return mediaRepository.save(existingMedia);
            });
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteMedia(@PathVariable Long id) {
        logger.info("Deleting media item with ID: " + id);
        return mediaRepository.deleteById(id);
    }
}

package app.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import app.project.model.Media;
import app.project.service.MediaService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/media")
@Slf4j
public class MediaController {

    @Autowired
    private MediaService mediaService;

    // Retrieve all media items
    @GetMapping
    public Flux<Media> getAllMedia() {
        log.info("Accessed all media items");
        return mediaService.getAllMedia();
    }

    // Retrieve a specific media item by ID
    @GetMapping("/{id}")
    public Mono<Media> getMediaById(@PathVariable Long id) {
        log.info("Accessed media item with ID: {}", id);
        return mediaService.getMediaById(id);
    }

    // Create a new media item
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Media> createMedia(@RequestBody Media media) {
        log.info("Creating new media item: {}", media.getTitle());
        return mediaService.createMedia(media);
    }

    // Update an existing media item by ID
    @PutMapping("/{id}")
    public Mono<Media> updateMedia(@PathVariable Long id, @RequestBody Media updatedMedia) {
        log.info("Updating media item with ID: {}", id);
        return mediaService.updateMedia(id, updatedMedia);
    }

    // Delete a media item by ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMedia(@PathVariable Long id) {
        log.info("Deleting media item with ID: {}", id);
        return mediaService.deleteMedia(id);
    }
}

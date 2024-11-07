package app.project.controller;

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

    @GetMapping
    public Flux<Media> getAllMedia() {
        log.info("Accessed all media items");
        return mediaService.getAllMedia();
    }

    @GetMapping("/{id}")
    public Mono<Media> getMediaById(@PathVariable Long id) {
        log.info("Accessed media item with ID: {}", id);
        return mediaService.getMediaById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Media> createMedia(@RequestBody Media media) {
        log.info("Creating new media item: {}", media.getTitle());
        return mediaService.createMedia(media);
    }

    @PutMapping("/{id}")
    public Mono<Media> updateMedia(@PathVariable Long id, @RequestBody Media updatedMedia) {
        log.info("Updating media item with ID: {}", id);
        return mediaService.updateMedia(id, updatedMedia);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMedia(@PathVariable Long id) {
        log.info("Deleting media item with ID: {}", id);
        return mediaService.deleteMedia(id);
    }
}

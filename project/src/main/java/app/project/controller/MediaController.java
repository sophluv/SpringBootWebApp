package app.project.controller;

import app.project.model.Media;
import app.project.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaRepository mediaRepository;

    @GetMapping
    public Flux<Media> getAllMedia() {
        return mediaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Media> getMediaById(@PathVariable Long id) {
        return mediaRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Media> createMedia(@RequestBody Media media) {
        return mediaRepository.save(media);
    }

    @PutMapping("/{id}")
    public Mono<Media> updateMedia(@PathVariable Long id, @RequestBody Media updatedMedia) {
        return mediaRepository.findById(id)
            .flatMap(existingMedia -> {
                existingMedia.setTitle(updatedMedia.getTitle());
                existingMedia.setReleaseDate(updatedMedia.getReleaseDate());
                existingMedia.setAverageRating(updatedMedia.getAverageRating());
                existingMedia.setType(updatedMedia.getType());
                return mediaRepository.save(existingMedia);
            });
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteMedia(@PathVariable Long id) {
        return mediaRepository.deleteById(id);
    }
}
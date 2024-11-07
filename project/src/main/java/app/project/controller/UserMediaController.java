package app.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import app.project.model.UserMedia;
import app.project.service.UserMediaService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user-media")
@Slf4j
public class UserMediaController {

    @Autowired
    private UserMediaService userMediaService;

    // Retrieve all user-media relationships
    @GetMapping
    public Flux<UserMedia> getAllUserMedia() {
        log.info("Retrieving all user-media relationships");
        return userMediaService.getAllUserMedia();
    }

    // Retrieve a specific user-media relationship by ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserMedia>> getUserMediaById(@PathVariable Long id) {
        log.info("Retrieving user-media relationship with ID: {}", id);
        return userMediaService.getUserMediaById(id)
                .map(userMedia -> new ResponseEntity<>(userMedia, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Link a user to a media item (create a user-media relationship)
    @PostMapping("/link")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserMedia> linkUserToMedia(@RequestParam Long userId, @RequestParam Long mediaId) {
        log.info("Linking user ID {} to media ID {}", userId, mediaId);
        return userMediaService.addUserMediaRelationship(userId, mediaId);
    }

    // Delete a user-media relationship by ID
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUserMedia(@PathVariable Long id) {
        log.info("Deleting user-media relationship with ID: {}", id);
        return userMediaService.deleteUserMedia(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

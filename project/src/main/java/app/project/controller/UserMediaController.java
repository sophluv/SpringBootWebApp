package app.project.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import app.project.model.UserMedia;
import app.project.repository.UserMediaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user-media")
public class UserMediaController {

    @Autowired
    private UserMediaRepository userMediaRepository;

    private static final Logger logger = Logger.getLogger(UserMediaController.class.getName());

    // Retrieve all user-media relationships
    @GetMapping
    public Flux<UserMedia> getAllUserMedia() {
        logger.info("Retrieving all user-media relationships");
        return userMediaRepository.findAll();
    }

    // Retrieve a specific user-media relationship by ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserMedia>> getUserMediaById(@PathVariable Long id) {
        logger.info("Retrieving user-media relationship with ID: " + id);
        return userMediaRepository.findById(id)
                .map(userMedia -> new ResponseEntity<>(userMedia, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create a new user-media relationship
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserMedia> createUserMedia(@RequestBody UserMedia userMedia) {
        logger.info("Creating new user-media relationship");
        return userMediaRepository.save(userMedia);
    }

    // Delete a user-media relationship by ID
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUserMedia(@PathVariable Long id) {
        logger.info("Deleting user-media relationship with ID: " + id);
        return userMediaRepository.findById(id)
                .flatMap(existingUserMedia -> 
                        userMediaRepository.delete(existingUserMedia)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

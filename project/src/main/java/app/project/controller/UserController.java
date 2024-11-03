package app.project.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import app.project.model.User;
import app.project.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    // Retrieve all users
    @GetMapping
    public Flux<User> getAllUsers() {
        logger.info("Retrieving all users");
        return userRepository.findAll();
    }

    // Retrieve a specific user by ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        logger.info(String.format("Retrieving user with ID: %d", id));
        return userRepository.findById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create a new user
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> createUser(@RequestBody User user) {
        logger.info("Creating new user");
        return userRepository.save(user);
    }

    // Update an existing user by ID
    @PutMapping("/{id}")
    public Mono<ResponseEntity<User>> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        logger.info(String.format("Updating user with ID: %d", id));
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setName(updatedUser.getName());
                    existingUser.setAge(updatedUser.getAge());
                    existingUser.setGender(updatedUser.getGender());
                    return userRepository.save(existingUser);
                })
                .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete a user by ID
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        logger.info(String.format("Deleting user with ID: %d", id));
        return userRepository.findById(id)
                .flatMap(existingUser -> 
                        userRepository.delete(existingUser)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

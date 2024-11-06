package app.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.project.model.User;
import app.project.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Retrieve all users
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Retrieve a specific user by ID
    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Create a new user
    public Mono<User> createUser(User user) {
        return userRepository.save(user);
    }

    // Update an existing user
    public Mono<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setName(updatedUser.getName());
                    existingUser.setAge(updatedUser.getAge());
                    existingUser.setGender(updatedUser.getGender());
                    return userRepository.save(existingUser);
                });
    }

    // Delete a user by ID
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .flatMap(userRepository::delete);
    }
}

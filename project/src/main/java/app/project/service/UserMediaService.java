package app.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.project.model.UserMedia;
import app.project.repository.UserMediaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserMediaService {

    @Autowired
    private UserMediaRepository userMediaRepository;

    // Retrieve all user-media relationships
    public Flux<UserMedia> getAllUserMedia() {
        return userMediaRepository.findAll();
    }

    // Retrieve a specific user-media relationship by ID
    public Mono<UserMedia> getUserMediaById(Long id) {
        return userMediaRepository.findById(id);
    }

    // Create a new user-media relationship
    public Mono<UserMedia> createUserMedia(UserMedia userMedia) {
        return userMediaRepository.save(userMedia);
    }

    // Delete a user-media relationship by ID
    public Mono<Void> deleteUserMedia(Long id) {
        return userMediaRepository.findById(id)
                .flatMap(existingUserMedia -> userMediaRepository.delete(existingUserMedia));
    }
}

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

    public Flux<UserMedia> getAllUserMedia() {
        return userMediaRepository.findAll();
    }

    public Mono<UserMedia> getUserMediaById(Long id) {
        return userMediaRepository.findById(id);
    }

    public Mono<UserMedia> addUserMediaRelationship(Long userId, Long mediaId) {
        UserMedia userMedia = new UserMedia();
        userMedia.setUserId(userId);
        userMedia.setMediaId(mediaId);
        return userMediaRepository.save(userMedia);
    }

    public Mono<Void> deleteUserMedia(Long id) {
        return userMediaRepository.findById(id)
                .flatMap(userMediaRepository::delete);
    }
}

package app.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.project.model.Media;
import app.project.repository.MediaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    // Retrieve all media items
    public Flux<Media> getAllMedia() {
        return mediaRepository.findAll();
    }

    // Retrieve a specific media item by ID
    public Mono<Media> getMediaById(Long id) {
        return mediaRepository.findById(id);
    }

    // Create a new media item
    public Mono<Media> createMedia(Media media) {
        return mediaRepository.save(media);
    }

    // Update an existing media item by ID
    public Mono<Media> updateMedia(Long id, Media updatedMedia) {
        return mediaRepository.findById(id)
                .flatMap(existingMedia -> {
                    existingMedia.setTitle(updatedMedia.getTitle());
                    existingMedia.setReleaseDate(updatedMedia.getReleaseDate());
                    existingMedia.setAverageRating(updatedMedia.getAverageRating());
                    existingMedia.setType(updatedMedia.getType());
                    return mediaRepository.save(existingMedia);
                });
    }

    // Delete a media item by ID
    public Mono<Void> deleteMedia(Long id) {
        return mediaRepository.deleteById(id);
    }
}

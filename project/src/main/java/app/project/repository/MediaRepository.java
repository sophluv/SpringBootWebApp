package app.project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import app.project.model.Media;

@Repository
public interface MediaRepository extends ReactiveCrudRepository<Media, Long> {
}

package app.project.repository;

import app.project.model.Media;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends ReactiveCrudRepository<Media, Long> {
}

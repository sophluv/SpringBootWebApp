package app.project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import app.project.model.UserMedia;

@Repository
public interface UserMediaRepository extends ReactiveCrudRepository<UserMedia, Long> {
}

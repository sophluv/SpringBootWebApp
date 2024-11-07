package app.project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import app.project.model.UserMedia;

public interface UserMediaRepository extends ReactiveCrudRepository<UserMedia, Long> {
}

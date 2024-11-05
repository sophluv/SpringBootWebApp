package app.project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import app.project.model.User;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
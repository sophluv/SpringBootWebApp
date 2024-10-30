package app.project.model;

import java.time.LocalDate;
import javax.persistence.*;

@Entity
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private LocalDate releaseDate;
    private double averageRating;
    private String type; // Could be Enum (e.g., MOVIE, TV_SHOW)

    // Getters and Setters
}
package app.project.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("media") // Table name in the database
public class Media {

    @Id // Marks the primary key
    private Long id;

    @Column("title") // Maps to the 'title' column
    private String title;

    @Column("release_date") // Maps to the 'release_date' column
    private LocalDate releaseDate;

    @Column("average_rating") // Maps to the 'average_rating' column
    private double averageRating;

    @Column("type") // Maps to the 'type' column
    private String type; // e.g., "Movie" or "TV Show"

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package app.client.model;

import java.time.LocalDate;

public class Media {
    private Long id;           // Identifier
    private String title;      // Title
    private LocalDate releaseDate; // Release date
    private double averageRating;  // Average rating (between 0 and 10)
    private String type;       // Type (e.g., Movie or TV Show)

    // Getters and Setters
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

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", releaseDate=" + releaseDate +
                ", averageRating=" + averageRating +
                ", type='" + type + '\'' +
                '}';
    }
}
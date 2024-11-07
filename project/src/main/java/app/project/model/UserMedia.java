package app.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_media") // Name matches the join table in the database
public class UserMedia {

    @Id // Primary key for the join table, auto-generated
    private Long id;

    @Column("user_id") // Foreign key to the User table
    private Long userId;

    @Column("media_id") // Foreign key to the Media table
    private Long mediaId;

    // Constructors
    public UserMedia() {}

    public UserMedia(Long userId, Long mediaId) {
        this.userId = userId;
        this.mediaId = mediaId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }
}

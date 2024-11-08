package app.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_media")
public class UserMedia {

    @Id 
    private Long id;

    @Column("user_id") 
    private Long userId;

    @Column("media_id") 
    private Long mediaId;

    public UserMedia() {}

    public UserMedia(Long userId, Long mediaId) {
        this.userId = userId;
        this.mediaId = mediaId;
    }

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

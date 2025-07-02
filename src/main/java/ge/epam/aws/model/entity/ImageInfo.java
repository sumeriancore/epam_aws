package ge.epam.aws.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Table(name = "IMAGES")
public class ImageInfo {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "FILENAME")
    private String fileName;

    @Column(name = "SIZE")
    private Long size;

    @Column(name = "EXTENSION")
    private String extension;

    @Column(name = "LAST_UPDATE")
    private ZonedDateTime lastUpdate;
}

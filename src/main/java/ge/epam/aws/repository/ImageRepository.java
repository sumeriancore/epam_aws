package ge.epam.aws.repository;

import ge.epam.aws.model.entity.ImageInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ImageRepository extends CrudRepository<ImageInfo, Long> {
    void deleteByFileName(String fileName);
    ImageInfo getImageInfoByFileName(String fileName);
    @Query("SELECT e.id FROM ImageInfo e")
    List<Long> findAllImageIds();
    ImageInfo getImageInfoById(Long id);
}

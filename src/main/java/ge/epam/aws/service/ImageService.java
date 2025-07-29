package ge.epam.aws.service;

import ge.epam.aws.model.entity.ImageInfo;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String saveImage(MultipartFile file);
    void deleteImage(String fileName);
    ImageInfo getImageMetadata(String fileName);
    ImageInfo getRandomImageMetadata();
    ImageInfo getImage(String fileName);
    String validateStoragesConsistency();
}

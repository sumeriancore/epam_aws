package ge.epam.aws.service.impl;

import ge.epam.aws.model.entity.ImageInfo;
import ge.epam.aws.repository.ImageRepository;
import ge.epam.aws.service.ImageService;
import ge.epam.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultImageService implements ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    @Value("${aws.s3.folder}")
    private String folder;

    @Transactional
    public String saveImage(MultipartFile file) {
        if (file == null) {
            log.error("File is null");
            return "File is null";
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.info("OriginalFilename is null or empty");
            return "failed to save image " + originalFilename;
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectKey = (folder != null && !folder.isEmpty() ? folder : "") + UUID.randomUUID() + fileExtension;

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setFileName(objectKey);
        imageInfo.setExtension(fileExtension);
        imageInfo.setSize(file.getSize());
        imageInfo.setLastUpdate(ZonedDateTime.now());
        imageRepository.save(imageInfo);

        s3Service.uploadFile(file, objectKey);

        return objectKey;
    }

    @Transactional
    public void deleteImage(String fileName) {
        String objectKey = (folder != null && !folder.isEmpty() ? folder : "") + fileName;
        imageRepository.deleteByFileName(objectKey);
        s3Service.deleteFile(objectKey);
    }

    @Override
    public ImageInfo getImageMetadata(String fileName) {
        return imageRepository.getImageInfoByFileName(fileName);
    }

    @Override
    public ImageInfo getRandomImageMetadata() {
        List<Long> imageInfoIds = imageRepository.findAllImageIds();
        if (!imageInfoIds.isEmpty()) {
            Collections.shuffle(imageInfoIds);
            return imageRepository.getImageInfoById(imageInfoIds.get(0));
        }
        return null;
    }

    @Override
    public ImageInfo getImage(String fileName) {
        ImageInfo imageInfo = imageRepository.getImageInfoByFileName(fileName);
        if (imageInfo != null) {
            s3Service.downloadFile(imageInfo.getFileName());
        }
        return imageInfo;
    }
}

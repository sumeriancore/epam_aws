package ge.epam.aws.service.impl;

import ge.epam.aws.model.entity.ImageInfo;
import ge.epam.aws.repository.ImageRepository;
import ge.epam.aws.service.ImageService;
import ge.epam.aws.service.MessagingService;
import ge.epam.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
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
    private final MessagingService awsMessagingService;
    private final RestTemplate restTemplate;
    //move to properties, install in CF
    private static final String API_GATEWAY_URL = "https://nebbotd0ye.execute-api.us-east-1.amazonaws.com/validate-images";

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
        awsMessagingService.sendMessageToSqs(
                "Saved image: { fileName: " + objectKey + ", extension: " + fileExtension + ", size: " + file.getSize() + "}"
        );
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

    @Override
    public String validateStoragesConsistency() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Caller-Source", "SpringBootApp");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    API_GATEWAY_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling Lambda: {}", e.getMessage());
            return "Error calling Lambda: " + e.getMessage();
        }
    }
}

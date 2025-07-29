package ge.epam.aws.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    void uploadFile(MultipartFile file, String objectKey);
    void deleteFile(String objectKey);
    boolean downloadFile(String objectKey);
}

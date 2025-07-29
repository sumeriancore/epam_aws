package ge.epam.aws.service.impl;

import ge.epam.aws.service.S3Service;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class DefaultS3Service implements S3Service {

    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${local.filePath}")
    private String localFilePath;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
        log.info("S3Service initialized with bucket: {} and region: {}", bucketName, region);
    }

    public void uploadFile(MultipartFile file, String objectKey) {
        try {
            if (file.isEmpty()) {
                log.info("Can't upload empty file: {}", file.getOriginalFilename());
            }
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
            log.info("File {} successfully uploaded to S3. URL: {}", objectKey, fileUrl);
        } catch (S3Exception e) {
            log.error("S3 error when uploading file: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException(e.getMessage(), e.getCause());
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public void deleteFile(String objectKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        log.info("Delete file: {}", objectKey);
    }

    @Override
    public boolean downloadFile(String objectKey) {
        Path downloadBasePath = Paths.get(localFilePath);
        Path targetPath = downloadBasePath.resolve(objectKey);
        Path parentDirectory = targetPath.getParent();
        try {
            if (parentDirectory != null && !Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);
                log.info("Directory created: {}", parentDirectory.toAbsolutePath());
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
                 OutputStream outputStream = new FileOutputStream(targetPath.toFile())) {

                byte[] buffer = new byte[524_288];
                int bytesRead;
                while ((bytesRead = s3Object.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                log.info("File {} successfully downloaded to {}", objectKey, localFilePath);

            } catch (IOException e) {
                log.error("Error writing file: {}", e.getMessage());
                log.error(e.getMessage(), e);
            }

        } catch (IOException e) {
            log.error("Directory creating error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Download error S3: {}", e.getMessage());
        }
        return true;
    }
}

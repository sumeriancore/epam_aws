package ge.epam.aws.controller;

import ge.epam.aws.model.dto.ImageInfoDto;
import ge.epam.aws.model.entity.ImageInfo;
import ge.epam.aws.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String savedFileName = imageService.saveImage(file);
        return new ResponseEntity<>(savedFileName, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteImage(@RequestParam("fileName") String fileName) {
        imageService.deleteImage(fileName);
        return new ResponseEntity<>(fileName, HttpStatus.OK);
    }

    @GetMapping("/metadata")
    public ResponseEntity<ImageInfoDto> getImageInfo(@RequestParam("fileName") String fileName) {
        ImageInfo imageInfo = imageService.getImageMetadata(fileName);
        return new ResponseEntity<>(
                new ImageInfoDto(
                        imageInfo.getId(),
                        imageInfo.getFileName(),
                        imageInfo.getSize(),
                        imageInfo.getExtension(),
                        imageInfo.getLastUpdate()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/random/metadata")
    public ResponseEntity<ImageInfoDto> getRandomImageInfo() {
        ImageInfo imageInfo = imageService.getRandomImageMetadata();

        if (imageInfo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(
                    new ImageInfoDto(
                            imageInfo.getId(),
                            imageInfo.getFileName(),
                            imageInfo.getSize(),
                            imageInfo.getExtension(),
                            imageInfo.getLastUpdate()
                    ),
                    HttpStatus.OK
            );
        }
    }

    @GetMapping
    public ResponseEntity<String> downloadImage(@RequestParam("fileName") String fileName) {
        ImageInfo imageInfo = imageService.getImage(fileName);
        return new ResponseEntity<>(imageInfo.getFileName(), HttpStatus.OK);
    }
}

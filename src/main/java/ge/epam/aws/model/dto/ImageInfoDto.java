package ge.epam.aws.model.dto;

import java.time.ZonedDateTime;

public record ImageInfoDto(long id, String fileName, Long size, String extension, ZonedDateTime lastUpdate){}

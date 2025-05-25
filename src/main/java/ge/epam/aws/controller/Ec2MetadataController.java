package ge.epam.aws.controller;

import com.amazonaws.util.EC2MetadataUtils;
import ge.epam.aws.model.Ec2Metadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Ec2MetadataController {

    @GetMapping("/metadata")
    public Ec2Metadata getMetadata() {
        String region = EC2MetadataUtils.getEC2InstanceRegion();
        String availabilityZone = EC2MetadataUtils.getAvailabilityZone();
        return new Ec2Metadata(
                region != null ? region : "Unknown",
                availabilityZone != null ? availabilityZone : "Unknown"
        );
    }
}
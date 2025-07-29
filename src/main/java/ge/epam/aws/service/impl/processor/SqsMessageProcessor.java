package ge.epam.aws.service.impl.processor;

import ge.epam.aws.service.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

@Slf4j
@Component
public class SqsMessageProcessor {

    private final MessagingService awsMessagingService;

    public SqsMessageProcessor(MessagingService awsMessagingService) {
        this.awsMessagingService = awsMessagingService;
    }

//    @Scheduled(fixedRate = 60000)
    public void processSqsMessagesAndPublishToSns() {
        log.info("Scheduled task: Checking SQS for messages...");
        List<Message> messages = awsMessagingService.receiveMessagesFromSqs(10);

        if (messages.isEmpty()) {
            log.info("No messages in SQS queue.");
            return;
        }

        log.info("Found {} messages in SQS.", messages.size());
        messages.forEach(
                message -> {
                    String photoUploadInfo = message.body();
                    String subject = "New Photo Upload Notification";
                    String snsMessage = "A new photo has been uploaded: " + photoUploadInfo;
                    awsMessagingService.publishToSns(subject, snsMessage);
                }
        );

        awsMessagingService.deleteSqsMessages(messages);
        log.info("Finished processing SQS messages and publishing to SNS.");
    }
}

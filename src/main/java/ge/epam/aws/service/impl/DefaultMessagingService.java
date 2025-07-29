package ge.epam.aws.service.impl;

import ge.epam.aws.service.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultMessagingService implements MessagingService {

    private final SnsClient snsClient;
    private final SqsClient sqsClient;

    @Value("${aws.sns.topic.arn}")
    private String snsTopicArn;

    @Value("${aws.sqs.queue.url}")
    private String sqsQueueUrl;

    public DefaultMessagingService(SnsClient snsClient, SqsClient sqsClient) {
        this.snsClient = snsClient;
        this.sqsClient = sqsClient;
    }

    @Override
    public String subscribeToNewsletter(String email) {
        try {
            SubscribeRequest request = SubscribeRequest.builder()
                    .protocol("email")
                    .endpoint(email)
                    .topicArn(snsTopicArn)
                    .build();
            return snsClient.subscribe(request).subscriptionArn();
        } catch (Exception e) {
            log.error("Error subscribing to SNS for email {} : {}", email,  e.getMessage());
            return null;
        }
    }

    @Override
    public void unsubscribeFromNewsletter(String subscriptionArn) {
        try {
            UnsubscribeRequest request = UnsubscribeRequest.builder()
                    .subscriptionArn(subscriptionArn)
                    .build();
            snsClient.unsubscribe(request);
            log.info("Successfully unsubscribed from SNS with ARN: {}", subscriptionArn);
        } catch (Exception e) {
            log.error("Error unsubscribing from SNS with ARN {} : {}", subscriptionArn, e.getMessage());
        }
    }

    @Override
    public void sendMessageToSqs(String messageBody) {
        try {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .messageBody(messageBody)
                    .build();
            sqsClient.sendMessage(sendMsgRequest);
            log.info("Message sent to SQS: '{}'", messageBody);
        }
        catch (Exception e) {
            log.error("Error sending message to SQS: {}", e.getMessage());
        }
    }

    @Override
    public List<Message> receiveMessagesFromSqs(int maxMessages) {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(20)
                    .build();
            return sqsClient.receiveMessage(receiveRequest).messages();
        } catch (Exception e) {
            log.error("Error receiving messages from SQS: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteSqsMessages(List<Message> messages) {
        if (messages.isEmpty()) {
            return;
        }
        List<DeleteMessageBatchRequestEntry> entries = messages.stream()
                .map(msg -> DeleteMessageBatchRequestEntry.builder()
                        .id(msg.messageId()) // Уникальный идентификатор сообщения
                        .receiptHandle(msg.receiptHandle()) // Специальный хэндл для удаления
                        .build())
                .collect(Collectors.toList());

        try {
            DeleteMessageBatchRequest deleteBatchRequest = DeleteMessageBatchRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .entries(entries)
                    .build();
            sqsClient.deleteMessageBatch(deleteBatchRequest);
            log.info("Successfully deleted {} messages from SQS.", messages.size());
        } catch (Exception e) {
            log.error("Error deleting messages from SQS: {}", e.getMessage());
        }
    }

    @Override
    public void publishToSns(String subject, String message) {
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .subject(subject)
                    .message(message)
                    .build();
            snsClient.publish(publishRequest);
            log.info("Message published to SNS: Subject='{}', Message='{}'",subject, message);
        } catch (Exception e) {
            log.error("Error publishing message to SNS: {}", e.getMessage());
        }
    }
}

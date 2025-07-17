package ge.epam.aws.service;

import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public interface MessagingService {

    String subscribeToNewsletter(String email);
    void unsubscribeFromNewsletter(String subscriptionArn);
    void sendMessageToSqs(String messageBody);
    List<Message> receiveMessagesFromSqs(int maxMessages);
    void deleteSqsMessages(List<Message> messages);
    void publishToSns(String subject, String message);
}

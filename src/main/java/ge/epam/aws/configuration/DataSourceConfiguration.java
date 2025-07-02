package ge.epam.aws.configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Value("${aws.secretsmanager.secret-id}")
    private String secretId;

    @Bean
    public DataSource dataSource() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretId);
        GetSecretValueResult getSecretValueResult;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve database secret from AWS Secrets Manager with ID: " + secretId + ". Please check IAM permissions and secret existence.", e);
        }

        if (getSecretValueResult == null || getSecretValueResult.getSecretString() == null) {
            throw new RuntimeException("Secret string is null or empty for secret ID: " + secretId + ". This might indicate an issue with the secret in Secrets Manager.");
        }

        String secretJson = getSecretValueResult.getSecretString();
        ObjectMapper mapper = new ObjectMapper();
        SecretData secretData;
        try {
            secretData = mapper.readValue(secretJson, SecretData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse database secret JSON from Secrets Manager: " + e.getMessage(), e);
        }

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                secretData.getHost(),
                secretData.getPort(),
                secretData.getDbname());

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .username(secretData.getUsername())
                .password(secretData.getPassword())
                .build();
    }

    @Getter
    @Setter
    static class SecretData {
        private String username;
        private String password;
        private String host;
        private String port;
        private String dbname;
    }
}

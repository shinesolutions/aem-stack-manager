package com.shinesolutions.aemstackmanager.config;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.util.EC2MetadataUtils;

@Configuration
@Profile("default")
public class AwsConfig {

    @Value("${aws.sqs.queueName}")
    private String queueName;

    @Value("${aws.client.useProxy}")
    private Boolean useProxy;

    @Value("${aws.client.protocol}")
    private String clientProtocol;

    @Value("${aws.client.proxy.host:@null}")
    private String clientProxyHost;

    @Value("${aws.client.proxy.port:@null}")
    private Integer clientProxyPort;

    @Value("${aws.client.connection.timeout}")
    private Integer clientConnectionTimeout;

    @Value("${aws.client.max.errorRetry}")
    private Integer clientMaxErrorRetry;


    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        /*
         * For info on how this works, see:
         * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/
         * credentials.html
         */
        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    public SQSConnection sqsConnection(AWSCredentialsProvider awsCredentialsProvider,
                                       ClientConfiguration awsClientConfig) throws JMSException {

        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder()
            .withRegion(RegionUtils.getRegion(EC2MetadataUtils.getEC2InstanceRegion())) //Gets region form meta data
            .withAWSCredentialsProvider(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();

        return connectionFactory.createConnection();
    }

    @Bean
    public MessageConsumer sqsMessageConsumer(SQSConnection connection) throws JMSException {

        /*
         * Create the session and use CLIENT_ACKNOWLEDGE mode. Acknowledging
         * messages deletes them from the queue
         */
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        return session.createConsumer(session.createQueue(queueName));
    }

    @Bean
    public ClientConfiguration awsClientConfig() {
        ClientConfiguration clientConfig = new ClientConfiguration();

        if (useProxy) {
            clientConfig.setProxyHost(clientProxyHost);
            clientConfig.setProxyPort(clientProxyPort);
        }

        clientConfig.setProtocol(Protocol.valueOf(clientProtocol.toUpperCase()));
        clientConfig.setConnectionTimeout(clientConnectionTimeout);
        clientConfig.setMaxErrorRetry(clientMaxErrorRetry);

        return clientConfig;
    }

}

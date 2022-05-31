package co.com.dgallego58.aws;


import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient;

import java.net.URI;

public final class DynamoDbClientFactory {
    public static final DynamoDbClient DYNAMO_DB_CLIENT;
    public static final DynamoDbStreamsClient STREAMS_CLIENT;
    public static final DynamoDbEnhancedClient DYNAMO_ENHANCED_CLIENT;

    static {
        var localDynamo = URI.create("http://localhost:8000");
        DYNAMO_DB_CLIENT = DynamoDbClient.builder()
                                         .region(Region.US_EAST_1)
                                         .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                                         .endpointOverride(localDynamo)
                                         .build();
        STREAMS_CLIENT = DynamoDbStreamsClient.builder()
                                              .region(Region.US_EAST_1)
                                              .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                                              .endpointOverride(localDynamo)
                                              .build();
        DYNAMO_ENHANCED_CLIENT = DynamoDbEnhancedClient.builder()
                                                       .dynamoDbClient(DYNAMO_DB_CLIENT)
                                                       .build();
    }

    private DynamoDbClientFactory() {
        //final
    }


}

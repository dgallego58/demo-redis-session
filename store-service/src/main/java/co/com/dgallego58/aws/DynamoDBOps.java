package co.com.dgallego58.aws;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.StreamSpecification;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.List;

public final class DynamoDBOps {

    public static final String TEST_TABLE_NAME = "TestTableForStreams";

    private DynamoDBOps() {
        //final
    }

    public static void createTable() {
        var client = DynamoDbClientFactory.DYNAMO_DB_CLIENT;

        var attributeDefinitions = List.of(AttributeDefinition.builder()
                                                              .attributeName("Id")
                                                              .attributeType(ScalarAttributeType.N)
                                                              .build());
        var keySchema = List.of(KeySchemaElement.builder().attributeName("Id").keyType(KeyType.HASH).build());

        var streamSpecification = StreamSpecification.builder()
                                                     .streamEnabled(true)
                                                     .streamViewType(StreamViewType.NEW_AND_OLD_IMAGES)
                                                     .build();

        var createTableRequest = CreateTableRequest.builder()
                                                   .tableName(TEST_TABLE_NAME)
                                                   .attributeDefinitions(attributeDefinitions)
                                                   .keySchema(keySchema)
                                                   .provisionedThroughput(builder -> builder.readCapacityUnits(10L)
                                                                                            .writeCapacityUnits(10L))
                                                   .streamSpecification(streamSpecification)
                                                   .build();

        System.out.printf("Cleaning... %n");

        client.waiter().waitUntilTableNotExists(builder -> builder.tableName(TEST_TABLE_NAME));
        System.out.printf("Cleaned... %n");
        //client.deleteTable(builder -> builder.tableName(TEST_TABLE_NAME));
        System.out.printf("Creating table %s%n", TEST_TABLE_NAME);
        client.createTable(createTableRequest);
        System.out.printf("wait for creation of %s%n", TEST_TABLE_NAME);
        DynamoDbWaiter waiter = client.waiter();
        waiter.waitUntilTableExists(builder -> builder.tableName(TEST_TABLE_NAME));
    }

    public static <T> DynamoDbTable<T> table(final Class<T> tableBean) {
        var enhancedClient = DynamoDbClientFactory.DYNAMO_ENHANCED_CLIENT;
        return enhancedClient.table(TEST_TABLE_NAME, TableSchema.fromBean(tableBean));
    }
}

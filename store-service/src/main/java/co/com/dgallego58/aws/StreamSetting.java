package co.com.dgallego58.aws;

import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;

import java.util.Map;
import java.util.stream.IntStream;

public final class StreamSetting {
    private StreamSetting() {
        //final
    }

    public static void describeTable() {

        var client = DynamoDbClientFactory.DYNAMO_DB_CLIENT;
        var describeTableResponse = client.describeTable(
                builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME));
        System.out.println("Current stream ARN for " + DynamoDBOps.TEST_TABLE_NAME + ": " +
                describeTableResponse.table().latestStreamArn());
        var streamSpec = describeTableResponse.table().streamSpecification();
        System.out.println("Stream enabled: " + streamSpec.streamEnabled());
        System.out.println("Update view type: " + streamSpec.streamViewType());
        System.out.println();
    }

    public static void writeOnTable() {
        System.out.printf("Writing on table: [%s]%n", DynamoDBOps.TEST_TABLE_NAME);
        var client = DynamoDbClientFactory.DYNAMO_DB_CLIENT;
        IntStream.range(1, 101)
                 .forEach(iteration -> {

                     //Write a new item
                     System.out.printf("Processing Item %s%n", iteration);
                     var item = Map.of("Id", AttributeValue.fromN(String.valueOf(iteration)),
                             "Message", AttributeValue.fromS("New Item! - " + iteration)
                     );
                     client.putItem(
                             builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME).item(item));
                     //update the written item
                     var key = Map.of("Id", AttributeValue.fromN(String.valueOf(iteration)));
                     var attributeValueUpdates = Map.of("Message", AttributeValueUpdate.builder()
                                                                                       .action(AttributeAction.PUT)
                                                                                       .value(builder -> builder.s(
                                                                                               "Changed item..."))
                                                                                       .build());
                     client.updateItem(builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME)
                                                         .key(key)
                                                         .attributeUpdates(attributeValueUpdates));

                     //delete item
                     client.deleteItem(builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME).key(key));

                 });
    }

}

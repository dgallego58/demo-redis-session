package co.com.dgallego58.aws;

import software.amazon.awssdk.services.dynamodb.model.DescribeStreamRequest;
import software.amazon.awssdk.services.dynamodb.model.GetRecordsRequest;
import software.amazon.awssdk.services.dynamodb.model.Record;
import software.amazon.awssdk.services.dynamodb.model.Shard;
import software.amazon.awssdk.services.dynamodb.model.ShardIteratorType;

import java.util.List;
import java.util.Objects;


public final class StreamShardsOps {

    private StreamShardsOps() {
        //final
    }

    public static void runShards() {
        String lastEvaluatedShardId = null;
        var client = DynamoDbClientFactory.DYNAMO_DB_CLIENT;
        var streamsClient = DynamoDbClientFactory.STREAMS_CLIENT;
        var describeTableResult = client.describeTable(builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME));

        String streamArn = describeTableResult.table().latestStreamArn();
        do {

            var describeStreamRequest = DescribeStreamRequest.builder()
                                                             .streamArn(streamArn)
                                                             .exclusiveStartShardId(lastEvaluatedShardId).build();
            var describeStreamResponse = streamsClient.describeStream(describeStreamRequest);
            var shards = describeStreamResponse.streamDescription().shards();
            for (Shard shard : shards) {

                String shardId = shard.shardId();
                System.out.printf("Shard: %s%n", shard);
                // get an iterator for the current shard
                var shardIteratorResponse = streamsClient.getShardIterator(
                        builder -> builder.shardId(shardId)
                                          .streamArn(streamArn)
                                          .shardIteratorType(ShardIteratorType.TRIM_HORIZON));
                String shardIterator = shardIteratorResponse.shardIterator();
                // Shard iterator is not null until the Shard is sealed (marked as READ_ONLY).
                // To prevent running the loop until the Shard is sealed, which will be on average
                // 4 hours, we process only the items that were written into DynamoDB and then exit.
                int processedRecordCount = 0;
                //maxItemCount = 100
                while (shardIterator != null && processedRecordCount < 100) {
                    System.out.printf("---- Shard Iterator %s%n", shardIterator.substring(2));
                    //Use the shard iterator to read the stream records
                    //String finalShardIterator = shardIterator;//ver
                    var recordsRequest = GetRecordsRequest.builder().shardIterator(shardIterator).build();
                    var recordsResponse = streamsClient.getRecords(recordsRequest);
                    List<Record> records = recordsResponse.records();
                    for (Record singleRecord : records) {
                        System.out.printf("-------- %s%n", singleRecord.dynamodb());
                    }

                    processedRecordCount = processedRecordCount + records.size();
                    shardIterator = recordsResponse.nextShardIterator();
                }
            }
            // If LastEvaluatedShardId is set, then there is
            // at least one more page of shard IDs to retrieve
            lastEvaluatedShardId = describeStreamResponse.streamDescription().lastEvaluatedShardId();
            System.out.printf("Last evaluated Shard Id: %s%n", lastEvaluatedShardId);
        } while (Objects.nonNull(lastEvaluatedShardId));

        // delete table
        System.out.printf("Deleting table %s%n", DynamoDBOps.TEST_TABLE_NAME);
        client.describeTable(builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME));
        client.deleteTable(builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME));
        System.out.printf("Demo completed... %n");

    }
}

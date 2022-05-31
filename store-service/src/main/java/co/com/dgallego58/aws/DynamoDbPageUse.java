package co.com.dgallego58.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DynamoDbPageUse {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbPageUse.class);

    public static void executePage() {
        var table = DynamoDBOps.table(TestTable.class);
        var s = table.query(builder -> builder.build());

        log.info("Creating table");
        table.createTable();
        DynamoDbWaiter waiter = DynamoDbClientFactory.DYNAMO_DB_CLIENT.waiter();
        waiter.waitUntilTableExists(builder -> builder.tableName(DynamoDBOps.TEST_TABLE_NAME));
        log.info("Insert");
        IntStream.range(0, 10_000)
                 .mapToObj(value -> {
                     var tableData = new TestTable();
                     tableData.setPk(String.valueOf(value));
                     tableData.setSk("-" + value);
                     tableData.setName("test " + value);
                     return tableData;
                 })
                 .forEach(table::putItem);


        log.info("scanning...");
        List<TestTable> items = new ArrayList<>();
        Map<String, AttributeValue> lastKey = null;

        do {

            var scan = table.scan(ScanEnhancedRequest.builder()
                                                     .limit(100)
                                                     .exclusiveStartKey(lastKey)
                                                     .build());
            var pages = scan.stream().collect(Collectors.toList());

            for (Page<TestTable> page : pages) {
                lastKey = page.lastEvaluatedKey();
                items.addAll(page.items());
                log.info("Last key is {}", lastKey);
            }

        } while (lastKey != null);
        log.info("scan result {}", items.size());
        log.info("cleaning");
        log.info("Deleting table: {}", table.describeTable());
        table.deleteTable();
    }

}

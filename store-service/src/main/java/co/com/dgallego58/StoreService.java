package co.com.dgallego58;

import co.com.dgallego58.aws.DynamoDBOps;
import co.com.dgallego58.aws.DynamoDbPageUse;
import co.com.dgallego58.aws.StreamSetting;
import co.com.dgallego58.aws.StreamShardsOps;

public class StoreService {


    public static void main(String[] args) {
        DynamoDBOps.createTable();
        StreamSetting.describeTable();
        StreamSetting.writeOnTable();
        StreamShardsOps.runShards();

        DynamoDbPageUse.executePage();
    }
}

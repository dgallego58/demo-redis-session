package co.com.dgallego58.aws;

import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Setter
public class TestTable {

    private String pk;
    private String sk;
    private String name;

    @DynamoDbPartitionKey
    @DynamoDbAttribute(value = "PK")
    public String getPk() {
        return "TEST_TABLE#" + pk;
    }



    @DynamoDbSortKey
    @DynamoDbAttribute(value = "SK")
    public String getSk() {
        return "TEST_TABLE#" + sk;
    }



    @DynamoDbAttribute(value = "NAME")
    public String getName() {
        return name;
    }


/*    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public void setName(String name) {
        this.name = name;
    }*/
}

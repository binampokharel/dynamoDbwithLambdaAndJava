package org.dynamoDB.entities;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

@Getter
@Setter
public class ZipcodeRequest {
    private String id;
    @Getter(onMethod = @__({@DynamoDbAttribute("ip_address")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("ip_address")}))
    private String ipAddress;

    @Getter(onMethod = @__({@DynamoDbAttribute("request_form")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("request_form")}))
    private String requestFrom;

    @Getter(onMethod = @__({@DynamoDbAttribute("created_date")}))
    @Setter(onMethod = @__({@DynamoDbAttribute("created_date")}))
    private String createdDate;


}

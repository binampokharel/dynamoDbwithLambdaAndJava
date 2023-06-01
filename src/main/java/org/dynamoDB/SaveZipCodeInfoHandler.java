package org.dynamoDB;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dynamoDB.DTO.ResponseDTO;
import org.dynamoDB.entities.ZipcodeRequest;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;
import java.util.Map;

public class SaveZipCodeInfoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @SneakyThrows
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        // Step 1: Parse the request body and extract the ZipcodeRequest
        ZipcodeRequest request = parseRequest(input.getBody());

        // Step 2: Create DynamoDbClient instance
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        // Step 3: Insert item into DynamoDB table
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("id", AttributeValue.builder().n(String.valueOf(request.getId())).build());
        itemValues.put("ipAddress", AttributeValue.builder().s(request.getIpAddress()).build());
        itemValues.put("requestFrom", AttributeValue.builder().s(request.getRequestFrom()).build());
        itemValues.put("createdDate", AttributeValue.builder().s(request.getCreatedDate()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("zip_code_details")
                .item(itemValues)
                .build();

        try {
            PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);

            // Step 4: Create the response message based on the result
            String responseMessage;
            String statusCode;
            if (putItemResponse.sdkHttpResponse().isSuccessful()) {
                responseMessage = "Data Inserted Successfully";
                statusCode = "200";
            } else {
                responseMessage = "Error Inserting data";
                statusCode = "500";
            }

            // Step 5: Create the ResponseDTO
            ResponseDTO responseDTO = new ResponseDTO(statusCode, responseMessage);

            // Step 6: Create and return the APIGatewayProxyResponseEvent
            APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
            apiResponse.setStatusCode(200);

            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(responseDTO);

            apiResponse.setBody(responseJson);

            return apiResponse;
        } catch (DynamoDbException | JsonProcessingException e) {
            // Handle exception
            e.printStackTrace();

            // Step 5: Create the ResponseDTO for exception case
            ResponseDTO responseDTO = new ResponseDTO("500", "Error Inserting data");

            // Step 6: Create and return the APIGatewayProxyResponseEvent for exception case
            APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
            apiResponse.setStatusCode(200);

            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(responseDTO);

            apiResponse.setBody(responseJson);

            return apiResponse;
        }
    }

    private ZipcodeRequest parseRequest(String requestBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(requestBody, ZipcodeRequest.class);
        } catch (JsonProcessingException e) {
            // Handle exception
            e.printStackTrace();
            // Return a default ZipcodeRequest or throw an exception based on your requirement
            return new ZipcodeRequest();
        }
    }

}

package example.micronaut;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import example.micronaut.domain.FuncRequest;
import example.micronaut.domain.FuncResponse;
import example.micronaut.domain.MessageMaster;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class FunctionRequestHandler
        extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static String id = "id";
    private static String title = "title";
    private static String text = "text";
    private static String tableName = "ex-table";

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent request) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            // FuncRequest req = objectMapper.readValue(request.getBody(), FuncRequest.class);
            FuncRequest req = new FuncRequest();
            FuncResponse res = runProcess(req);

            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(res));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500);
            JSONObject obj = new JSONObject();
            obj.put("error", "error");
            response.setBody(obj.toString());
        }
        return response;
    }

    /**
     * メイン処理
     * 
     * @param request
     * @return
     * @throws URISyntaxException
     */
    private FuncResponse runProcess(FuncRequest request) throws URISyntaxException {

        String url = "http://localhost:8000";

        DynamoDbClient client = DynamoDbClient.builder().endpointOverride(new URI(url))
                .region(Region.AP_NORTHEAST_1).build();

        // SELECTリクエスト
        Optional<MessageMaster> item = getItem(client);
        System.out.println(item);

        // SAVEリクエスト
        // MessageMaster master = new MessageMaster();
        // master.setMessageId("2");
        // master.setTitle("example title");
        // master.setText("example text");
        // putItemRequest(client, master);

        FuncResponse response = new FuncResponse();
        response.setResult("OK");
        response.setUrl(url);
        return response;
    }

    /**
     * SELECT リクエスト
     * 
     * @param client
     * @return
     */
    private Optional<MessageMaster> getItem(DynamoDbClient client) {
        AttributeValue value = AttributeValue.builder().s("1").build();
        // AttributeValue titleValue = AttributeValue.builder().s("example title").build();
        Map<String, AttributeValue> mp = new HashMap<>();
        mp.put(id, value);
        // mp.put(title, titleValue);

        GetItemRequest getRequest = GetItemRequest.builder().tableName(tableName).key(mp).build();

        GetItemResponse getResponse = client.getItem(getRequest);

        if (!getResponse.hasItem()) {
            return Optional.empty();
        }

        return Optional.of(messageOf(getResponse.item()));
    }

    /**
     * CREATE TABLEリクエスト
     * 
     * @param client
     */
    private void createTable(DynamoDbClient client) {

        CreateTableRequest createRequest = CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder().attributeName(id)
                        .attributeType(ScalarAttributeType.S).build())
                .keySchema(Arrays.asList(
                        KeySchemaElement.builder().attributeName(id).keyType(KeyType.HASH).build()))
                .billingMode(BillingMode.PAY_PER_REQUEST).tableName(tableName).build();

        CreateTableResponse response = client.createTable(createRequest);
    }

    /**
     * INSERTリクエスト
     * 
     * @param client
     */
    private void putItemRequest(DynamoDbClient client, MessageMaster master) {

        PutItemRequest putRequest = PutItemRequest.builder().tableName(tableName).item(item(master)).build();

        PutItemResponse putResponse = client.putItem(putRequest);
    }

    /**
     * アトリビュートMap to モデルクラス
     * 
     * @param item
     * @return
     */
    private MessageMaster messageOf(@NonNull Map<String, AttributeValue> item) {
        return new MessageMaster(item.get(id).s(), item.get(title).s(), item.get(text).s());
    }

    /**
     * モデルクラス to アトリビュートMap
     * 
     * @param mm
     * @return
     */
    private Map<String, AttributeValue> item(@NonNull MessageMaster mm) {
        Map<String, AttributeValue> result = new HashMap<>();
        result.put(id, AttributeValue.builder().s(mm.getMessageId()).build());
        result.put(title, AttributeValue.builder().s(mm.getTitle()).build());
        result.put(text, AttributeValue.builder().s(mm.getText()).build());
        return result;
    }
}

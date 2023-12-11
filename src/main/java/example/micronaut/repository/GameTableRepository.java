package example.micronaut.repository;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import example.micronaut.domain.GameTable;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Singleton
public class GameTableRepository extends DynamoDbRepository {

    public GameTableRepository(@Value("${dynamodb.host}") String host, @Value("${dynamodb.port}") String port)
            throws URISyntaxException {
        super(host, port);
    }

    private final static String TABLE_NAME = "GameTable";
    private final static String GAME_CATEGORY = "gameCategory";
    private final static String GAME_ID = "gameId";
    private final static String GAME_TITLE = "gameTitle";
    private final static String PUBLISH_DATE = "publishDate";

    /**
     * GETリクエスト
     * 
     * @param client
     * @return
     */
    public Optional<GameTable> getItem(GameTable game) {
        Map<String, AttributeValue> mp = new HashMap<>();
        mp.put(GAME_CATEGORY, AttributeValue.builder().s(game.getGameCategory()).build());
        mp.put(GAME_ID, AttributeValue.builder().s(game.getGameId()).build());

        GetItemRequest getRequest = GetItemRequest.builder().tableName(TABLE_NAME).key(mp).build();
        GetItemResponse getResponse = this.client.getItem(getRequest);

        if (!getResponse.hasItem()) {
            return Optional.empty();
        }

        return Optional.of(attributeToModel(getResponse.item()));
    }

    /**
     * PUTリクエスト
     * 
     * @param client
     */
    public void putItem(GameTable game) {

        PutItemRequest putRequest = PutItemRequest.builder().tableName(TABLE_NAME).item(modelToAttribute(game)).build();
        PutItemResponse putResponse = this.client.putItem(putRequest);
        SdkHttpResponse response = putResponse.sdkHttpResponse();
        System.out.println(response.statusCode());
        System.out.println(response.statusText());
    }

    /**
     * DELETEリクエスト
     * 
     * @param client
     */
    public void deleteItem(GameTable game) {

        Map<String, AttributeValue> mp = new HashMap<>();
        mp.put(GAME_CATEGORY, AttributeValue.builder().s(game.getGameCategory()).build());
        mp.put(GAME_ID, AttributeValue.builder().s(game.getGameId()).build());

        DeleteItemRequest deleteRequest = DeleteItemRequest.builder().tableName(TABLE_NAME).key(mp).build();
        DeleteItemResponse deleteResponse = this.client.deleteItem(deleteRequest);
        SdkHttpResponse response = deleteResponse.sdkHttpResponse();
        System.out.println(response.statusCode());
        System.out.println(response.statusText());
    }

    public void queryItem(GameTable game) {

        StringBuilder condition = new StringBuilder();
        condition.append("gameCategory = :gameCategory ");
        condition.append("AND gameId = :gameId ");

        StringBuilder filter = new StringBuilder();
        filter.append("publishDate >= :publishDate");

        Map<String, AttributeValue> expression = new HashMap<>();
        expression.put(":gameCategory", buildValue(game.getGameCategory()));
        expression.put(":gameId", buildValue(game.getGameId()));
        expression.put(":publishDate", buildValue(game.getPublishDate()));

        QueryRequest request = QueryRequest.builder().tableName(TABLE_NAME).keyConditionExpression(condition.toString())
                .filterExpression(filter.toString()).expressionAttributeValues(expression).build();

        QueryResponse response = this.client.query(request);
        System.out.println(response);
    }

    /**
     * CREATE TABLEリクエスト
     * 
     * @param client
     */
    public void createTable() {

        CreateTableRequest createRequest = CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder().attributeName(GAME_CATEGORY)
                        .attributeType(ScalarAttributeType.S).build())
                .keySchema(Arrays.asList(
                        KeySchemaElement.builder().attributeName(GAME_CATEGORY).keyType(KeyType.HASH).build()))
                .billingMode(BillingMode.PAY_PER_REQUEST).tableName(TABLE_NAME).build();

        CreateTableResponse createResponse = this.client.createTable(createRequest);
        SdkHttpResponse response = createResponse.sdkHttpResponse();
        System.out.println(response.statusCode());
        System.out.println(response.statusText());
    }

    /**
     * アトリビュート文字列を生成しする
     * @param value
     * @return
     */
    private AttributeValue buildValue(String value) {
        return AttributeValue.builder().s(value).build();
    }

    /**
     * アトリビュートMap to モデル
     * 
     * @param item
     * @return
     */
    private GameTable attributeToModel(@NonNull Map<String, AttributeValue> item) {
        return new GameTable(item.get(GAME_CATEGORY).s(), item.get(GAME_ID).s(), item.get(GAME_TITLE).s(),
                item.get(PUBLISH_DATE).s());
    }

    /**
     * モデル to アトリビュートMap
     * 
     * @param game
     * @return
     */
    private Map<String, AttributeValue> modelToAttribute(@NonNull GameTable game) {
        Map<String, AttributeValue> result = new HashMap<>();
        result.put(GAME_CATEGORY, AttributeValue.builder().s(game.getGameCategory()).build());
        result.put(GAME_ID, AttributeValue.builder().s(game.getGameId()).build());
        result.put(GAME_TITLE, AttributeValue.builder().s(game.getGameTitle()).build());
        result.put(PUBLISH_DATE, AttributeValue.builder().s(game.getPublishDate()).build());
        return result;
    }
}

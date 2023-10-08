package example.micronaut.repository;

import java.net.URI;
import java.net.URISyntaxException;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public abstract class DynamoDbRepository {
    protected DynamoDbClient client;

    protected DynamoDbRepository(String host, String port) throws URISyntaxException {
        this.client = DynamoDbClient.builder().endpointOverride(new URI(host + ":" + port))
                .region(Region.AP_NORTHEAST_1).build();
    }
}

package example.micronaut;

import java.net.URISyntaxException;
import java.util.Optional;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import example.micronaut.domain.FuncRequest;
import example.micronaut.domain.FuncResponse;
import example.micronaut.domain.GameTable;
import example.micronaut.repository.GameTableRepository;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;

public class FunctionRequestHandler
        extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    GameTableRepository gameTableRepository;
    @Inject
    private ObjectMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent request) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            FuncRequest req = objectMapper.readValue(request.getBody(), FuncRequest.class);
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

        switch (request.getAction()) {
        case "get":
            Optional<GameTable> item = gameTableRepository.getItem(request.getGameTablie());
            System.out.println(item);
            break;
        case "put":
            gameTableRepository.putItem(request.getGameTablie());
            break;
        case "delete":
            gameTableRepository.deleteItem(request.getGameTablie());
            break;
        case "query":
            gameTableRepository.queryItem(request.getGameTablie());
            break;
        default:
            gameTableRepository.createTable();
        }

        FuncResponse response = new FuncResponse();
        response.setResult("OK");
        return response;
    }
}

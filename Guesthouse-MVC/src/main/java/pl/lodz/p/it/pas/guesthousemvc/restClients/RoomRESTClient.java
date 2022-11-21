package pl.lodz.p.it.pas.guesthousemvc.restClients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.lodz.p.it.pas.dto.CreateRoomDTO;
import pl.lodz.p.it.pas.dto.UpdateRoomDTO;
import pl.lodz.p.it.pas.guesthousemvc.utils.Utils;
import pl.lodz.p.it.pas.model.Room;

import javax.enterprise.context.RequestScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@RequestScoped
public class RoomRESTClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<Room> getRoomList() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(Utils.API_URL + "/rooms"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), new TypeReference<List<Room>>() {
        });
    }

    public int removeRoom(int id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.
                newBuilder(URI.create(Utils.API_URL + "/rooms/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    public int addRoom(CreateRoomDTO createRoomDTO) throws IOException, InterruptedException {
        String requestBody = this.mapper.writeValueAsString(createRoomDTO);
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/api/rooms"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    public Room getRoomById(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(Utils.API_URL + "/rooms/" + id)).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), Room.class);
    }

    public int updateRoom(Long id, UpdateRoomDTO dto) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(Utils.API_URL + "/rooms/" + id))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(dto)))
                .header("Content-type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }
}

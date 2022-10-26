package pl.lodz.pas.manager;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import pl.lodz.pas.dto.CreateRentDTO;
import pl.lodz.pas.model.Rent;
import pl.lodz.pas.model.Room;

import java.time.LocalDateTime;

class RoomManagerTest {


    @Test
    void shouldReturnRoomWithStatusCode200() {
        when()
            .get("/api/rooms/{id}", 643)
            .then()
            .assertThat().statusCode(Response.Status.OK.getStatusCode())
            .assertThat().contentType(ContentType.JSON)
            .assertThat().body("roomNumber", equalTo(643))
            .assertThat().body("price", equalTo(250.0F))
            .assertThat().body("size", equalTo(6));
    }

    @Test
    void shouldReturnListOfRoomsWithStatusCode200() {
        when()
            .get("/api/rooms")
            .then()
            .assertThat().statusCode(Response.Status.OK.getStatusCode())
            .assertThat().contentType(ContentType.JSON);
        //TODO add some assertions
    }

    @Test
    void shouldCreateRoomWithStatusCode201() {

        Room room = new Room(1, 600.0, 1);

        JSONObject req = new JSONObject(room);
        given()
            .contentType(ContentType.JSON)
            .body(req.toString())
            .when()
            .post("/api/rooms")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void shouldFailCreatingRoomWithExistingNumberWithStatusCode409() {
        Room room = new Room(643, 200.0, 10);

        JSONObject req = new JSONObject(room);
        given()
            .contentType(ContentType.JSON)
            .body(req.toString())
            .when()
            .post("/api/rooms")
            .then()
            .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    void shouldGetRoomByIdWithStatusCode200() {
        given().when()
               .get("/api/rooms/id/2")
               .then()
               .statusCode(Response.Status.OK.getStatusCode())
               .contentType(ContentType.JSON)
               .body("id", equalTo(2),
                     "price", equalTo(707.19F),
                     "roomNumber", equalTo(836),
                     "size", equalTo(1));
    }

    @Test
    void shouldGetRoomByIdFailWithStatusCode404() {
        given().when()
               .get("/api/rooms/id/123456")
               .then()
               .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void shouldFindPastRentsForRoomWithStatusCode200() {
        given().param("past", true)
               .when().get("/api/rooms/11/rents")
               .then()
               .statusCode(Response.Status.OK.getStatusCode())
               .contentType(ContentType.JSON)
               .body("size()", equalTo(1));
    }

    @Test
    void shouldFindActiveRentsForRoomWithStatusCode200() {
        given().param("past", false)
               .when().get("/api/rooms/11/rents")
               .then()
               .statusCode(Response.Status.OK.getStatusCode())
               .contentType(ContentType.JSON)
               .body("size()", equalTo(2));
    }

    @Test
    void shouldRemoveRoomWithStatusCode204() {
        Room room = new Room(1234, 200.0, 4);
        JSONObject json = new JSONObject(room);
        ResponseBody responseBody = given()
                .body(json.toString())
                .contentType(ContentType.JSON)
                .when().post("/api/rooms/").getBody();

        Room addedRoom = responseBody.as(Room.class);

        given()
                .contentType(ContentType.JSON)
                .when().delete("/api/rooms/" + addedRoom.getId())
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void shouldFailRemoveRoomWhenThereAreActiveRentsForItWithStatusCode409() {
        Room room = new Room(4321, 200.0, 4);
        JSONObject json = new JSONObject(room);

        ResponseBody responseBody = given()
                .body(json.toString())
                .contentType(ContentType.JSON)
                .when().post("/api/rooms")
                .getBody();

        Room addedRoom = responseBody.as(Room.class);

        LocalDateTime beginDate = LocalDateTime.of(2025, 11, 22, 11, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 11, 25, 10, 0, 0);

        CreateRentDTO dto = new CreateRentDTO(beginDate, endDate, true, 2L, addedRoom.getId());
        JSONObject body = new JSONObject(dto);

        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/rents")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());


        given()
                .contentType(ContentType.JSON)
                .when().delete("/api/rooms/" + addedRoom.getId())
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }
}

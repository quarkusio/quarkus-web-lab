package web.lab.comments;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentResourceTest {
    
    @Test @Order(1)
    void testAddCommentEndpoint() {
        given()
            .when()
                .body(TEST_COMMENT)
                .contentType(ContentType.JSON)
                .post("/comment")
            .then()
                .log().all()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].ref", equalTo("unittest"))
                .body("[0].name", equalTo("tester"))   
                .body("[0].comment", equalTo("I an just testing"));
    }

    @Test @Order(2)
    void testGetCommentsEndpoint() {
        given()
            .when()
                .accept(ContentType.JSON)
                .get("/comment/unittest")
            .then()
                .log().all()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].ref", equalTo("unittest"))
                .body("[0].name", equalTo("tester"))   
                .body("[0].comment", equalTo("I an just testing"));
    }
    
    
    private static final String TEST_COMMENT = """
                                               {
                                                 "ref": "unittest",
                                                 "name": "tester",
                                                 "comment": "I an just testing"
                                               }
                                               """;
    
}
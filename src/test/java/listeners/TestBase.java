package listeners;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import utilities.ReadDataFromPropertyFile;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

public class TestBase {

    public static String UserName;
    public static String hash;
    public static String Password;
    public static String apiKey = ReadDataFromPropertyFile.getProperty("apiKey");
    public static String excelFilePath = ReadDataFromPropertyFile.getProperty("excelFilePath");

    @BeforeSuite

    public void setCredentials(){

        Credentials credentials = new Credentials();
        credentials.setUsername("TestUser_ssk2");
        credentials.setFirstName("FirstUser");
        credentials.setLastName("Tester");
        credentials.setEmail("testmail@gmail.com");

        String postObject =GSONtoJSON.convertTOJSON(credentials);    // to convert java object to JSON using GSON library.
        System.out.println("JSON body of post request: \n"+postObject);

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("apiKey",apiKey)
                .and()
                .body(postObject)
                .when()
                .post("https://api.spoonacular.com/users/connect")
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(),200);
        response.then().log().body();
        JsonPath jsonPathEvaluator = response.jsonPath();
        UserName = jsonPathEvaluator.get("username");
        hash = jsonPathEvaluator.get("hash");
        Password = jsonPathEvaluator.get("spoonacularPassword");
        System.out.println("\n username: "+UserName+"\n hash: "+hash+"\n spoonacularPassword: "+Password);

    }

    @BeforeMethod
    public static void setup(){

        baseURI = "https://api.spoonacular.com/mealplanner/"+UserName+"/templates?hash="+hash;

    }
}

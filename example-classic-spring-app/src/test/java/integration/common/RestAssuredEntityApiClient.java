package integration.common;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestAssuredEntityApiClient {

  @NonNull
  private final String baseEntityUrl;

  @NonNull
  private final String idFieldName;

  @NonNull
  private final String versionFieldName;

  public <R> R create(Object createEntityDTO, Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .body(createEntityDTO)
      .when()
      .post(baseEntityUrl)
      .then()
      .body(idFieldName, notNullValue())
      .body(versionFieldName, equalTo(0))
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(resultClass);
  }

  public <R> R readProperty(String id,String property,TypeRef<R> typeRef){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .get(baseEntityUrl+"/"+id+"/"+property)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(typeRef);
  }

  public <R> R patch(String id, Map<String,Object> patch,Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .body(patch)
      .patch(baseEntityUrl+"/"+id)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(resultClass);
  }

  public <R> R findById(String id,Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .get(baseEntityUrl+"/"+id)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(resultClass);
  }

  public <R> List<R> findAllById(Collection<Serializable> ids, Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .param("ids",ids)
      .when()
      .get(baseEntityUrl)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .body()
      .jsonPath()
      .getList(".", resultClass);
  }

  public <R> List<R> findAllPaged(String search, Class<R> resultClass){
    RequestSpecification reqSpec = RestAssured.given()
      .contentType(ContentType.JSON);
    if (search!=null) reqSpec.param("search",search);
    return
      reqSpec
      .when()
      .get(baseEntityUrl)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .body()
      .jsonPath()
      .getList("content", resultClass);
  }

  public <R> R addToCollection(String id, String collectionName, List<?> createEntityDTO, TypeRef<R> typeRef){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .body(createEntityDTO)
      .post(baseEntityUrl+"/"+id+"/"+collectionName+"/add")
      .then()
      .body("size()", is(createEntityDTO.size()))
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(typeRef);
  }

  public void removeFromCollection(String id, String collectionName, List<?> ids){
    RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .body(ids)
      .delete(baseEntityUrl+"/"+id+"/"+collectionName+"/remove")
      .then()
      .statusCode(200)
      .log()
      .all();
  }

  public void clearCollection(String id, String collectionName){
    RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .delete(baseEntityUrl+"/"+id+"/"+collectionName+"/clear")
      .then()
      .statusCode(200)
      .log()
      .all();
  }

  public void delete(String id){
    RestAssured.given()
      .when()
      .delete(baseEntityUrl+"/"+id)
      .then()
      .statusCode(200)
      .log()
      .all();
  }

  public Long count(String search){
    return RestAssured.given()
      .when()
      .get(baseEntityUrl+"/count")
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(Long.class);
  }


}

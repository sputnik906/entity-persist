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
  private final String hostUrl;

  @NonNull
  private final String idFieldName;

  @NonNull
  private final String versionFieldName;

  public <R> R create(String path, Object createEntityDTO, Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .body(createEntityDTO)
      .when()
      .post(hostUrl+"/"+path)
      .then()
      .body(idFieldName, notNullValue())
      .body(versionFieldName, equalTo(0))
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(resultClass);
  }

  public <R> R readProperty(String path,String id,String property,TypeRef<R> typeRef){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .get(hostUrl+"/"+path+"/"+id+"/"+property)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(typeRef);
  }

  public <R> R patch(String path,String id, Map<String,Object> patch,Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .body(patch)
      .patch(hostUrl+"/"+path+"/"+id)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(resultClass);
  }

  public <R> R findById(String path,String id,Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .get(hostUrl+"/"+path+"/"+id)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(resultClass);
  }

  public <R> List<R> findAllById(String path,Collection<Serializable> ids, Class<R> resultClass){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .param("ids",ids)
      .when()
      .get(hostUrl+"/"+path)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .body()
      .jsonPath()
      .getList(".", resultClass);
  }

  public <R> List<R> findAllPaged(String path,String search, Class<R> resultClass){
    RequestSpecification reqSpec = RestAssured.given()
      .contentType(ContentType.JSON);
    if (search!=null) reqSpec.param("search",search);
    return
      reqSpec
      .when()
      .get(hostUrl+"/"+path)
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .body()
      .jsonPath()
      .getList("content", resultClass);
  }

  public <R> R addToCollection(String path,String id, String collectionName, List<?> createEntityDTO, TypeRef<R> typeRef){
    return RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .body(createEntityDTO)
      .post(hostUrl+"/"+path+"/"+id+"/"+collectionName+"/add")
      .then()
      .body("size()", is(createEntityDTO.size()))
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(typeRef);
  }

  public void removeFromCollection(String path,String id, String collectionName, List<?> ids){
    RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .body(ids)
      .delete(hostUrl+"/"+path+"/"+id+"/"+collectionName+"/remove")
      .then()
      .statusCode(200)
      .log()
      .all();
  }

  public void clearCollection(String path,String id, String collectionName){
    RestAssured.given()
      .contentType(ContentType.JSON)
      .when()
      .delete(hostUrl+"/"+path+"/"+id+"/"+collectionName+"/clear")
      .then()
      .statusCode(200)
      .log()
      .all();
  }

  public void delete(String path,String id){
    RestAssured.given()
      .when()
      .delete(hostUrl+"/"+path+"/"+id)
      .then()
      .statusCode(200)
      .log()
      .all();
  }

  public Long count(String path,String search){
    return RestAssured.given()
      .when()
      .get(hostUrl+"/"+path+"/count")
      .then()
      .statusCode(200)
      .log()
      .all()
      .extract()
      .as(Long.class);
  }


}

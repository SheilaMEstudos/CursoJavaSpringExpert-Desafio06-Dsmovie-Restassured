package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import com.devsuperior.dsmovie.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MovieControllerRA {

	private String title;
	private Long movieId, nonExistingMovieId;
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;

	private Map<String, Object> postMovieIntance;

	@BeforeEach
	public void setUp() throws JSONException {
		baseURI = "http://localhost:8080";

		clientUsername = "alex@gmail.com";
		clientPassword = "123456";
		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);

		title = "Vingadores: Ultimato";
		movieId = 1L;
		nonExistingMovieId = 1000L;

		postMovieIntance = new HashMap<>();
		postMovieIntance.put("count", 0);
		postMovieIntance.put("id", 30);
		postMovieIntance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");
		postMovieIntance.put("score", 0.0);
		postMovieIntance.put("title", "Test Movie");
	}
	
	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
			given()
				.get("/movies")
			.then()
				.statusCode(200);
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
		given()
			.get("/movies?title={title}", title)
		.then()
			.statusCode(200)
			.body("content.title[0]", equalTo("Vingadores: Ultimato"));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		given()
			.get("/movies/{id}", movieId)
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"))
			.body("score", is(4.5F))
			.body("title", equalTo("The Witcher"))	;
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		given()
			.get("/movies/{id}", nonExistingMovieId)
		.then()
			.statusCode(404);
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {		
		postMovieIntance.put("title", " ");
		JSONObject newMovie = new JSONObject(postMovieIntance);

		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
				.body(newMovie.toJSONString())
				.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/movies")
			.then()
			.statusCode(422);
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieIntance);

		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.body(newMovie.toJSONString())
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/movies")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieIntance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + invalidToken)
				.body(newMovie.toJSONString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(401);
	}
}
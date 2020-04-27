/**
 * The MIT License
 * 
 * Copyright (c) 2019-2022 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.tutor.sample.repos;

import io.reactivex.Single;
import io.roxa.vertx.rx.jdbc.JdbcAgent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class DataRepository {

	private JdbcAgent jdbc;

	public DataRepository(JdbcAgent jdbc) {
		this.jdbc = jdbc;
	}

	public Single<Integer> upsertProduct(JsonObject productInfo) {
		long nowTimeMillis = System.currentTimeMillis();
		String productId = productInfo.getString("id");
		return jdbc.update(
				"insert into product(id, name, price, description, date_created, date_modified) values(?,?,?,?,?,?)"
						+ " on duplicate key update date_modified=?",
				new JsonArray().add(productId).add(productInfo.getString("name")).add(productInfo.getInteger("price"))
						.add(productInfo.getString("description")).add(nowTimeMillis).add(nowTimeMillis)
						.add(nowTimeMillis))
				.map(affect -> 0);
	}

	public Single<JsonObject> selectProductBy(String productId) {
		return jdbc.queryFirstRow(
				"select id, name, price, description, date_created, date_modified from product where id=?",
				new JsonArray().add(productId));
	}

	public Single<JsonArray> selectProducts() {
		return jdbc.queryRows("select id, name, price, description, date_created, date_modified from product", null)
				.map(rows -> new JsonArray(rows));
	}

	public Single<JsonObject> deleteProduct(String productId) {
		return jdbc.update("delete from product where id=?", new JsonArray().add(productId))
				.map(affected -> new JsonObject().put("product_id", productId));
	}

}

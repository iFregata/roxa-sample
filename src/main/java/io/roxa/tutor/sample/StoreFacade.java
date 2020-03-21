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
package io.roxa.tutor.sample;

import io.reactivex.Single;
import io.roxa.vertx.rx.EventActionDispatcher;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class StoreFacade extends EventActionDispatcher {

	public StoreFacade(String urn, String dataSourceName) {
		super(urn, dataSourceName);
	}

	public Single<JsonObject> saveProduct(JsonObject productInfo) {
		long nowTimeMillis = System.currentTimeMillis();
		String productId = productInfo.getString("id");
		return jdbc.update(
				"insert into product(id, name, price, description, date_created, date_modified) values(?,?,?,?,?,?)"
						+ " on duplicate key update date_modified=?",
				new JsonArray().add(productId).add(productInfo.getString("name")).add(productInfo.getInteger("price"))
						.add(productInfo.getString("description")).add(nowTimeMillis).add(nowTimeMillis)
						.add(nowTimeMillis))
				.flatMap(affected -> {
					return jdbc.queryFirstRow(
							"select id, name, price, description, date_created, date_modified from product where id=?",
							new JsonArray().add(productId));
				});
	}

	public Single<JsonObject> findProduct(JsonObject params) {
		String productId = params.getString("productId");
		return jdbc.queryFirstRow(
				"select id, name, price, description, date_created, date_modified from product where id=?",
				new JsonArray().add(productId));
	}

	public Single<JsonArray> listProducts() {
		return jdbc.queryRows("select id, name, price, description, date_created, date_modified from product", null)
				.map(rows -> new JsonArray(rows));
	}

	public Single<JsonObject> removeProduct(JsonObject params) {
		String productId = params.getString("productId");
		return jdbc.update("delete from product where id=?", new JsonArray().add(productId))
				.map(affected -> new JsonObject().put("product_id", productId));
	}
}

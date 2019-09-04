/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.tutor.sample;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.roxa.vertx.rx.jdbc.JdbcExecutor;
import io.roxa.vertx.rx.jdbc.JdbcManager;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class StoreRepositoryImpl implements StoreRepository {

	private JdbcExecutor jdbc;

	public StoreRepositoryImpl() {
		JdbcManager.register("mystore", this::setJdbc);
	}

	protected void setJdbc(JdbcExecutor jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Single<JsonObject> findProduct(String productId) {
		return jdbc.queryFirstRow(
				"select id, name, price, description, date_created, date_modified from product where id=?",
				new JsonArray().add(productId));
	}

	@Override
	public Single<List<JsonObject>> listProducts() {
		return jdbc.queryRows("select id, name, price, description, date_created, date_modified from product", null);
	}

	@Override
	public Single<JsonObject> saveProduct(JsonObject productInfo) {
		long nowTimeMillis = System.currentTimeMillis();
		String productId = productInfo.getString("id");
		return jdbc.with(conn -> {
			return jdbc.queryFirstRow("select id from product where id=?",
					new JsonArray().add(productId == null ? "NIL" : productId)).flatMap(row -> {
						if (row == null || row.isEmpty())
							return jdbc.update(conn,
									"insert into product(id, name, price, description, date_created, date_modified) values(?,?,?,?,?,?)",
									new JsonArray().add(productId).add(productInfo.getString("name"))
											.add(productInfo.getInteger("price"))
											.add(productInfo.getString("description")).add(nowTimeMillis)
											.add(nowTimeMillis));
						return jdbc.update(conn,
								"update product set name=?, price=?, description=?, date_modified=? where id=?",
								new JsonArray().add(productInfo.getString("name")).add(productInfo.getInteger("price"))
										.add(productInfo.getString("description")).add(nowTimeMillis).add(productId));
					}).flatMap(affected -> {
						return jdbc.queryFirstRow(conn,
								"select id, name, price, description, date_created, date_modified from product where id=?",
								new JsonArray().add(productId));
					});
		});
	}

	@Override
	public Completable removeProduct(String productId) {
		return jdbc.updateCompletable("delete from product where id=?", new JsonArray().add(productId));
	}

}

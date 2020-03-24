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
package io.roxa.tutor.sample.bloc;

import io.reactivex.Single;
import io.roxa.tutor.sample.repos.DataRepository;
import io.roxa.vertx.rx.EventActionDispatcher;
import io.roxa.vertx.rx.jdbc.JdbcExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class StoreFacade extends EventActionDispatcher {

	private DataRepository dataRepository;

	public StoreFacade(String urn, String dataSourceName) {
		super(urn, dataSourceName);
	}

	protected void setJdbc(JdbcExecutor jdbc) {
		dataRepository = new DataRepository(jdbc);
	}

	public Single<JsonObject> doSomeScheduledJob() {
		return Single.just(new JsonObject().put("job_status", "done"));
	}

	public Single<JsonObject> saveProduct(JsonObject productInfo) {
		String productId = productInfo.getString("productId");
		return dataRepository.insertProduct(productInfo).flatMap(i -> dataRepository.selectProductBy(productId));
	}

	public Single<JsonObject> findProduct(JsonObject params) {
		String productId = params.getString("productId");
		return dataRepository.selectProductBy(productId);
	}

	public Single<JsonArray> listProducts() {
		return dataRepository.selectProducts();
	}

	public Single<JsonObject> removeProduct(JsonObject params) {
		String productId = params.getString("productId");
		return dataRepository.deleteProduct(productId);
	}
}

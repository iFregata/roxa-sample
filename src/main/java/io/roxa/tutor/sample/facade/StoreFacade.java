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
package io.roxa.tutor.sample.facade;

import io.reactivex.Single;
import io.roxa.tutor.sample.repos.DataRepository;
import io.roxa.vertx.rx.EventActionDispatcher;
import io.roxa.vertx.rx.cassandra.CassandraAgent;
import io.roxa.vertx.rx.http.WebAPIs;
import io.roxa.vertx.rx.jdbc.JdbcAgent;
import io.roxa.vertx.rx.jdbc.JdbcDeployer;
import io.roxa.vertx.rx.nitrite.NitriteAgent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class StoreFacade extends EventActionDispatcher {

	private DataRepository dataRepository;

	private CassandraAgent cassandraAgent;

	private NitriteAgent nitriteAgent;

	public StoreFacade(String urn, JdbcDeployer jdbcDeployer) {
		super(urn, jdbcDeployer);
	}

	@Override
	protected void didSetupDispatch() {
		cassandraAgent = CassandraAgent.create(vertx, "unicorn");
		nitriteAgent = NitriteAgent.create(vertx, "appNO2");

	}

	@Override
	protected void setJdbcAgent(JdbcAgent jdbc) {
		dataRepository = new DataRepository(jdbc);
	}

	public Single<JsonObject> createWebAPIClient(JsonObject registerInfo) {
		String clientTitle = registerInfo.getString("client_title");
		JsonObject clientInfo = WebAPIs.generateClientRegister(clientTitle);
		JsonObject clientId = new JsonObject().put("client_id", clientInfo.getString("client_id"));
		return nitriteAgent.upsert("webapi_clients", clientId, clientInfo).flatMap(affect -> {
			return nitriteAgent.selectFirst("webapi_clients", clientId);
		});
	}

	public Single<JsonArray> listWebAPIClient() {
		return nitriteAgent.select("webapi_clients");
	}

	public Single<JsonObject> doSomeScheduledJob() {
		return Single.just(new JsonObject().put("job_status", "done"));
	}

	public Single<JsonArray> listSales() {
		return cassandraAgent.queryRows("select * from unicorn_data.turnover_per_5m where dmd_moy=?",
				new JsonArray().add("2020-04"));
	}

	public Single<JsonObject> saveProduct(JsonObject productInfo) {
		String productId = productInfo.getString("productId");
		return dataRepository.upsertProduct(productInfo).flatMap(i -> dataRepository.selectProductBy(productId));
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

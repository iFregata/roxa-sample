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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.roxa.vertx.rx.http.AbstractHttpVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @author Steven Chen
 *
 */
public class AppServer extends AbstractHttpVerticle {

	private static final Logger logger = LoggerFactory.getLogger(AppServer.class);

	private RedisExecutor redis;

	private DataAccess1 da1;

	private DataAccess2 da2;

	public AppServer(JsonObject conf) {
		super(conf);
	}

	@Override
	protected String getServerName() {
		return "Sample Server";
	}

	@Override
	protected Completable setupResources() {
		this.redis = RedisExecutor.create(vertx, getServerConfiguration());
		da1 = new DataAccess1();
		da2 = new DataAccess2();
		return super.setupResources();
	}

	@Override
	protected Single<Router> setupRouter(Router router) {
		router.post(pathOf("/redis/json")).handler(this::postJson);
		router.post(pathOf("/redis/kv")).handler(this::postKv);
		router.get(pathOf("/jdbc")).handler(this::pingJdbc);
		router.get(pathOf("/redis/json/:key")).handler(this::getJson);
		router.get(pathOf("/redis/kv")).handler(this::getKv);
		return super.setupRouter(router);
	}

	private void pingJdbc(RoutingContext rc) {
		da2.queryNum().subscribe(rs -> {
			logger.info("Query num: {}", rs.encode());
		}, e -> {
			logger.error("dd", e);
			failed(rc, e);
		});
		da1.queryData().subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			logger.error("dd", e);
			failed(rc, e);
		});

	}

	private void postJson(RoutingContext rc) {
		JsonObject body = rc.getBodyAsJson();
		String key = requestParam(rc, "key");
		redis.put(key, body).subscribe(() -> {
			succeeded(rc);
		}, e -> {
			failed(rc, e);
		});

	}

	private void getJson(RoutingContext rc) {
		String key = requestParam(rc, "key");
		redis.getJsonObject(key).subscribe((s) -> {
			logger.debug("String resp: {}", s);
			succeeded(rc, s);
		}, e -> {
			failed(rc, e);
		});

	}

	private void postKv(RoutingContext rc) {
		JsonObject body = rc.getBodyAsJson();
		String key = body.getString("key");
		String value = body.getString("value");
		redis.set(key, value).subscribe(() -> {
			succeeded(rc);
		}, e -> {
			failed(rc, e);
		});

	}

	private void getKv(RoutingContext rc) {
		String key = requestParam(rc, "key");
		redis.get(key).subscribe(v -> {
			succeeded(rc, new JsonObject().put("key", key).put("value", v));
		}, e -> {
			failed(rc, e);
		});

	}

}

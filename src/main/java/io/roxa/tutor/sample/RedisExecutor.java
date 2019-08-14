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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.Response;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.impl.types.SimpleStringType;

/**
 * @author Steven Chen
 *
 */
public class RedisExecutor {

	private static final Logger logger = LoggerFactory.getLogger(RedisExecutor.class);

	private static final JsonObject EMPTY_JsonObject = new JsonObject();

	private RedisAPI redisAPI;

	RedisExecutor() {
	}

	public static RedisExecutor create(Vertx vertx, JsonObject conf) {
		RedisExecutor redisExecutor = new RedisExecutor();
		createRedisClient(vertx, conf).subscribe(redis -> {
			redisExecutor.redisAPI = RedisAPI.api(redis);
		}, e -> {
			throw new RuntimeException(e);
		});
		return redisExecutor;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Completable set(String key, String value) {
		return redisAPI.rxSet(Arrays.asList(key, value)).ignoreElement();
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Single<String> get(String key) {
		return redisAPI.rxGet(key).toSingle(Response.newInstance(SimpleStringType.create("NIL"))).map(resp -> {
			return resp.toString();
		});
	}

	/**
	 * @param key
	 * @param body
	 */
	public Completable put(String key, JsonObject data) {
		return redisAPI.rxHmset(hmsetLeteral(key, data)).ignoreElement();
	}

	/**
	 * Get JSON value with key
	 * 
	 * @param key
	 * @return
	 */
	public Single<JsonObject> getJsonObject(String key) {
		return redisAPI.rxHgetall(key).map(resp -> {
			if (resp.size() == 0)
				return EMPTY_JsonObject;
			JsonObject json = new JsonObject();
			for (int i = 0; i < resp.size(); i += 2) {
				json.put(resp.get(i).toString(), resp.get(i + 1).toString());
			}
			return json;
		}).toSingle();

	}

	private static Single<Redis> createRedisClient(Vertx vertx, JsonObject conf) {
		RedisOptions redisOptions = new RedisOptions();
		JsonArray redisGroup = conf.getJsonArray("redis_conf");
		if (redisGroup == null || redisGroup.isEmpty())
			throw new IllegalStateException("No redis host, port configuration found!");
		if (redisGroup.size() == 1) {
			JsonObject cfg = (JsonObject) redisGroup.getJsonObject(0);
			logger.debug("Prepare to connect redis standalone server, {}", cfg.encode());
			return Redis.createClient(vertx, io.vertx.reactivex.core.net.SocketAddress
					.inetSocketAddress(cfg.getInteger("port"), cfg.getString("host"))).rxConnect();
		}
		redisGroup.stream().forEach(e -> {
			JsonObject cfg = (JsonObject) e;
			redisOptions.addEndpoint(SocketAddress.inetSocketAddress(cfg.getInteger("port"), cfg.getString("host")));
		});
		logger.debug("Prepare to connect redis cluster servers, {}", redisGroup.encode());
		return Redis.createClient(vertx, redisOptions).rxConnect();
	}

	private static List<String> hmsetLeteral(String key, JsonObject data) {
		List<String> list = new ArrayList<>();
		list.add(key);
		data.stream().forEach(e -> {
			list.add(e.getKey());
			list.add(String.valueOf(e.getValue()));
		});
		return list;
	}

}

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

import io.reactivex.Single;
import io.roxa.util.Codecs;
import io.roxa.util.Moments;
import io.roxa.util.Randoms;
import io.roxa.vertx.rx.EventActionEndpoint;
import io.roxa.vertx.rx.http.AbstractHttpVerticle;
import io.roxa.vertx.rx.http.WebAPIs;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @author Steven Chen
 *
 */
public class APIServer extends AbstractHttpVerticle {

	private static final Logger logger = LoggerFactory.getLogger(APIServer.class);

	private String storeFacadeURN;

	public APIServer(JsonObject conf, String storeFacadeURN) {
		super(conf);
		this.storeFacadeURN = storeFacadeURN;
	}

	@Override
	protected String getServerName() {
		return "Sample API Server";
	}

	@Override
	protected Single<Router> setupRouter(Router router) {
		router.get(pathOf("/api/client")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::generateClient);
		router.post(pathOf("/api/client-bearer")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::generateBearer);
		router.get(pathOf("/products/:productId")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::findProductHandler);
		router.get(pathOf("/products")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::listProductHandler);
		router.delete(pathOf("/products/:productId")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::removeProductHandler);
		router.post(pathOf("/products")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::saveProductHandler);

		router.get(pathOf("/sales")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::listSales);

		router.post(pathOf("/client-register")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::clientRegister);
		router.get(pathOf("/client-register")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::listClientRegister);
		return super.setupRouter(router);
	}

	private void clientRegister(RoutingContext rc) {
		JsonObject registerInfo = rc.getBodyAsJson();
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("createWebAPIClient")
				.<JsonObject>request(registerInfo.copy()).subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

	private void listClientRegister(RoutingContext rc) {
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("listWebAPIClient").<JsonArray>request()
				.subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

	private void listSales(RoutingContext rc) {
		authorize(rc, getServerConfiguration().getJsonObject("auth_policy")).flatMap(authInfo -> {
			return EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("listSales").<JsonArray>request();
		}).subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

	private void generateBearer(RoutingContext rc) {
		JsonObject signInfo = rc.getBodyAsJson();
		String bearerHeader = WebAPIs.bearerAuthorization(signInfo.getString("client_id"),
				signInfo.getString("client_key"), signInfo.getString("verb"));
		succeeded(rc, new JsonObject().put("bearer_header", bearerHeader));
	}

	private void generateClient(RoutingContext rc) {
		String clientId = Codecs
				.asMD5String(String.format("%s@%d", Randoms.randomHexString(12), Moments.currentTimeMillis()));
		String clientKey = Randoms.randomString(16);
		succeeded(rc, new JsonObject().put("client_id", clientId).put("client_key", clientKey));
	}

	private void saveProductHandler(RoutingContext rc) {
		logger.debug("Handle the save product request.");
		JsonObject productInfo = rc.getBodyAsJson();
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("saveProduct")
				.<JsonObject>request(productInfo.copy()).subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

	@Override
	protected String getAuthBaseSecretCode() {
		return "abc";
	}

	@Override
	protected Single<JsonObject> getClientRegister(String clientId) {
		return Single.just(getServerConfiguration().getJsonObject("auth_policy"));
	}

	private void listProductHandler(RoutingContext rc) {
		authorize(rc, getServerConfiguration().getJsonObject("auth_policy")).flatMap(authInfo -> {
			return EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("listProducts").<JsonArray>request();
		}).subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

	private void findProductHandler(RoutingContext rc) {
		logger.debug("Handle the find product request.");
		String productId = requestParam(rc, "productId");
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("findProduct")
				.<JsonObject>request(new JsonObject().put("productId", productId)).subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

	private void removeProductHandler(RoutingContext rc) {
		logger.debug("Handle the remove product request.");
		String productId = requestParam(rc, "productId");
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("removeProduct")
				.<JsonObject>request(new JsonObject().put("productId", productId)).subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

}

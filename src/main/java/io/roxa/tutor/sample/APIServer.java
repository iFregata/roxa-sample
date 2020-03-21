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
import io.roxa.vertx.rx.EventActionDispatcherHelper;
import io.roxa.vertx.rx.http.AbstractHttpVerticle;
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
		router.get(pathOf("/products/:productId")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::findProductHandler);
		router.get(pathOf("/products")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::listProductHandler);
		router.delete(pathOf("/products/:productId")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::removeProductHandler);
		router.post(pathOf("/products")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::saveProductHandler);
		return super.setupRouter(router);
	}

	private void saveProductHandler(RoutingContext rc) {
		logger.debug("Handle the save product request.");
		JsonObject productInfo = rc.getBodyAsJson();
		EventActionDispatcherHelper.<JsonObject>request(vertx, storeFacadeURN, "saveProduct", productInfo.copy())
				.subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

	private void listProductHandler(RoutingContext rc) {
		logger.debug("Handle the list product request.");
		EventActionDispatcherHelper.<JsonArray>request(vertx, storeFacadeURN, "listProducts").subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

	private void findProductHandler(RoutingContext rc) {
		logger.debug("Handle the find product request.");
		String productId = requestParam(rc, "productId");
		EventActionDispatcherHelper
				.<JsonObject>request(vertx, storeFacadeURN, "findProduct", new JsonObject().put("productId", productId))
				.subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

	private void removeProductHandler(RoutingContext rc) {
		logger.debug("Handle the remove product request.");
		String productId = requestParam(rc, "productId");
		EventActionDispatcherHelper.<JsonObject>request(vertx, storeFacadeURN, "removeProduct",
				new JsonObject().put("productId", productId)).subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
	}

}

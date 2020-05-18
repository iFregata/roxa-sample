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
import io.roxa.http.BadRequestException;
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
		setFileUploadsLocation(System.getProperty("user.dir") + "/upload");
	}

	@Override
	protected String getServerName() {
		return "Sample API Server";
	}

	@Override
	protected Single<Router> setupRouter(Router router) {
		/* These are basic jdbc CRUD operation */
		router.get(pathOf("/products/:productId")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::findProductHandler);
		router.get(pathOf("/products")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::listProductHandler);
		router.delete(pathOf("/products/:productId")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::removeProductHandler);
		router.post(pathOf("/products")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::saveProductHandler);

		/* This is to demo the cassandra connection */
		router.get(pathOf("/sales")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::listSales);

		/*
		 * This is to demo the jdbc batch proccssing and jdbc signle connection
		 * transaction
		 */
		router.post(pathOf("/sales")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::bookSale);

		/* This is a demo for client upload a file */
		router.post(pathOf("/upload")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::uploadHandle);

		/*
		 * These are authorization and authentication demo, that can enable the client
		 * token and client payload signuature
		 */
		router.post(pathOf("/authz/client-register")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::clientRegister);

		router.get(pathOf("/authz/client-register")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::listClientRegister);

		router.get(pathOf("/authz/sign-sample")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::clientSignSample);

		router.get(pathOf("/authz/token")).produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::getToken);

		router.get(pathOf("/authz/token-protected")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::getTokenProtectedResource);

		router.get(pathOf("/authz/sign-protected")).produces(MEDIA_TYPE_APPLICATION_JSON)
				.handler(this::getSignProtectedResource);

		return super.setupRouter(router);
	}

	/**
	 * auth_polic.mode: bypass, base, bearer
	 */
	protected String getAuthBaseSecretCode() {
		return "abc";
	}

	protected Single<JsonObject> getClientRegister(String clientId) {
		logger.debug("client id: {}", clientId);
		return EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("findWebAPIClient")
				.<JsonObject>request(new JsonObject().put("client_id", clientId));
	}

	private void clientSignSample(RoutingContext rc) {
		String clientId = requestParam(rc, "client_id");
		getClientRegister(clientId).map(clientRegister -> {
			return WebAPIs.bearerAuthorization(clientRegister.getString("client_id"),
					clientRegister.getString("client_key"), "GET");
		}).subscribe(rs -> {
			succeeded(rc, new JsonObject().put("auth", rs));
		}, e -> {
			failed(rc, e);
		});
	}

	private void getTokenProtectedResource(RoutingContext rc) {
		verifyClientToken(rc).map(clientRegister -> {
			logger.info("Client register: {}", clientRegister.encode());
			return new JsonObject().put("content", "Token protected resource");
		}).subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

	private void getSignProtectedResource(RoutingContext rc) {
		authorize(rc, getServerConfiguration().getJsonObject("auth_policy")).map(clientRegister -> {
			logger.info("Client register: {}", clientRegister.encode());
			return new JsonObject().put("content", "Sign protected resource");
		}).subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

	private void getToken(RoutingContext rc) {
		issueClientToken(rc, getServerConfiguration().getJsonObject("token_policy")).subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

	private void uploadHandle(RoutingContext rc) {
		JsonObject uploadFileInfo = resolveFileInfo(rc);
		if (uploadFileInfo == null) {
			failed(rc, new BadRequestException("No file data found!"));
			return;
		}
		logger.debug("upload file info:{}", uploadFileInfo.encode());
		succeeded(rc, uploadFileInfo);
	}

	private void bookSale(RoutingContext rc) {
		JsonObject saleInfo = rc.getBodyAsJson();
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("bookSale").<JsonObject>request(saleInfo.copy())
				.subscribe(rs -> {
					succeeded(rc, rs);
				}, e -> {
					failed(rc, e);
				});
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

	// To show this method must be enabled the cassandra agent!!!!
	private void listSales(RoutingContext rc) {
		EventActionEndpoint.create(vertx).urn(storeFacadeURN).action("listSales").<JsonArray>request().subscribe(rs -> {
			succeeded(rc, rs);
		}, e -> {
			failed(rc, e);
		});
	}

}

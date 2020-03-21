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

import io.reactivex.Completable;
import io.roxa.vertx.Runner;
import io.roxa.vertx.rx.AbstractBootVerticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class Bootstrap extends AbstractBootVerticle {
	private static final String storeFacadeURU = StoreFacade.class.getName();

	public static void main(String[] args) {
		Runner.run(Bootstrap.class.getName());
	}

	public void start() throws Exception {
		setupJdbcManager();
	}

	protected Completable deploy(JsonObject conf) {

		return redeploy(new StoreFacade(storeFacadeURU, "mystore"))
				.andThen(redeploy(new APIServer(conf, storeFacadeURU)));
	}
}

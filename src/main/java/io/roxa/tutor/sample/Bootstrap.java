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
import io.roxa.vertx.rx.AbstractBootVerticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class Bootstrap extends AbstractBootVerticle {

	public void start() throws Exception {
		setupJdbcManager();
	}

	protected Completable deploy(JsonObject conf) {
		return redeploy(new AppServer(conf));
	}
}

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
import io.roxa.vertx.rx.AbstractBootVerticle;
import io.vertx.config.ConfigChange;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;

/**
 * @author Steven Chen
 *
 */
public class Bootstrap extends AbstractBootVerticle {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	@Override
	public void start(Future<Void> startFuture) throws Exception {

	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		configuration().flatMapCompletable(this::deploy).subscribe(CompletableHelper.toObserver(startPromise.future()));
	}

	protected void configurationChanged(ConfigChange change) {
		JsonObject cfgNew = change.getNewConfiguration();
		JsonObject cfgOld = change.getPreviousConfiguration();
		if (!cfgNew.equals(cfgOld)) {
			logger.debug("Configuration change detected, the new one is {}", cfgNew.encodePrettily());
			deploy(cfgNew).subscribe(() -> logger.info("Apply new configuration to deploy succeeded"),
					e -> logger.error("Apply new configuration to deploy failed", e));
		}
	}

	private Completable deploy(JsonObject conf) {
		return redeploy(new AppServer(conf));
	}
}

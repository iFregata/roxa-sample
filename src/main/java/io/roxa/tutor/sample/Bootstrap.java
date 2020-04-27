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
import io.roxa.tutor.sample.facade.StoreFacade;
import io.roxa.vertx.Runner;
import io.roxa.vertx.rx.AbstractBootVerticle;
import io.roxa.vertx.rx.cassandra.CassandraDeployer;
import io.roxa.vertx.rx.nitrite.NitriteDeployer;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class Bootstrap extends AbstractBootVerticle {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	private static final String storeFacadeURU = StoreFacade.class.getName();

	public static void main(String[] args) {
		Runner.run(Bootstrap.class.getName());
	}

	public void start() throws Exception {
		// setupCronScheduler();
		setupCassandraManager();
		setupNitriteManager();
		setupJdbcDeployer("mystore");
	}

	protected void setupNitriteManager() {
		deploy(new NitriteDeployer("appNO2")).subscribe(id -> {
			logger.info("Deployed NitriteDeployer with id: {}", id);
		}, e -> {
			logger.info("Deployed NitriteDeployer error", e);
		});
	}

	protected void setupCassandraManager() {
		deploy(new CassandraDeployer("unicorn")).subscribe(id -> {
			logger.info("Deployed CassandraDeployer with id: {}", id);
		}, e -> {
			logger.info("Deployed CassandraDeployer error", e);
		});
	}

	protected Completable configure(JsonObject conf) {
		return awareDeploy(new StoreFacade(storeFacadeURU, getJdbcDeployer("mystore")))
				.andThen(awareDeploy(new APIServer(conf, storeFacadeURU)));
		// .andThen(redeploy(new JobScheduler(storeFacadeURU)))
	}
}

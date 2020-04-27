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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.roxa.vertx.rx.EventActionEndpoint;
import io.roxa.vertx.rx.cron.CronScheduler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;

/**
 * @author Steven Chen
 *
 */
public class JobScheduler extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
	private String serviceURN;

	public JobScheduler(String serviceURN) {
		this.serviceURN = serviceURN;
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		final String cronExpr = "0 0/1 * 1/1 * ? *";
		CronScheduler.schedule(vertx, "CronScheduler-Test", cronExpr).subscribe(next -> {
			logger.info("Test the scheduler with cronExpr: {}", cronExpr);
			EventActionEndpoint.create(vertx).urn(serviceURN).action("doSomeScheduledJob").<JsonObject>request()
					.subscribe(rs -> {
						logger.info("Test the job: {}", rs.encode());
					}, e -> {
						logger.warn("Test the job failed", e);
					});
		}, e -> {
			logger.error("Test the scheduler failed");
		}, () -> {
			logger.info("Test the scheduler completed");
		});
		super.start(startPromise);
	}

}

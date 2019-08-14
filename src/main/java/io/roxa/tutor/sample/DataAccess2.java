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
import io.roxa.vertx.rx.jdbc.JdbcExecutor;
import io.roxa.vertx.rx.jdbc.JdbcManager;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class DataAccess2 {
	private static final Logger logger = LoggerFactory.getLogger(DataAccess2.class);

	private JdbcExecutor jdbc;

	public DataAccess2() {
		JdbcManager.register("sample", this::setJdbc);
	}

	/**
	 * @param jdbc the jdbc to set
	 */
	protected void setJdbc(JdbcExecutor jdbc) {
		logger.debug("Set Jdbc: {}", jdbc);
		this.jdbc = jdbc;
	}

	public Single<JsonObject> queryNum() {
		return jdbc.queryFirstRow("select 1 as num", null);
	}
}

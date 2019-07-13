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

import io.roxa.vertx.http.AbstractHttpVerticle;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public class SampleServer extends AbstractHttpVerticle {

	public SampleServer(JsonObject conf) {
		super(conf);
	}

	@Override
	protected String getServerName() {
		return "Sample Server";
	}

}

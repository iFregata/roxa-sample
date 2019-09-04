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

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

/**
 * @author Steven Chen
 *
 */
public interface StoreRepository {

	/**
	 * The factory for StoreRepository implementation
	 * 
	 * @return
	 */
	static StoreRepository create() {
		return new StoreRepositoryImpl();
	}

	/**
	 * Find a product by ID
	 * 
	 * @param productId - The ID of product
	 * @return - The product info represented by JSON
	 */
	Single<JsonObject> findProduct(String productId);

	/**
	 * Retrieve the all of products
	 * 
	 * @return - All of products that represented by list of JSON
	 */
	Single<List<JsonObject>> listProducts();

	/**
	 * Save the product info
	 * 
	 * @param productInfo - JSON
	 * @return - Saved the product info
	 */
	Single<JsonObject> saveProduct(JsonObject productInfo);

	/**
	 * Remove the product info by ID
	 * 
	 * @param productId - The ID of product
	 * @return
	 */
	Completable removeProduct(String productId);

}

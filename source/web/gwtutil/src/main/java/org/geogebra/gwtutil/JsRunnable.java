package org.geogebra.gwtutil;

import jsinterop.annotations.JsFunction;

/**
 * JavaScript function with no arguments.
 */
@JsFunction
public interface JsRunnable {
	/**
	 * Call the function.
	 */
	void run();
}

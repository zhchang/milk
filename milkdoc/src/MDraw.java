/**
 * Abstract conceptual class. Not to be used directly in milk programming. Only
 * its subclasses can be used.
 * 
 * @author lijing
 * @see MPlayer
 * @see MText
 * @see MGroup
 * @see MTiles
 */
public abstract class MDraw {

	/**
	 * setter for ZIndex
	 * 
	 * @param value
	 *            new ZIndex to be set
	 * 
	 */
	void setZIndex(Int value) {

	}

	/**
	 * getter for ZIndex
	 * 
	 * @return current ZIndex
	 */
	Int getZIndex() {
		return null;
	}

	/**
	 * setter for x
	 * 
	 * @param value
	 *            new x (Local) to be set
	 */
	void setX(Int value) {

	}

	/**
	 * getter for x
	 * 
	 * @return current x (Local)
	 */
	Int getX() {
		return null;
	}

	/**
	 * setter for y
	 * 
	 * @param value
	 *            new y (Local) to be set
	 */
	void setY(Int value) {

	}

	/**
	 * getter for y
	 * 
	 * @return current y (Local)
	 */
	Int getY() {
		return null;
	}

	/**
	 * setter for visibility
	 * 
	 * @param value
	 *            new visibility to be set (1 for visible, 0 for invisible)
	 */
	void setVisible(Int value) {

	}

	/**
	 * getter for visibility
	 * 
	 * @return current visibility (1 for visible, 0 for invisible)
	 */
	Int getVisible() {
		return null;
	}

	/**
	 * setter for parent
	 * 
	 * @param value
	 *            new parent to be set
	 */
	void setParent(MGroup value) {

	}

	/**
	 * getter for parent
	 * 
	 * @return current parent
	 */
	MGroup getParent() {
		return null;
	}

	/**
	 * input global coordinates and return coordinates.
	 * 
	 * @param x
	 *            Global x
	 * @param y
	 *            Global y
	 * @return [localX, localY]
	 */
	Array getLocalPoint(Int x, Int y) {
		return null;
	}

	/**
	 * input local coordinates and return global coordinates
	 * 
	 * @param x
	 *            Local x
	 * @param y
	 *            Local y
	 * @return [GlobalX, GlobalY]
	 */
	Array getGlobalPoint(Int x, Int y) {
		return null;
	}

	/**
	 * hook callback to fingerDown event on the drawable Object.
	 * <p>
	 * Callback will be fed with 3 parameters:
	 * <ul>
	 * <li>Int x (Global x)</li>
	 * <li>Int y (Global y)</li>
	 * <li>MPlayer/MText obj (which captured the event)</li>
	 * </ul>
	 * and it returns a Int value indicating whether the callback consumes this
	 * event. (1:consumed,0:not) If the event is not consumed, then it will be
	 * bubbled to Globally hooked callback.
	 * 
	 * <p>
	 * The callback will be triggered if finger down event is captured by the
	 * drawable object
	 * <p>
	 * Hook callback event on MGroup/MTiles objects has no effects.
	 * 
	 * @param callback
	 *            the function to be hooked
	 */
	void onFingerDown(Callback callback) {

	}

	/**
	 * hook callback to fingerUp event on the drawable Object.
	 * <p>
	 * Callback will be fed with 3 parameters:
	 * <ul>
	 * <li>Int x (Global x)</li>
	 * <li>Int y (Global y)</li>
	 * <li>MPlayer/MText obj (which captured the event)</li>
	 * </ul>
	 * and it returns a Int value indicating whether the callback consumes this
	 * event. (1:consumed,0:not) If the event is not consumed, then it will be
	 * bubbled to Globally hooked callback.
	 * 
	 * <p>
	 * The callback will be triggered if finger down event is captured by the
	 * drawable object
	 * <p>
	 * Hook callback event on MGroup/MTiles objects has no effects.
	 * 
	 * @param callback
	 *            the function to be hooked
	 */
	void onFingerUp(Callback callback) {

	}

	/**
	 * hook callback to fingerMove event on the drawable Object.
	 * <p>
	 * Callback will be fed with 3 parameters:
	 * <ul>
	 * <li>Int x (Global x)</li>
	 * <li>Int y (Global y)</li>
	 * <li>MPlayer/MText obj (which captured the event)</li>
	 * </ul>
	 * and it returns a Int value indicating whether the callback consumes this
	 * event. (1:consumed,0:not) If the event is not consumed, then it will be
	 * bubbled to Globally hooked callback.
	 * 
	 * <p>
	 * The callback will be triggered if finger down event is captured by the
	 * drawable object
	 * <p>
	 * Hook callback event on MGroup/MTiles objects has no effects.
	 * 
	 * @param callback
	 *            the function to be hooked
	 */
	void onFingerMove(Callback callback) {

	}

	/**
	 * move drawable object to destination within time duration specified, with
	 * finish callback.
	 * 
	 * @param duration
	 *            in milli seconds
	 * @param destX
	 *            in local coordinates
	 * @param destY
	 *            in local coordinates
	 * @param callback
	 *            takes MDraw Object as input (only one input).
	 */
	void moveTo(Int duration, Int destX, int destY, Callback callback) {

	}

	/**
	 * moveTo without callback. (Last parameter must be LITERAL -1!)
	 * 
	 * @param duration
	 *            in milli seconds
	 * @param destX
	 *            in local coordinates
	 * @param destY
	 *            in local coordinates
	 * @param callback
	 *            (LITERAL -1)
	 */
	void moveTo(Int duration, Int destX, int destY, Int callback) {

	}

	/**
	 * stop the last movement.
	 */
	void stop() {

	}

}

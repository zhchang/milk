package com.mozat.sv3.smartview3.utils;

import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.elements.Sv3Input;

public interface IEventHandler {
	// public static byte EVENT_WILL_TRIGGER = 0, EVENT_WILL_FOCUS = 1, EVENT_DID_FOCUS = 2, EVENT_WILL_UNFOCUS = 3,
	// EVENT_DID_UNFOCUS = 4, EVENT_STATE_WILL_CHANGE = 5, EVENT_STATE_DID_CHANGE = 6, EVENT_DID_FINISH_INPUT = 7,
	// EVENT_TYPE_COUNT = 8;

	/**
	 * @param element
	 * @return whether the event has been cancelled. true if cancelled by this handler and the event should NOT be
	 *         process further by the invoker.
	 */
	public boolean willTrigger(Sv3Element element);

	/**
	 * 
	 * @param element
	 * @return whether the event has been cancelled. true if cancelled by this handler and the event should NOT be
	 *         process further by the invoker.
	 */
	public boolean willFocus(Sv3Element element);

	public void didFocus(Sv3Element element);

	/**
	 * 
	 * @param element
	 * @return whether the event has been cancelled. true if cancelled by this handler and the event should NOT be
	 *         process further by the invoker.
	 */
	public boolean willEnable(Sv3Element elment);

	public void didEnable(Sv3Element element);

	/**
	 * 
	 * @param element
	 * @return whether the event has been cancelled. true if cancelled by this handler and the event should NOT be
	 *         process further by the invoker.
	 */
	public boolean willDisable(Sv3Element elment);

	public void didChangeAttrib(Sv3Element elem, String name, boolean needsRepaint, boolean needsReplayout);

	public void didDisable(Sv3Element element);

	/**
	 * 
	 * @param element
	 * @return whether the event has been cancelled. true if cancelled by this handler and the event should NOT be
	 *         process further by the invoker.
	 */
	public boolean willUnfocus(Sv3Element element);

	public void didUnfocus(Sv3Element element);

	/**
	 * 
	 * @param element
	 *            can be either Sv3Checkbox, Sv3Input or Sv3Select
	 * @return whether the event has been cancelled. true if cancelled by this handler and the event should NOT be
	 *         process further by the invoker.
	 */
	public boolean willChangeValue(Sv3Element element);

	/**
	 * 
	 * @param element
	 *            can be either Sv3Checkbox, Sv3Input or Sv3Select
	 */
	public void didChangeValue(Sv3Element element);

	public void didFinishInput(Sv3Input element);
}

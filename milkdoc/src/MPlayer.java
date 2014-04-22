/**
 * MPlayer class abstracts image or geo-shapes
 * <p>
 * an MPlayer object can have one or many states,each state can be defined as :
 * <ul>
 * <li>a single repeating frame</li>
 * <li>a series of image resources as frames with a frame-delay in milliseconds</li>
 * <li>a SPRITE resource, which can be divided into several image frames, and a
 * frame-delay in milliseconds</li>
 * </ul>
 * </p>
 * 
 * @author lijing
 * 
 */
public class MPlayer extends MDraw {
	/**
	 * get position rect
	 * 
	 * @return current position
	 */
	public MRect getRect() {
		return null;
	}

	/**
	 * set position rect
	 * 
	 * @param value
	 *            the rect to set
	 */
	public void setRect(MRect value) {

	}

	/**
	 * define a state that has a single frame
	 * 
	 * @param stateId
	 *            the uid of state
	 * @param resourceId
	 *            the id of resource
	 */
	public void defineState(Int stateId, String resourceId) {

	}

	/**
	 * define a state that has multiple frames
	 * <p>
	 * stateData(Array) has 2 forms:
	 * <ol>
	 * <li>
	 * [frameDelay(Int),finishCallback(Int),resourceId(String),[frame0(Int),
	 * frame1 (Int),...]]</li>
	 * <li>
	 * [frameDelay(Int),finishCallback(Int),resourceId0(String),resourceId1(
	 * String),...]</li>
	 * </ol>
	 * </p>
	 * <p>
	 * finishCallback is -1 or callback.toInt();
	 * </p>
	 * 
	 * @param stateId
	 *            the uid of state
	 * @param stateData
	 *            state data in Array form
	 */
	public void defineState(Int stateId, Array stateData) {

	}
}

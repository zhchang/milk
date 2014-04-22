package milk.implement;

public class Resource {

	public static final int TYPE_SOURCE = 0;
	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_AUDIO = 2;
	public static final int TYPE_9PATCH = 3;
	public static final int TYPE_L10N = 4;
	public static final int TYPE_PANORAMA = 5;
	public static final int TYPE_PAGE = 6;
	public static final int TYPE_L10N2 = 9;

	int version = 0;
	String resourceId;
	byte type;
	public byte split = 1;
	byte must = 1;

	public int loadW = 0;
	public int loadH = 0;
	public int alpha = 1;
}
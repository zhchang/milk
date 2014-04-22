package milk.implement.mk;

import milk.implement.MilkCallback;

public class MoveState {

	public static final int ConstantSpeed = 0;
	public static final int ConstantStep = 1;
	public static final int VariableSpeed = 4;

	public int startX;
	public int startY;
	public int destX;
	public int destY;
	public long time;
	public long startTime;
	public MilkCallback callback = null;

	public int mode = ConstantSpeed;

}

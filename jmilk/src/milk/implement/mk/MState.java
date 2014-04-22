package milk.implement.mk;

import java.util.Vector;

import milk.implement.MilkCallback;
import milk.implement.VectorPool;

public class MState {

	public static final byte stateTypeStatic = 0;
	public static final byte stateTypeDynamic = 1;
	public static final byte stateTypeSprite = 2;

	public int id;
	public Vector frames;
	public String staticFrame;
	public byte type;
	public int frameDelay;
	public long startTime;
	public MilkCallback finishCallback = null;
	public long lastCallback = 0;
	public String spriteId;
	public byte spriteIndex = 0;

	public MState() {

	}

	MState(MState state) {
		this.id = state.id;
		this.frames = state.frames;
		this.staticFrame = state.staticFrame;
		this.type = state.type;
		this.frameDelay = state.frameDelay;
		this.startTime = state.startTime;
		this.finishCallback = state.finishCallback;
		this.lastCallback = state.lastCallback;
		this.spriteId = state.spriteId;
		this.spriteIndex = state.spriteIndex;
	}

	public void start() {
		startTime = System.currentTimeMillis();
		lastCallback = System.currentTimeMillis();
	}

	public void setFrames(Vector frames) {
		VectorPool.recycle(this.frames);
		this.frames = frames;
	}

}

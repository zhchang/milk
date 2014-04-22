package milk.ui;

import java.util.TimerTask;

import milk.implement.TaskRunner;
import milk.ui2.MilkTask;

public class MilkTaskImpl extends MilkTask {
	public TimerTask task;

	public MilkTaskImpl(TaskRunner runner) {
		super(runner);
		task = new TimerTask() {
			public void run() {
				perform();
			}
		};
	}

	public void cancel() {
		task.cancel();
	}

}

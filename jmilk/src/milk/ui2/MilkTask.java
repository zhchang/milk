package milk.ui2;

import milk.implement.TaskRunner;

public abstract class MilkTask {

	public MilkTask(TaskRunner runner) {
		this.runner = runner;
	}

	TaskRunner runner;

	public void perform() {
		runner.doTask();
	}

	abstract public void cancel();

}

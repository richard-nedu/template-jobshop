package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Vector;

public class GreedySolverEST_LRPT implements Solver {
	
	private int getlong(Instance instance, Task task) {
		int tlong = 0;		
		for(int i = task.task; i<instance.numTasks; i++){
			tlong = tlong + instance.duration(task.job, i);
		}
		
		return(tlong);
	}

	private Task lrpt(Vector<Task> task, Instance instance){
		Task current = task.firstElement();
		Task best = current;
		for(int j=1; j<task.size(); j++) {
			if(getlong(instance, current) > getlong(instance,best)) {
				best = current;
			}
		}
			return(best);
	}

	private int start_time(Task task, Instance instance) {
		int time=0;
		for(int i=0; i<task.task; i++) {
			time = time+instance.duration(task.job, i);
		}
		return time;
	}
	
	private Task est_lrpt(Vector<Task> tasks, Instance instance){
		Vector<Task> possible_tasks = new Vector<Task>();
		Task best = tasks.firstElement();
		int best_time = start_time(best, instance);

		for(Task t : tasks) {
			if(start_time(t, instance) < best_time) {
				best_time = start_time(t, instance);
			}
		} 
		
		for(Task t : tasks) {
			if(start_time(t, instance) <= best_time) {
				possible_tasks.add(new Task(t.job,t.task));
			}
		}
		return lrpt(possible_tasks, instance); 
	}

	@Override
	public Result solve(Instance instance, long deadline) {
		//initialisations
		Vector<Task> realisable = new Vector<Task>();
		ResourceOrder sol = new ResourceOrder(instance);
		
		//init
		for(int j=0; j<instance.numJobs; j++){
			realisable.add(new Task(j,0));
		}
		
		int remainingTasks = instance.numJobs*instance.numMachines;
		int machine;
		while(remainingTasks>0){
			Task current = est_lrpt(realisable,instance);
			machine = instance.machine(current);
			sol.tasksByMachine[machine][sol.nextFreeSlot[machine]] = current;
			sol.nextFreeSlot[machine]++;
			if(current.task + 1 < instance.numMachines) {
			realisable.add(new Task(current.job, current.task + 1));
			}
			realisable.remove(current);
			remainingTasks --;
		}
		return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
		}
}

 

package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Vector;

public class GreedySolverEST_LRPT implements Solver {
	
	ArrayList<Task> TasksDone = new ArrayList<Task>();
	
	private int getlong(Instance instance, Task task) {
		int tlong = 0;		
		for(int i = task.task; i<instance.numTasks; i++){
			tlong = tlong + instance.duration(task.job, i);
		}
		
		return(tlong);
	}

	private Task lrpt(Vector<Task> task, Instance instance){
		Vector<Task> earliest_tasks = earliest_tasks(task, instance);
		Task current = earliest_tasks.firstElement();
		Task best = current;
		for(int j=1; j<earliest_tasks.size(); j++) {
			current = task.get(j);
			if(getlong(instance, current) > getlong(instance,best)) {
				best = current;
			}
		}
			return(best);
	}
	
	boolean TaskAlreadyDone(int task, int job, Instance instance){
		for(Task t : TasksDone) {
            if(t.job ==  task && t.task == task) return true;
        }
			
		return false;
			
		}
	
	private int start_time(Task t, Instance instance) {
		int[] nextFreeTimeResource = new int[instance.numMachines];
		int[] nextTask = new int[instance.numJobs];
		int[][] startTimes = new int[instance.numJobs][instance.numTasks];
		
        for(int job = 0; job < instance.numJobs; job++) {
            int task = nextTask[job];
            int machine = instance.machine(job, task);
            // earliest start time for this task
            int est = task == 0 ? 0 : startTimes[job][task-1] + instance.duration(job, task-1);
            est = Math.max(est, nextFreeTimeResource[machine]);

            startTimes[job][task] = est;
            nextFreeTimeResource[machine] = est + instance.duration(job, task);
            nextTask[job] = task + 1;
        }
        
		return startTimes[t.job][t.task];
	}
	
	private Vector<Task> earliest_tasks(Vector<Task> tasks, Instance instance){
		Vector<Task> possible_tasks = new Vector<Task>();
		Task best = tasks.firstElement();
		int best_time = start_time(best, instance);

		for(Task t : tasks) {
			if(start_time(t, instance) < best_time) {
				best_time = start_time(t, instance);
			}
		} 
		
		for(Task t : tasks) {
			if(start_time(t, instance) == best_time) {
				possible_tasks.add(new Task(t.job,t.task));
			}
		}
		return possible_tasks; 
	}

	@Override
	public Result solve(Instance instance, long deadline) {
		Vector<Task> realisable = new Vector<Task>();
		ResourceOrder sol = new ResourceOrder(instance);
		
		for(int j=0; j<instance.numJobs; j++){
			realisable.add(new Task(j,0));
		}
		
		int remainingTasks = instance.numJobs*instance.numMachines;
		int machine;
		while(remainingTasks>0){
			Task current = lrpt(realisable,instance);
			machine = instance.machine(current);
			sol.tasksByMachine[machine][sol.nextFreeSlot[machine]] = current;
			sol.nextFreeSlot[machine]++;
			if(current.task + 1 < instance.numMachines) {
			realisable.add(new Task(current.job, current.task + 1));
			}
			TasksDone.add(current);
			realisable.remove(current);
			remainingTasks --;
		}
		return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
		}
}

 

package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.TabooSolver.sTabou;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class GreedySolverEST_SPT implements Solver {
	
	ArrayList<Task> TasksDone = new ArrayList<Task>();
	
	private Task spt(Vector<Task> tasks, Instance instance){
		Vector<Task> earliest_tasks = earliest_tasks(tasks, instance);
		Task current = earliest_tasks.firstElement();
		Task best = current;
		for(int j=1; j<earliest_tasks.size(); j++) {
			current = earliest_tasks.get(j);
			if(instance.duration(current) < instance.duration(best)) {
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
	
	private int start_time(Task task, Instance instance) {
		int time=0;
		int longest_time = 0;
		int final_time = 0;
		int t=0;
		for(int i=0; i<task.task; i++) {
			time = time+instance.duration(task.job, i);
		}
	 	for(int j=0; j < instance.numJobs; j++) {
	 		longest_time = 0;
			while(instance.machine(j, t) != instance.machine(task.job, task.task) && t < (instance.numTasks-1)) {
				longest_time = longest_time + instance.duration(j,t);
				t++;
			}
			if(longest_time < time) {
				if(TaskAlreadyDone(t,j, instance)) {
					if((longest_time+instance.duration(j,t)) > (time+instance.duration(task.job, task.task))){
						if(longest_time>final_time) final_time = longest_time = longest_time + instance.duration(j,t);
					}
				}
			}

		}
		return final_time;
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
			Task current = spt(realisable,instance);
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

 

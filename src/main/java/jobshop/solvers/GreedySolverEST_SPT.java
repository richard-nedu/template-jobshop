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
import java.util.Scanner;
import java.util.Vector;

public class GreedySolverEST_SPT implements Solver {
    
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
        
       /* for(int job = 0 ; job< instance.numJobs ; job++) {
            for(int task = 0 ; task < instance.numTasks ; task++) {
                if(tasksAchieved[job][task] != 1 ) {
                    if(task != t.task && job != t.job) {
                    	startTimes[job][task] = 0;
                    }
                }
            }
        } */
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
			Task current = spt(realisable,instance);
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

 

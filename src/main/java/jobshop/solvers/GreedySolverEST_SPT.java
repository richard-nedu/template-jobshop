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
	int[][] endTimes;
	boolean[] machines;
	
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
		apply_time(best, instance);
		return(best);
			
		}
	
	
	private int start_time(Task t, Instance instance) {
		if(machines[instance.machine(t)]==false) return 0;
		int time = 0;
		for(int job = 0; job < instance.numJobs; job++) {
			for(int task = 0; task < instance.numTasks; task++) {
				if(instance.machine(t)==instance.machine(job, task)) {
					if(endTimes[job][task] > time) {
						time = endTimes[job][task];	
					}
				}
			}
		}
		return time;
	}
		
	
	private void apply_time(Task t, Instance instance) {
		int time = 0;
		int previous_task[] = new int[2];
		for(int job = 0; job < instance.numJobs; job++) {
			for(int task = 0; task < instance.numTasks; task++) {
				if(instance.machine(t)==instance.machine(job, task)) {
					if (endTimes[job][task] > time) {
						time = endTimes[job][task];
						previous_task[0] = job;
						previous_task[1] = task;
					}
				}
			}
		}
		machines[instance.machine(t)]=true;
		if(time==0) endTimes[t.job][t.task] = instance.duration(t);
		else {
			endTimes[t.job][t.task] = time;
		}
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
	   	endTimes = new int[instance.numJobs][instance.numTasks];
	   	machines = new boolean[instance.numMachines];
	   	
        for(int m=0; m < instance.numMachines; m++)
        {        	
        	machines[m]=false;
            for(int j=0; j<instance.numJobs; j++) endTimes[m][j]=0;
        }
        
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

 

package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Vector;

public class GreedySolverSPT implements Solver {
		
	private Task spt(Vector<Task> task, Instance instance){
		Task current = task.firstElement();
		Task best = current;
		for(int j=1; j<task.size(); j++) {
			current = task.get(j);
			if(instance.duration(current) < instance.duration(best)) {
				best = current;
			}
		}
		return(best);
			
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

 

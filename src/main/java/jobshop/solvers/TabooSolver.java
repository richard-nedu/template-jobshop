package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.GreedySolverLRPT;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class TabooSolver implements Solver {
    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block { 
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
        	Task task;
        	task = order.tasksByMachine[machine][t1];
        	order.tasksByMachine[machine][t1] = order.tasksByMachine[machine][t2];
        	order.tasksByMachine[machine][t2] = task;        }
    }
    

    
    static class sTabou {
        int[][] visited_tabou;
        Instance instance;
        sTabou(Instance instance) {
        	 this.visited_tabou = new int[instance.numMachines][instance.numJobs];
        	 this.instance = instance;
             for(int m=0; m < instance.numMachines; m++)
             {
                 for(int j=0; j<instance.numJobs; j++) visited_tabou[m][j]=0;
             }
        }
    }
    

    @Override
    public Result solve(Instance instance, long deadline) {
    	GreedySolverLRPT solver_init = new GreedySolverLRPT();
    	Result result_init = solver_init.solve(instance, deadline);

    	sTabou ListTabou = new sTabou(instance);
		Schedule best_solution = result_init.schedule;
		Schedule best_tabou = result_init.schedule;
   	 	int [] coord_tabou = new int [2];
		ResourceOrder best_sol_order = new ResourceOrder(best_solution);
		ResourceOrder candidate_order = new ResourceOrder(best_solution);
		int best_time = best_solution.makespan();
		int best_tabou_time = best_solution.makespan();
		int iterations =  1;
		int max_iter = 20;
		int dureeTabou = 3;
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis()-time<deadline && iterations<max_iter) {
			List<Block> block_list = blocksOfCriticalPath(best_sol_order);
			for (Block b : block_list) {
				List<Swap> swaps = neighbors(b);
				best_tabou = candidate_order.toSchedule();
				best_tabou_time = candidate_order.toSchedule().makespan();
				for(Swap s : swaps) {
					s.applyOn(candidate_order);
					if(best_tabou_time>candidate_order.toSchedule().makespan()) {
						if(ListTabou.visited_tabou[s.t1][s.t2]<iterations) {
						best_tabou = candidate_order.toSchedule();
						best_tabou_time = candidate_order.toSchedule().makespan();
						coord_tabou[0] = s.t1;
						coord_tabou[1] = s.t2;
						}
						else if(best_tabou_time<best_time) {
							best_solution = candidate_order.toSchedule();
							best_time = candidate_order.toSchedule().makespan();
							best_sol_order = candidate_order;
							best_tabou = candidate_order.toSchedule();
							best_tabou_time = candidate_order.toSchedule().makespan();
							coord_tabou[0] = s.t1;
							coord_tabou[1] = s.t2;
						}
					}

					ListTabou.visited_tabou[coord_tabou[0]][coord_tabou[1]]=dureeTabou+iterations;
				}
			}
			iterations++;
		}
		return new Result(instance, best_solution, Result.ExitCause.Blocked);

    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) { 
    	List<Task> critical_path = order.toSchedule().criticalPath();
        LinkedList<Block> blocks_list = new LinkedList<>();
        ArrayList<Task> visited = new ArrayList<Task>();
    	int path_index = 0;
    	int path_index_cpy = 0;
    	Task first_task;
		Task last_task = null;
		int index_first = 0;
		int index_last = 0;
		
    	for (Task t : critical_path) {
    		path_index++;
    		Task next_task = critical_path.get(path_index);
    		if(order.instance.machine(t.job,t.task)==order.instance.machine(next_task.job,next_task.task)) {
    			

    			if(!visited.contains(t)) {
	    			visited.add(t);
	    			first_task = t;
	    			path_index_cpy = path_index;
	    			while(order.instance.machine(t.job,t.task)==order.instance.machine(next_task.job,next_task.task)) {
	    				visited.add(next_task);
	    				path_index_cpy++;
	    				last_task = next_task;
	    				if (path_index_cpy>critical_path.size()) next_task = critical_path.get(path_index_cpy);
	    				else break;
	    			}	    			
	    			for(int m=0; m < order.instance.numMachines; m++)
	    	        {
	    	            for(int j=0; j<order.instance.numJobs; j++)
	    	            {
	    	                if(order.tasksByMachine[m][j].job == last_task.job && order.tasksByMachine[m][j].task == last_task.task) ; index_last = j;
	    	                if(order.tasksByMachine[m][j].job == first_task.job && order.tasksByMachine[m][j].task == first_task.task); index_last = j;
	    	            }
	    	        }
	    			Block new_block = new Block(order.instance.machine(t.job,t.task),index_first, index_last);
	    			blocks_list.add(new_block);
    			}
    			
    		}
    		if (path_index_cpy  >= critical_path.size()) break;
    	}
        if (blocks_list.isEmpty()) throw new UnsupportedOperationException();
    	return blocks_list;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) { 
    	LinkedList<Swap> neighbors = new LinkedList<>();
        if(block.lastTask-block.firstTask<=1) {
        	Swap perm = new Swap(block.machine, block.firstTask, block.lastTask);
    		neighbors.add(perm);
        } 
        else {
	        for(int i = block.firstTask ; i<=block.lastTask; i++) {
	        	System.out.println(i + " " + block.machine +" "+ block.firstTask +" "+ block.lastTask);
	        	if (i<block.lastTask) {
	        		Swap perm = new Swap(block.machine, i, i+1);
	        		neighbors.add(perm);
	        	}
	        	else {
	        		Swap perm = new Swap(block.machine, block.firstTask, block.lastTask);
	        		neighbors.add(perm);
	        	}
	        }
        }
        if (neighbors.isEmpty()) throw new UnsupportedOperationException();
        return neighbors;
    }

}

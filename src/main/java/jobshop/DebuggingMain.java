package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.GreedySolverLRPT;
import jobshop.solvers.GreedySolverSPT;
import jobshop.solvers.TabooSolver;
import jobshop.solvers.GreedySolverEST_SPT;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.GreedySolverEST_LRPT;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/ft06"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            
           /* JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1; */

        	GreedySolverLRPT solver_init = new GreedySolverLRPT();
        	Schedule sched = solver_init.solve(instance, 15).schedule;
        	TabooSolver taboo = new TabooSolver();
            System.out.println(taboo.solve(instance, 15).schedule);
           
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        

    }
}

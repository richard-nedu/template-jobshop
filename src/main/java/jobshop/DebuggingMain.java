package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.GreedySolverLRPT;
import jobshop.solvers.GreedySolverSPT;
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
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

           // System.out.println("\nENCODING: " + enc);

            Schedule sched = enc.toSchedule();
            // TODO: make it print something meaningful
            // by implementing the toString() method
            //System.out.println("SCHEDULE: " + sched);
            //System.out.println("VALID: " + sched.isValid());
            //System.out.println("MAKESPAN: " + sched.makespan());
            
            Result result;
            GreedySolverEST_SPT solver = new GreedySolverEST_SPT();  
            result = solver.solve(instance, 15);
            System.out.println(result.schedule.toString());
            
            ResourceOrder order = new ResourceOrder(sched);
            System.out.println(order.toString());
            
            DescentSolver newsolver = new DescentSolver();            

            Result result2 = newsolver.solve(instance, 15);
            Schedule sched2 = result2.schedule;
            ResourceOrder order2 = new ResourceOrder(sched2);
            System.out.println(order2);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        

    }
}

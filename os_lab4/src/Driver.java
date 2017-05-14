import java.io.File;
import java.util.Scanner;

/**
 * Created by MonaBest on 4/21/17.
 */
public class Driver {
    public static void main(String args[]) throws Exception {

        Scanner randomGenerator = new Scanner(new File("./src/random-numbers.txt"));

        int machineSize = Integer.parseInt(args[0]);
        int pageSize = Integer.parseInt(args[1]);
        int processSize = Integer.parseInt(args[2]);
        int jobMix = Integer.parseInt(args[3]);
        int numberOfReferences = Integer.parseInt(args[4]);
        String algorithm = args[5];
        int outputMode = Integer.parseInt(args[6]);

        System.out.println("The machine size is " + machineSize + ".");
        System.out.println("The page size is " + pageSize + ".");
        System.out.println("The process size is " + processSize + ".");
        System.out.println("The job mix number is " + jobMix + ".");
        System.out.println("The number of references per process is " + numberOfReferences + ".");
        System.out.println("The replacement algorithm is " + algorithm + "." + "\n");
        System.out.println("The level of debugging output is " + outputMode);

        Paging pageJob = new Paging(machineSize, pageSize, processSize, jobMix, numberOfReferences, algorithm, randomGenerator,outputMode);
        pageJob.run();
        pageJob.printSummary();

    }
}
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;

/**
 * The driver class will process the input and initialize four scheduler with different algorithms
 *
 * Created by MonaBest on 2/23/17.
 */
public class Driver {

    public static void main(String[] args) throws Exception {
        boolean verbose = false;
        //get the instruction from the command line
        Scanner command = new Scanner(System.in);
        String instruction = command.nextLine();
        if(instruction.indexOf("verbose") != -1){
            verbose = true;
        }

        String inputString = "";
        if(verbose == true) {
            String[] ins = instruction.split("\\s+");
            inputString = ins[1];
        }
        else{
            inputString = instruction.trim();
        }

        //process the input process file
        File file = new File(inputString);
        Scanner input = new Scanner(file);
        int num = input.nextInt();
        String data = input.useDelimiter("\\Z").next();
        data = data.replaceAll("[()]", ",");
        data = data.replaceAll("[\\D]", " ");
        data = data.trim();
        String[] e = data.split("\\s+");

        //store the process data
        ArrayList<Process> processList = new ArrayList<>();
        for (int i = 0; i < e.length; i += 4) {
            int a = Integer.parseInt(e[i]);
            int b = Integer.parseInt(e[i + 1]);
            int c = Integer.parseInt(e[i + 2]);
            int m = Integer.parseInt(e[i + 3]);
            processList.add(new Process(a, b, c, m,i));
        }

        ArrayList<Process> processList_pos = new ArrayList<>(processList);
        //sort the data
        sort(processList_pos);


        //create four scheduler for each algorithm
        Scheduler sch1 = new Scheduler(new ArrayList<Process>(processList_pos), "FCFS", verbose);
        sch1.printMeta(sch1.algoName, new ArrayList<Process>(processList), new ArrayList<Process>(processList_pos));
        sch1.FCFSstart();
        System.out.println("The scheduling algorithm used was First Come First Served ");
        sch1.printProcess();
        sch1.summary();
        //initialize the list every time
        init(processList);
        init(processList_pos);


        Scheduler sch2 = new Scheduler(new ArrayList<Process>(processList_pos), "RR", verbose);
        sch2.printMeta(sch2.algoName, new ArrayList<Process>(processList), new ArrayList<Process>(processList_pos));
        sch2.RRstart();
        System.out.println("The scheduling algorithm used was Round Robbin ");
        sch2.printProcess();
        sch2.summary();
        boolean exexu3 = true;

        //initialize the list every time
        init(processList);
        init(processList_pos);



        Scheduler sch3 = new Scheduler(new ArrayList<Process>(processList_pos), "Uniprogrammed", verbose);
        sch3.printMeta(sch3.algoName, new ArrayList<Process>(processList), new ArrayList<Process>(processList_pos));
        sch3.uniprogrammedstart();
        System.out.println("The scheduling algorithm used was Uniprocessing ");
        sch3.printProcess();
        sch3.summary();
        //initialize the list every time
        init(processList);
        init(processList_pos);

        Scheduler sch4 = new Scheduler(new ArrayList<Process>(processList_pos), "SJF", verbose);
        sch4.printMeta(sch4.algoName, new ArrayList<Process>(processList), new ArrayList<Process>(processList_pos));
        sch4.SJFstart();
        System.out.println("The scheduling algorithm used was Shortest Job First ");
        sch4.printProcess();
        sch4.summary();

    }

        //sort the process list based on arrival time
        public static void sort(ArrayList<Process> processList){
            Collections.sort(processList, new Comparator<Process>(){
                public int compare(Process a, Process b){
                    int aTime = a.arrival;
                    int bTime = b.arrival;
                    if(aTime > bTime){
                        return 1;
                    }
                    else if (aTime < bTime){
                        return -1;
                    }
                    else{
                        return 0;
                    }
                }
            });
        }

        public static void init(ArrayList<Process> list){
            for(Process p: list){
                p.initlize();
            }
        }
}

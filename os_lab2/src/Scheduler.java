import java.io.*;
import java.util.*;
import java.math.*;
/**
 * Scheduler class contains 4 scheduling algorithms.
 *
 * For each process:
 * A is arrival time
 * B is used for generating CPU bursts
 * C is total time CPU needed : UDRIs randomOs(U)
 * M is used for calculating the I/O bursts
 *
 * Created by MonaBest on 2/22/17.
 */
public class Scheduler {
    private Queue<Integer> randomQueue;
    private ArrayList<Process> processList;
    String algoName;
    final int Q = 2; //RR, q = 2
    private boolean verbose;
    private int time = -1; // start time
    private int ioUse = 0;


    public Scheduler(ArrayList<Process> processList, String name, boolean verbose) throws IOException {
        this.processList = processList;
        getRandomQueue();
        this.algoName = name;
        this.verbose = verbose;
    }

    //scanner the random number file
    public void getRandomQueue() throws IOException{
        //process the random number file
        File randomFile = new File("random-numbers.txt");
        Scanner randomInput = new Scanner(randomFile);
        randomQueue = new LinkedList<>();
        while (randomInput.hasNext()) {
            randomQueue.offer(randomInput.nextInt());
        }
    }

    public int getRandom(){
        return randomQueue.poll();
    }


    public int randomOS(int u, int num){
        return 1 + (num % u);
    }


    public void printProcess(){
        for(int i = 0; i < processList.size(); i++){
            System.out.println("\n" + "Process " + i + ":");
            System.out.println(processList.get(i).info());
        }
    }





    private Queue<Process> runningQueue = new LinkedList<>();
    private Queue<Process> readyQueue = new LinkedList<>();
   // ArrayList<Process> blockedList = new ArrayList<>();



    //FCFS algorithm
    /*--------------------------------------------------------------------*/
    public void FCFSstart() {

        int expired = 0; //number of terminated processes
        while (expired != processList.size()) {
            time++;

            if(runningQueue.isEmpty()){
                if(!readyQueue.isEmpty()){
                    Process p = readyQueue.poll();
                    runningQueue.offer(p);
                    p.status = 2;
                }
            }

            if(!runningQueue.isEmpty()) {
                for (Process p : runningQueue) {
                    if (p.runningRemain == 0) {
                        int random = getRandom();
                        int u = p.interval;
                        int cpuBurst = randomOS(u, random);
                        //if the value returned by randomOS is larger than total CPU remaining, then set to remaining time
                        if (cpuBurst > p.cpuRemain) {
                            cpuBurst = p.cpuRemain;
                        }
                        p.runningRemain = cpuBurst;
                        int ioBurst = cpuBurst * p.m;
                        p.blockRemain = ioBurst;
                        if(verbose) {
                            System.out.println("First burst when choosing ready process to run " + random);
                        }
                    }
                }
            }

            StringBuilder cycle = new StringBuilder();
            cycle.append("Before cycle" + "    " + time + ":  ");
            for (int i = 0; i < processList.size(); i++) {
                cycle.append(processList.get(i).getStatus());
            }
            cycle.append(".");

            /* now begin the cycle */

            //do the blocked processes
            if(!nonOneBlock()){
                ioUse ++;
            }
            for(Process p: processList){
                if(p.status == 3){
                    p.blockRemain--;
                    p.ioTime++;
                    if (p.blockRemain == 0) {
                        p.status = 1;    //status: 0 - unstarted; 1 - ready; 2 -running; 3 - blocked; 4 - finished
                        readyQueue.add(p);
                    }

                }
            }


            // do the running processes
           if(!runningQueue.isEmpty()) {
               for(Process p: runningQueue) {
                   p.runningRemain--;
                   p.cpuRemain--;
                   //if cpu burst is used up
                   if (p.runningRemain == 0) {
                       runningQueue.remove(p);
                       //the process is finished after this cpu burst
                       if (p.cpuRemain == 0) {
                           expired++;
                           p.status = 4;
                           //set the finish time
                           p.finishTime = time;
                       }
                       //still have cpuRemain time, move to the blocked list.
                       else {
                           p.status = 3;
                       }
                   }
               }
           }


            //do the arriving processes
            for (Process p : processList) {
                if (p.status == 0 && p.arrival == time) {
                    p.status = 1;
                    readyQueue.offer(p);
                }
            }

            //do ready processes
            if (!readyQueue.isEmpty()) {
                for (Process p : readyQueue) {
                    p.waitTime++;
                }
            }

            if(verbose) {
                System.out.println(cycle.toString());
            }

        }
    }




    //RR algorithm
   /*--------------------------------------------------------------------*/
/*	class TmpComparator<Process> implements Comparator<Process>{
        @Override
	public int compare(Process o1, Process o2){
               if(o1.arrival > o2.arrival){
                    return 1;
                }
                else if(o1.arrival < o2.arrival){
                    return -1;
                }
                else{
                    return o1.getInputOrder() - o2.getInputOrder();
                }
}
}
*/

   //for the temporary storage.
    PriorityQueue<Process> readyQueueTmp = new PriorityQueue<Process>(100, new Comparator<Process>(){
	 @Override
        public int compare(Process o1, Process o2){
               if(o1.arrival > o2.arrival){
                    return 1;
                }
                else if(o1.arrival < o2.arrival){
                    return -1;
                }
                else{
                    return o1.getInputOrder() - o2.getInputOrder();
                }
}
});
 
    public void RRstart() {
        int expired = 0; //number of terminated processes
        while (expired != processList.size()) {
            time++;

            if (runningQueue.isEmpty()) {
                if (!readyQueue.isEmpty()) {
                    Process p = readyQueue.poll();
                    runningQueue.offer(p);
                    p.status = 2;
                }
            }

            if (!runningQueue.isEmpty()) {
                for (Process p : runningQueue) {
                    if (p.runningRemain == 0) {
                        int random = getRandom();
                        int u = p.interval;
                        int cpuBurst = randomOS(u, random);
                        //if the value returned by randomOS is larger than total CPU remaining, then set to remaining time
                        if (cpuBurst > p.cpuRemain) {
                            cpuBurst = p.cpuRemain;
                        }
                        p.runningRemain = cpuBurst;
                        int ioBurst = cpuBurst * p.m;
                        p.blockRemain = ioBurst;
    
                   
                        if(verbose){
                            System.out.println("First burst when choosing ready process to run " + random);
                        }
                    }
                }
            }

            StringBuilder cycle = new StringBuilder();
            cycle.append("Before cycle" + "    " + time + ":  ");
            for (int i = 0; i < processList.size(); i++) {
                cycle.append(processList.get(i).getStatus());
            }
            cycle.append(".");

            /* now begin the cycle */


            //do the blocked processes
            if(!nonOneBlock()){
                ioUse ++;
            }
            for(Process p: processList){
                if(p.status == 3){
                    p.blockRemain--;
                    p.ioTime++;
                    if (p.blockRemain == 0) {
                        p.status = 1;    //status: 0 - unstarted; 1 - ready; 2 -running; 3 - blocked; 4 - finished
                        readyQueueTmp.offer(p);
                    }
                }
            }


            // do the running processes
            if (!runningQueue.isEmpty()) {
                for (Process p : runningQueue) {
                    if (p.runningRemain > 0) {
                        p.runningRemain--;
                        p.cpuRemain--;
                    }
                    if (p.quantum < 2) {
                        p.quantum++;
                    }
                    //a running process's event order: terminate - block - preempted
                    //if cpuBurst is used up
                    if (p.runningRemain == 0) {
                        runningQueue.remove(p);
                        //the process is finished after this cpu burst
                        if (p.cpuRemain == 0) {
                            expired++;
                            p.status = 4;
                            //set the finish time
                            p.finishTime = time;
                            p.quantum = 0;//clear the quantum

                        }
                        //still have cpuRemain time, move to the blocked list.
                        else {
                            p.status = 3;
                            p.quantum = 0; //clear the quantum
                        }
                        continue;
                    }
                    //if have run maximum quantum. it is preempted
                    if (p.status == 2 && p.quantum == Q) {
                        runningQueue.remove(p);
                        readyQueueTmp.offer(p);
                        //
                        p.status = 1;
                        p.quantum = 0;//clear the quantum
                    }

                }
            }

            //do the arriving processes
            for (Process p : processList) {
                if (p.status == 0 && p.arrival == time) {
                    p.status = 1;
                    readyQueueTmp.offer(p);
                }

            }


            //do ready processes
            if (!readyQueue.isEmpty()) {
                for (Process p : readyQueue) {
                    p.waitTime++;
                }
            }


            while(!readyQueueTmp.isEmpty()){
                readyQueue.offer(readyQueueTmp.poll());
            }

            if(verbose) {
                System.out.println(cycle.toString());
            }

        }
    }



    //uniprogrammed algorithm
    /*--------------------------------------------------------------------*/


    public boolean noOneRunning(){
        for(Process p : processList){
            if(p.status == 2){
                return false;
            }
        }
        return true;
    }
    public boolean nonOneBlock(){
        for(Process p : processList){
            if(p.status == 3){
                return false;
            }
        }
        return true;
    }


    public void uniprogrammedstart() {
        int expired = 0; //number of terminated processes
        while (expired != processList.size()) {
            time++;
            if (runningQueue.isEmpty()) {
                if (!readyQueue.isEmpty()) {
                    Process p = readyQueue.poll();
                    runningQueue.offer(p);
                    p.status = 2;
                }
            }

            if(!runningQueue.isEmpty()) {
                for (Process p : runningQueue) {
                    if (p.runningRemain == 0) {
                        int random = getRandom();
                        int u = p.interval;
                        int cpuBurst = randomOS(u, random);
                        //if the value returned by randomOS is larger than total CPU remaining, then set to remaining time
                        if (cpuBurst > p.cpuRemain) {
                            cpuBurst = p.cpuRemain;
                        }
                        p.runningRemain = cpuBurst;
                        int ioBurst = cpuBurst * p.m;
                        p.blockRemain = ioBurst;
                        p.status = 2;
                        if(verbose) {
                            System.out.println("First burst when choosing ready process to run " + random);
                        }
                    }
                }
            }

            StringBuilder cycle = new StringBuilder();
            cycle.append("Before cycle" + "    " + time + ":  ");
            for (int i = 0; i < processList.size(); i++) {
                cycle.append(processList.get(i).getStatus());
            }
            cycle.append(".");

            /* now begin the cycle */

            //do the blocked processes
            if(!nonOneBlock()){
                ioUse ++;
            }
            for(Process p: processList){
                if(p.status == 3){
                    p.blockRemain--;
                    p.ioTime++;
                    if (p.blockRemain == 0) {
                        p.status = 1;    //status: 0 - unstarted; 1 - ready; 2 -running; 3 - blocked; 4 - finished
                        readyQueue.offer(p);
                    }
                }
            }

            // do the running processes
            if(!runningQueue.isEmpty()) {
                for(Process p: runningQueue) {
                    p.runningRemain--;
                    p.cpuRemain--;
                    //if cpu burst is used up
                    if (p.runningRemain == 0) {
                        runningQueue.remove(p);
                        //the process is finished after this cpu burst
                        if (p.cpuRemain == 0) {
                            expired++;
                            p.status = 4;
                            //set the finish time
                            p.finishTime = time;

                        }
                        //still have cpuRemain time, move to the blocked list.
                        else {
                            p.status = 3;
                        }
                    }
                }
            }

            //do the arriving processes
            for (Process p : processList) {
                if (p.status == 0 && p.arrival == time) {
                    p.status = 1;
                    if(runningQueue.isEmpty() && nonOneBlock()&& readyQueue.isEmpty()) {
                        readyQueue.offer(p);
                    }
                }
                if(runningQueue.isEmpty() && nonOneBlock() && p.status == 1 && readyQueue.isEmpty()) {
                    readyQueue.offer(p);
                }

            }


            //do ready processes
            if (!readyQueue.isEmpty()) {
                for (Process p : readyQueue) {
                    p.waitTime++;
                }
            }

            if(verbose) {
                System.out.println(cycle.toString());
            }

        }
    }



    //Shortest Job First
    /*--------------------------------------------------------------------*/

    PriorityQueue<Process> readyQueueSJF = new PriorityQueue<>(100, new Comparator<Process>() {
        @Override
        public int compare(Process o1, Process o2) {
            if(o1.cpuRemain < o2.cpuRemain){
                return -1;
            }
            else if (o1.cpuRemain > o2.cpuRemain){
                return 1;
            }
            else{
                if(o1.arrival > o2.arrival){
                    return 1;
                }
                else if(o1.arrival < o2.arrival){
                    return -1;
                }
                else{
                    return o1.getInputOrder() - o2.getInputOrder();
                }
            }
        }
    });

    public void SJFstart() {
        int expired = 0; //number of terminated processes
        while (expired != processList.size()) {
            time++;
            if(runningQueue.isEmpty()){
                if(!readyQueueSJF.isEmpty()){
                    Process p = readyQueueSJF.poll();
                    runningQueue.offer(p);
                    p.status = 2;
                }
            }

            if(!runningQueue.isEmpty()) {
                for (Process p : runningQueue) {
                    if (p.runningRemain == 0) {
                        int random = getRandom();
                        int u = p.interval;
                        int cpuBurst = randomOS(u, random);
                        //if the value returned by randomOS is larger than total CPU remaining, then set to remaining time
                        if (cpuBurst > p.cpuRemain) {
                            cpuBurst = p.cpuRemain;
                        }
                        p.runningRemain = cpuBurst;
                        int ioBurst = cpuBurst * p.m;
                        p.blockRemain = ioBurst;
                        if(verbose) {
                            System.out.println("First burst when choosing ready process to run " + random);
                        }
                    }
                }
            }

            StringBuilder cycle = new StringBuilder();
            cycle.append("Before cycle" + "    " + time + ":  ");
            for (int i = 0; i < processList.size(); i++) {
                cycle.append(processList.get(i).getStatus());
            }
            cycle.append(".");

            /* now begin the cycle */

            //do the blocked processes
            if(!nonOneBlock()){
                ioUse ++;
            }
            for(Process p: processList){
                if(p.status == 3){
                    p.blockRemain--;
                    p.ioTime++;
                    if (p.blockRemain == 0) {
                        p.status = 1;    //status: 0 - unstarted; 1 - ready; 2 -running; 3 - blocked; 4 - finished
                        readyQueueSJF.add(p);
                    }

                }
            }


            // do the running processes
            if(!runningQueue.isEmpty()) {
                for(Process p: runningQueue) {
                    p.runningRemain--;
                    p.cpuRemain--;
                    //if cpu burst is used up
                    if (p.runningRemain == 0) {
                        runningQueue.remove(p);
                        //the process is finished after this cpu burst
                        if (p.cpuRemain == 0) {
                            expired++;
                            p.status = 4;
                            //set the finish time
                            p.finishTime = time;
                        }
                        //still have cpuRemain time, move to the blocked list.
                        else {
                            p.status = 3;
                        }
                    }
                }
            }


            //do the arriving processes
            for (Process p : processList) {
                if (p.status == 0 && p.arrival == time) {
                    p.status = 1;
                    readyQueueSJF.offer(p);
                }
            }

            //do ready processes
            if (!readyQueueSJF.isEmpty()) {
                for (Process p : readyQueueSJF) {
                    p.waitTime++;
                }
            }

            if(verbose) {
                System.out.println(cycle.toString());
            }

        }
    }



    //meta process data
    /*--------------------------------------------------------------------*/
    public void printMeta(String algoName, ArrayList<Process> processList, ArrayList<Process> processList_pos){
        //Before sort
        System.out.print("The original input was: " + processList.size() + " ");
        for(int i = 0; i < processList.size(); i++){
            System.out.print(processList.get(i).numberInfo());
        }

        System.out.print("\n" + "The (sorted) input is: " + processList_pos.size() + " ");

        //after sort
        for(int i = 0; i < processList_pos.size(); i++){
            System.out.print(processList_pos.get(i).numberInfo());
        }
        System.out.println("\n");
    }


    //summary data
    /*--------------------------------------------------------------------*/
    public void summary(){
        int number = processList.size();
        StringBuilder sb = new StringBuilder();
        int cpuUse = 0;
        int turnaround = 0;
        int waiting = 0;
        for(Process p : processList){
            cpuUse = cpuUse + p.total;
            waiting = waiting + p.getWaitTime();
            turnaround = turnaround + p.getTurnaroundTime();
        }
        double cpuPercent = rounding(cpuUse * 1.0 / time);
        double ioPercent = rounding(ioUse * 1.0 / time);

        double throughput = rounding(number * 1.0/ time * 100);
        double avgTurnaround = rounding(turnaround * 1.0 /number);
        double avgWaiting = rounding(waiting * 1.0 /number);



        sb.append("Summary Data: ");
        sb.append("\n");
        sb.append("\t\t" + "Finishing time: " + time + "\n");
        sb.append("\t\t" + "CPU Utilization: " + String.format("%.6f",cpuPercent) + "\n");
        sb.append("\t\t" + "I/O Utilization: " + String.format("%.6f",ioPercent) + "\n");
        sb.append("\t\t" + "Throughput: " + String.format("%.6f",throughput) + " processes per hundred cycles\n");
        sb.append("\t\t" + "Average turnaround time: " + String.format("%.6f",avgTurnaround) + "\n");
        sb.append("\t\t" + "Average waiting time: " + String.format("%.6f",avgWaiting) +  "\n");

        System.out.println("\n"+sb.toString());
    }


    public double rounding(double f){
        BigDecimal b =  new  BigDecimal(f);
        double f1 = b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class is PessimisticBanker class which simulates the banker algorithm.
 *
 * Created by MonaBest on 4/4/17.
 */
public class PessimisticBanker {
    int numTasks;
    int numResources;
    HashMap<Integer, Integer> rMap;
    Task[] tlist;

    /**
     * This is the constructor for initiating the object.
     *
     * @param numTasks
     * @param numResources
     * @param resourceMap
     * @param tasklist
     */
    public PessimisticBanker(int numTasks, int numResources, HashMap<Integer, Integer> resourceMap, Task[] tasklist){
        this.numTasks = numTasks;
        this.numResources = numResources;
        rMap = resourceMap;
        tlist = tasklist;
    }

    /**
     * This run method processes the activities of each task.
     */
    public void run(){
        int cycle = 0;
        ArrayList<Task> blockedTask = new ArrayList<>();
        int terminated = 0; //number of tasks that are terminated.
        int blocked = 0;    //number of tasks that are blocked.
        int aborted = 0;    //number of tasks that are aborted.


        while(terminated + aborted!= numTasks && cycle !=30) {

            //if the resource is released at this cycle, only be available at next cycle.
            int[] releasedResource = new int[numResources];

            //it takes 'numResources' cycles to initiate the claims.
            if(cycle < numResources){
                System.out.println("During " + cycle + "-" + (cycle + 1) + " each task completes its initiate");
                for(int i = 0; i < numTasks; i++){
                    int claimType = tlist[i].activities.peek().num1;
                    int claimAmount = tlist[i].activities.peek().num2;
                    tlist[i].activities.poll();  //poll out of all resources initiate.
                    tlist[i].claims[claimType-1] = claimAmount;

                    //if claim exceeds the amount of resource the system has, then abort the process
                    if(claimAmount > rMap.get(claimType)){
                        tlist[i].status = "aborted";
                        aborted++;
                        System.out.println("\tTask " + tlist[i].taskNo +  " is aborted (claim exceeds total in system)");
                    }
                }
            }
            else{
                System.out.println("\n" + "During " + cycle + "-" + (cycle + 1));

                //first check blocked list if there is any blocked list will be available.
                Queue<Task> taskTobeRemoved = new LinkedList<>();
                ArrayList<Integer> hasProcessed = new ArrayList<>();

                //first check the blocked list to see if any could be satisfied.
                if(blockedTask.size() > 0){
                    for(int i = 0; i< blockedTask.size(); i++){
                        Task bTask = blockedTask.get(i);

                        int requestType = bTask.needing.type;
                        int requestAmount = bTask.needing.amount;
                        //if grant this task's request, the status is safe, then grant it.
                        if(checkSafe(bTask)){
                            taskTobeRemoved.add(bTask);
                            bTask.status = " ";

                            int index = requestType - 1;
                            int nowHas = bTask.holding[index] + requestAmount;
                            bTask.holding[index] = nowHas;

                            System.out.println("\t" + "Task " + bTask.taskNo + " completes its request (i.e., the request is granted).");
                            taskTobeRemoved.add(bTask);

                            int now =  rMap.get(requestType) - requestAmount;
                            rMap.put(requestType, now);
                            bTask.needing = new Resource(0,0);  //reset to 0,0

                            hasProcessed.add(bTask.taskNo);
                            bTask.activities.poll(); //after granted, then poll out this activity.
                        }
                    }
                }

                blocked = blocked - taskTobeRemoved.size();
                for(Task t: taskTobeRemoved){
                    blockedTask.remove(t);
                }

                //print blocked task still can't be granted
                for(Task t : blockedTask){
                    System.out.println("\tTask " + t.taskNo + "'s request can't be granted. (i.e., is still blocked)");
                }


                // start loop for each task
                for(int i = 0; i < numTasks; i++){
                    //skip the task that has been processed at the blocked list check
                    if(hasProcessed.contains(i+1)){
                        continue;
                    }
                    //current task
                    Task curTask = tlist[i];
                    if(curTask.computeRemain > 0 ){
                        curTask.computeRemain --;
                        System.out.println("\tTask " + curTask.taskNo + " computes (" + (curTask.computeTime - curTask.computeRemain) + " of " + curTask.computeTime + " cycles)");
                        continue;
                    }
                    if(curTask.delayed > 1 ){
                        curTask.delayed --;
                        if(curTask.delayed == 1){
                            boolean end = curTask.activities.size() == 1 ? true : false;
                            if (!end) {
                                System.out.println("\tTask " + curTask.taskNo + " delayed " + curTask.delayed);
                            } else {
                                terminated++;
                                curTask.finishTime = cycle+1;
                                curTask.status = "terminated";
                                System.out.println("\tTask " + curTask.taskNo + " delayed " + curTask.delayed + " and terminates at " + (cycle + 1) + ".");
                            }
                        }
                        else {
                            System.out.println("\tTask " + curTask.taskNo + " delayed " + curTask.delayed);
                        }
                        continue;
                    }

                    if (!curTask.status.equals("terminated") && !curTask.status.equals("blocked") && !curTask.status.equals("aborted")) {
                        Queue<Activity> activityQueue = curTask.activities;

                        if (!activityQueue.isEmpty()) {
                            Activity a = activityQueue.peek();

                            if (a.name.equals("request")) {
                                int requestType = a.num1;
                                int requestAmount = a.num2;
                                curTask.needing.type = requestType;
                                curTask.needing.amount = requestAmount;

                               // if request exceeds the initial claims, then abort the process.
                                int currentHolding = curTask.holding[requestType-1];
                                if(curTask.claims[requestType-1] < (requestAmount + currentHolding)){
                                    tlist[i].status = "aborted";
                                    System.out.println("\tTask " + tlist[i].taskNo +  " is aborted (request exceeds its claim (current have + request)");
                                    aborted ++;
                                    //aborted process's resources are available next cycle.
                                    for(int m = 0; m < numResources; m ++){
                                        releasedResource[m] += tlist[i].holding[m];
                                    }
                                    continue;
                                }

                                //check if grant current task's request then state is safe or not.
                                if (checkSafe(curTask)) {
                                    //if check it is safe status then grant it (and update rMap)
                                    int remain = rMap.get(requestType) - requestAmount;
                                    rMap.put(requestType, remain);
                                    curTask.needing = new Resource(0, 0);  //reset to 0

                                    int index = requestType - 1;
                                    int nowHas = curTask.holding[index] + requestAmount;
                                    curTask.holding[index] = nowHas;


                                    activityQueue.poll(); //after granted, then poll out this activity.
                                    System.out.println("\t" + "Task " + curTask.taskNo + " completes its request (i.e., the request is granted).");
                                    continue;   //finish current task, continue this numTask loop.
                                } else {
                                    blocked++;
                                    curTask.status = "blocked";
                                    blockedTask.add(curTask);
                                    System.out.println("\t" + "Task " + curTask.taskNo + "'s request can't be granted. (i.e., task " + curTask.taskNo + " is blocked). ");
                                }
                            }
                            else if (a.name.equals("release")) {
                                int releaseType = a.num1;
                                int releaseAmount = a.num2;
                                releasedResource[releaseType-1] += releaseAmount;
                                curTask.holding[releaseType - 1] -= releaseAmount;  //release.

                                activityQueue.poll();
                                boolean end = activityQueue.size() == 1 ? true : false;

                                if (!end) {
                                    System.out.println("\t" + "Task " + curTask.taskNo + " releases " + releaseAmount + " unit (available at " + (cycle + 1) + ").");
                                } else {
                                    terminated++;
                                    curTask.finishTime = cycle+1;
                                    curTask.status = "terminated";
                                    System.out.println("\t" + "Task " + curTask.taskNo + " releases " + releaseAmount + " unit (available at " + (cycle + 1) + ") and terminates at " + (cycle + 1) + ".");

                                }
                            }
                            else if (a.name.equals("compute")){
                                if(noResources(curTask)){
                                    curTask.delayed  = a.num1;
                                    activityQueue.poll();
                                    if(a.num1 == 1){
                                        boolean end = activityQueue.size() == 1 ? true : false;
                                        if (!end) {
                                            System.out.println("\tTask " + curTask.taskNo + " delayed " + curTask.delayed);
                                        } else {
                                            terminated++;
                                            curTask.finishTime = cycle+1;
                                            curTask.status = "terminated";
                                            System.out.println("\tTask " + curTask.taskNo + " delayed " + curTask.delayed + "and terminates at " + (cycle + 1) + ".");
                                        }
                                    }
                                    else {
                                        System.out.println("\tTask " + curTask.taskNo + " delayed " + curTask.delayed);
                                    }
                                    continue;
                                }
                                tlist[i].computeTime = a.num1;
                                tlist[i].computeRemain = a.num1;
                                tlist[i].computeRemain --;
                                activityQueue.poll();

                                boolean end = activityQueue.size() == 1 ? true : false;
                                if (!end) {
                                    System.out.println("\tTask " + curTask.taskNo + " computes (1 of " + a.num1 + " cycles)");
                                } else {
                                    terminated++;
                                    curTask.finishTime = cycle+1;
                                    curTask.status = "terminated";
                                    System.out.println("\tTask " + curTask.taskNo + " computes (1 of " + a.num1 + " cycles)" +  " and terminates at " + (cycle + 1) + ".");

                                }
                            }
                            else if (a.name.equals("terminate")) {
                                activityQueue.poll();
                            }

                        }

                    }
                }

            }

            //released resource used for next cycle
            for(int i = 0; i < numResources; i++){
                rMap.put(i+1, rMap.get(i+1) + releasedResource[i]);
            }
            cycle++;
        }
    }

    /**
     * This method checks the state is safe or not after pretend to grant this task's request
     * @param t : task
     * @return boolean : false for not safe.
     */
        public boolean checkSafe(Task t){
            boolean isSafe = false;
            Activity a = t.activities.peek();
            //if current resource couldn't satisfy this process. then this process is blocked.
            int requestType = a.num1;
            int requestAmount = a.num2;

            //if current resource could satisfy this process, then pretend to grant it to see if the status is safe. if not then don't grant
            int[] tmpResource = new int[numResources];
            for (int i = 0; i < numResources; i++) {
                tmpResource[i] = rMap.get(i + 1);
            }

            tmpResource[requestType-1] -= requestAmount;
            t.holding[requestType-1] += requestAmount;

            //1. if there are no processes remaining, the state is safe.
            int count = 0;
            for (int i = 0; i < numTasks; i++) {
                if (tlist[i].status.equals("terminated")) {
                    count++;
                }
            }
            if (count == numTasks) {
                isSafe = true;
            }

            //2. seek a process P whose maximum additional request for each resource type is less than or equal to what remains for that type
            boolean[] finishedTask = new boolean[numTasks];
            //to initiate finished (before the check, how many processes have already been terminated or aborted
            for(int i = 0; i < numTasks; i++){
                if(tlist[i].status.equals("terminated") || tlist[i].status.equals("aborted")){
                    finishedTask[i] = true;
                }
            }

            while (true) {
                int previous = 0;
                int finished = 0;
                //before loop, count the previous finished tasks
                for (int i = 0; i < numTasks; i++) {
                    if (finishedTask[i] == true) {
                        previous++;
                    }
                }

                for (int i = 0; i < numTasks; i++) {
                    //continue check the task not finished yet.
                    if (finishedTask[i] == false) {

                        boolean satisfy = true;
                        int[] additional = tlist[i].additionalNeed();
                        for (int j = 0; j < numResources; j++) {
                            //couldn't satisfy this process.
                            if (tmpResource[j] <  additional[j]) {
                                satisfy = false;
                            }
                        }
                        //if satisfy, then regain all resources it has
                        if (satisfy == true) {
                            for (int k = 0; k < numResources; k++) {
                                tmpResource[k] += tlist[i].holding[k];
                            }
                            finishedTask[i] = true;
                            i = -1;   //reset the index to check the task list
                        }
                    }

                }

                // after check all the tasks, count finished process, if the number doesn't change, means that it is an unsafe state
                for (int i = 0; i < numTasks; i++) {
                    if (finishedTask[i] == true) {
                        finished++;
                    }
                }

                //if all process could be finished. then it's a safe state.
                if (finished == numTasks) {
                    isSafe = true;
                    break;
                }
                if (finished == previous) {
                    isSafe = false;
                    break;
                }
            }

            //reset the t's holding as grant not happened
            t.holding[requestType-1] -= requestAmount;
            return isSafe;

        }

    /**
     * This method check if task is holding any resources
     * @param t : task
     * @return boolean true for holding no resources
     */
    public boolean noResources(Task t){
        for(int i = 0; i < numResources; i++){
            if(t.holding[i] != 0){
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used for print short output such as the time taken, wait time and percentage of time spent waiting..
     */
    public void concise(){
        System.out.println("\n\t\t\tBANKER'S\t\t\t");
        int countWait = 0;
        int countFin = 0;
        for(int i = 0; i < numTasks; i++){
            if(tlist[i].status.equals("aborted")){
                System.out.println("\tTask " + (i+1) + "\t\t" + "aborted");
            }
            else{
                int fTime = tlist[i].finishTime;
                int wTime = tlist[i].getWaitingTime();
                countWait += wTime;
                countFin += fTime;
                double p = wTime * 1.0 / fTime * 100;
                System.out.println("\tTask " + (i+1) + "\t\t" + fTime + "\t" + wTime + "\t" + Math.round(p) + "%");
            }
        }
        System.out.println("\ttotal" + "\t\t" + countFin + "\t" + countWait + "\t" + Math.round((countWait * 1.0 / countFin * 100)) + "%");

    }
}

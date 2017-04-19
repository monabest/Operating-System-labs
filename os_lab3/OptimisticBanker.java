import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.*;

/**
 * This class is OptimisticBanker class which simulates the FIFO algorithm.
 *
 * Created by MonaBest on 4/1/17.
 */
public class OptimisticBanker {
    int numTasks;
    int numResources;
    HashMap<Integer, Integer> rMap;
    Task[] tlist;

    /**
     * This is the constructor to initiate the object.
     * @param numTasks
     * @param numResources
     * @param resourceMap
     * @param tasklist
     */
    public OptimisticBanker(int numTasks, int numResources, HashMap<Integer, Integer> resourceMap, Task[] tasklist){
        this.numTasks = numTasks;
        this.numResources = numResources;
        rMap = resourceMap;
        tlist = tasklist;
    }

    int terminated = 0; //number to record how many tasks terminated.
    int blocked = 0;    //number of process stuck in one cycle
    int numAborted = 0; //number of the aborted processes

    /**
     * This run method processes the activities of each task.
     */
    public void run(){
        int cycle = 0;
        //list to keep the order of blocked tasks.
        ArrayList<Task> blockedTask = new ArrayList<>();
        boolean isDead = false;

        //loop when there are processes not terminated or aborted
        while(terminated + numAborted != numTasks ) {

            //if the resource is released at this cycle, only be available at next cycle.
            int[] releasedResource = new int[numResources];
            if (cycle < numResources) {
                // optimistic banker ignores the claims.
                System.out.println("During " + cycle + "-" + (cycle + 1) + " each task completes its initiate");
                for(int j = 0; j < numTasks; j++){
                      tlist[j].activities.poll();  //poll out of all resources initiate.
                }
            }
            else{
                isDead = isDeadlock();
                if(isDead){
                    //abort from lowest number process
                    for(int i = 0; i < numTasks; i++){
                        if(tlist[i].status.equals("blocked")){
                            tlist[i].status = "aborted";
                            Task target = tlist[i];
                            blockedTask.remove(target);
                            blocked--;
                            numAborted++;
                            System.out.println("\nAccording to the spec, tasks " + (i +1) + " is  aborted now and resources are available next cycle " + (cycle) + "-" + (cycle+1));
                            for(int j = 0; j < numResources; j++){
                                int now = rMap.get(j+1) +  target.holding[j];
                                rMap.put(j+1, now);
                            }
                            if(!isDeadlock()){
                                isDead = false;  //abort process until it's not dead
                                break;
                            }

                        }

                    }
                }

                //next cycle starts
                System.out.println("\n" + "During " + cycle + "-" + (cycle + 1));

                //first check blocked list if there is any blocked list will be available.
                Queue<Task> taskTobeRemoved = new LinkedList<>();   //task needs to be removed from the blocked list

                //record the number of the process that has been processed in blocked list check, doesn't have to been processed in the main loop
                ArrayList<Integer> hasProcessed = new ArrayList<>();
                if(blocked > 0){
                    for(int i = 0; i < blockedTask.size(); i++) {
                        Task bTask = blockedTask.get(i);
                        int requestType = bTask.needing.type;
                        int requestAmount = bTask.needing.amount;

                        //if could satisfy the request then unblock the task
                        if (rMap.get(requestType) >= bTask.needing.amount) {
                            bTask.status = " ";
                            int index = requestType - 1;
                            int nowHas = bTask.holding[index] + requestAmount;
                            bTask.holding[index] = nowHas;

                            System.out.println("\t" + "Task " + bTask.taskNo + " completes its request (i.e., the request is granted).");
                            taskTobeRemoved.add(bTask);

                            int now =  rMap.get(requestType) - requestAmount;
                            rMap.put(requestType, now);
                            bTask.needing = new Resource(0,0);

                            hasProcessed.add(bTask.taskNo);
                            bTask.activities.poll(); //after granted, then poll out this activity from this task.

                        }

                    }
                }
                blocked = blocked - taskTobeRemoved.size();
                for(Task t: taskTobeRemoved){
                    blockedTask.remove(t);
                }

                //print blocked task that still can't be granted the request
                for(Task t : blockedTask){
                    System.out.println("\tTask " + t.taskNo + "'s request can't be granted. (i.e., is still blocked)");
                }

                //process each task's activities except the ones that already been processed in the blocked list check
                for(int i = 0; i < numTasks; i++) {
                    //has been processed at the blocked list check, skip
                    if(hasProcessed.contains(i+1)){
                        continue;
                    }
                    //current task
                    Task curTask = tlist[i];

                    //if it still has compute time, then computes in this cycle
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
                    if (!curTask.status.equals("terminated")  && !curTask.status.equals("aborted") && !curTask.status.equals("blocked")) {
                        Queue<Activity> activityQueue = curTask.activities;
                        if (!activityQueue.isEmpty()) {
                            Activity a = activityQueue.peek();

                            if (a.name.equals("request")) {
                                int requestType = a.num1;
                                int requestAmount = a.num2;
                                curTask.needing.type = requestType;
                                curTask.needing.amount = requestAmount;

                                if (rMap.get(requestType) >= requestAmount) {
                                    //if banker has enough resources, grant it immediately and update the resource map
                                    int remain = rMap.get(requestType) - requestAmount;
                                    rMap.put(requestType, remain);

                                    curTask.needing = new Resource(0, 0);  //reset to 0,0

                                    int index = requestType - 1;
                                    int nowHas = curTask.holding[index] + requestAmount;
                                    curTask.holding[index] = nowHas;

                                    activityQueue.poll(); //after granted, then poll out this activity.
                                    System.out.println("\t" + "Task " + curTask.taskNo + " completes its request (i.e., the request is granted).");
                                    continue;   //finish current task, continue this numTask loop.
                                } else {
                                    blocked++;
                                    curTask.status = "blocked";
                                    blockedTask.add(curTask);   //doesn't poll out the activity here
                                    System.out.println("\t" + "Task " + curTask.taskNo + "'s request can't be granted. (i.e., task " + curTask.taskNo + " is blocked). ");
                                }
                            }
                            else if (a.name.equals("release")) {
                                int releaseType = a.num1;
                                int releaseAmount = a.num2;
                                //add the released resource to the list for now
                                releasedResource[releaseType-1] += releaseAmount;

                                curTask.holding[releaseType - 1] -= releaseAmount;
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
                                //if it currently doesn't hold resource, then it is delayed
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

            //update the resource map, add the resource released this cycle and use them for the next cycle
            for(int i = 0; i < numResources; i++){
                rMap.put(i+1, rMap.get(i+1) + releasedResource[i]);
            }
            cycle++;
        }

    }


    /**
     * This method checks whether the last cycle is deadlock or not.
     * It is deadlock if resource map can't satisfying any request need.
     *
     * @return boolean : true for isDead
     */
    public boolean isDeadlock(){

        for(int i = 0; i < numTasks; i++){
            Task t = tlist[i];
            if(!t.status.equals("aborted") && !t.status.equals("terminated")){
                if(!t.status.equals("blocked")){
                    return false;
                }
                if(!t.activities.isEmpty()) {
                    Activity a = t.activities.peek();
                    if (a.name.equals("release")) {
                        return false;
                    }
                    if (a.name.equals("request")) {
                        if (rMap.get(a.num1)  >= a.num2 ) {
                            return false;  //can satisfy one of the processes making request.
                        }
                    }
                }
            }
        }
        return true;   //else return true for isDeadlock
    }

    /**
     * This method checks if the task is holding any resources.
     *
     * @param t : task
     * @return boolean : true for holding no resources.
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
        System.out.println("\n\t\t\tFIFO\t\t\t");
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

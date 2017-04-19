import java.util.LinkedList;
import java.util.*;

/**
 * Task class is used for creating task object.
 *
 * Created by MonaBest on 4/1/17.
 */
public class Task {

    int taskNo;
    int finishTime;
    int waitingTime;
    String status;

    int[] holding;     //holding how much resources the task is holding for each resource type.
    Resource needing;

    Queue<Activity> activities;
    int numResources;
    int computeTime;
    int computeRemain;

    Queue<Activity> activitiesCopy;   //used for time calculation

    int delayed;
    int[] claims;    //record the claims at the initialization

    /**
     * This is a constructor used for initializing the Task object
     * @param taskNo
     * @param numResources
     */
    public Task(int taskNo, int numResources){
        this.taskNo = taskNo;
        this.finishTime = 0;
        this.waitingTime = 0;
        this.status = "";
        this.activities = new LinkedList<>();
        this.activitiesCopy = new LinkedList<>();

        this.holding = new int[numResources];
        this.needing = new Resource(0,0);
        this.numResources = numResources;
        this.computeTime = 0;
        this.computeRemain = 0;
        this.delayed = 0;
        this.claims = new int[numResources];
    }

    /**
     * This method calculates the waiting time.
     * @return
     */
    public int getWaitingTime(){
        int activeCycle = 0;
        for(Activity a : activitiesCopy){
            if(a.name.equals("request") || a.name.equals("release") || a.name.equals("initiate")){
                activeCycle++;
            }
            if(a.name.equals("compute")){
                activeCycle += a.num1;
            }
        }
        waitingTime = finishTime - activeCycle;
        return waitingTime;
    }

    /**
     * This methods calculates the amount of additional resources the task needs to finish
     * @return
     */
    public int[] additionalNeed(){
        int[] additional = new int[numResources];
        for(int i = 0; i < numResources; i++){
            additional[i] = claims[i] - holding[i];
        }
        return additional;
    }
}

import java.util.*;

/**
 * Created by MonaBest on 4/19/17.
 */
public class Paging {

    final int Quantum = 3;
    int machineSize;
    int pageSize;
    int processSize;
    int jobMix;
    int numOfReferences;
    String algorithm;
    int totalPageFaults;
    int residencyTime;

    int processAmount;
    Queue<Process> processQueue;
    Queue<Process> finishedQueue;

    Page[] frameTable;
    Scanner randomGenerator;
    int numOfFrame;
    int outputMode;

    public Paging(int machineSize, int pageSize, int processSize, int jobMix, int numberOfReferences, String algorithm, Scanner randomGenerator, int outputMode){
        this.machineSize = machineSize;
        this.pageSize = pageSize;
        this.processSize = processSize;
        this.jobMix = jobMix;
        this.numOfReferences= numberOfReferences;
        this.algorithm = algorithm;
        this.randomGenerator = randomGenerator;
        this.outputMode = outputMode;
        finishedQueue = new LinkedList<>();

        numOfFrame = machineSize / pageSize; //number of frames.
        frameTable = new Page[numOfFrame];
        for(int i = 0; i < numOfFrame; i++){
            frameTable[i] = new Page(-1, -1, -1); //default initialization.
        }
       

        //initialize the process queue
        processQueue = new LinkedList<>();
        switch(jobMix) {
           //fully sequential case
            case 1: processAmount = 1;
                for(int i = 0; i < processAmount; i++) {
                    Process p = new Process(i+1,numberOfReferences, processSize,1,0,0); //make the No. of process offset 1
                    processQueue.add(p);
                }
                break;
            case 2: processAmount = 4;
                for(int i = 0; i < processAmount; i++) {
                    Process p = new Process(i+1, numberOfReferences, processSize,1,0,0);
                    processQueue.add(p);
                }
                break;
            case 3: processAmount = 4;
                for(int i = 0; i < processAmount; i++) {
                    Process p = new Process(i+1, numberOfReferences, processSize, 0,0,0); //fully random references
                    processQueue.add(p);
                }
                break;
            case 4: processAmount = 4;
                for(int i = 0; i < processAmount; i++) {
                    if(i == 0) {
                        Process p = new Process(i+1, numberOfReferences, processSize, 0.75, 0.25, 0);
                        processQueue.add(p);
                    }
                    else if(i == 1) {
                        Process p = new Process(i+1, numberOfReferences, processSize, 0.75, 0, 0.25);
                        processQueue.add(p);
                    }
                    else if(i == 2) {
                        Process p = new Process(i+1, numberOfReferences, processSize, 0.75, 0.125, 0.125);
                        processQueue.add(p);
                    }
                    else {
                        Process p = new Process(i+1, numberOfReferences, processSize, 0.5, 0.125, 0.125);
                        processQueue.add(p);
                    }

                }
                break;
        }

    }


    int time = 0;
    public void run() {
        while(!processQueue.isEmpty()){
            Process current = processQueue.peek();
            while(current.quantum < 3 && current.numReference >= 1) {
                time++;
                int word = current.nextWord;
                int page = word / pageSize;  //page number start from 0, frame start from 0

                boolean hit = false;
                for (int i = 0; i < numOfFrame; i++) {
                    //if there are page already loads, then hit.
                    if (frameTable[i].pageNumber == page && frameTable[i].processNo == current.processNo) {
                        if(outputMode != 0) {
                            System.out.println(current.processNo + " references word " + word + " (page " + page + ") at time " + time + " : Hit in frame " + i);
                        }
                        //update the last referenced time attribute
                        frameTable[i].lastReferenced = time;
                        //first calculate next word even before context switching
                        current.calculateNextWord(randomGenerator,outputMode);
                        hit = true;
                        break;
                    }
                }

                //if there are page faults, have to find the free frame or do the replacement.
                if (hit == false) {
                    //find highest numbered free frame;
                    //if found, then load the page into the frame
                    int highest = highestFreeFrame();
                    if (highest != -1) {
                        frameTable[highest] = new Page(time, page, current.processNo);

                        current.totalFaults++;
                        if(outputMode != 0) {
                            System.out.println(current.processNo + " references word " + word + " (page " + page + ") at time " + time + ": fault, using free frame " + highest);
                        }
                        //update the last referenced time attribute
                        frameTable[highest].lastReferenced = time;
                        current.calculateNextWord(randomGenerator, outputMode);

                        current.quantum++;
                        current.numReference--;
                        continue; //hit the frame, continue the outer process loop.
                    }

                    //use certain algorithm to find the target
                    int frameEvicted = 0;
                    //random replacement algorithm
                    if (algorithm.equals("random")) {
                        //if free frame not found, do the random replacement algorithm.
                        int randomNumber = randomGenerator.nextInt();
                        frameEvicted = randomNumber % numOfFrame;
                        if(outputMode !=0) {
                            System.out.println(current.processNo + " uses random number: " + randomNumber);
                        }
                    }

                    //lru replacement algorithm
                    else if (algorithm.equals("lru")) {
                        int leastRecent = time;
                        for (int j = 0; j < numOfFrame; j++) {
                            if (frameTable[j].lastReferenced < leastRecent) {
                                frameEvicted = j;
                                leastRecent = frameTable[j].lastReferenced;
                            }
                        }

                    }
                    //lifo replacement algorithm
                    else if (algorithm.equals("lifo")) {
                        int lastLoadTime = 0; //initialize
                        for (int j = 0; j < numOfFrame; j++) {
                            if (frameTable[j].loadTime > lastLoadTime) {
                                lastLoadTime = frameTable[j].loadTime;
                                frameEvicted = j; //update
                            }
                        }

                    }

                    int victimPage = frameTable[frameEvicted].pageNumber;
                    //update the victim process
                    frameTable[frameEvicted].setEvictTime(time);
                    int victimProcess = -1;
                    int increaseTime = frameTable[frameEvicted].getResidencyTime();

                    boolean checked = false;
                    for (Process p : processQueue) {
                        if (p.processNo == frameTable[frameEvicted].processNo) {
                            p.runningSum += increaseTime;
                            p.numOfEviction++;
                            victimProcess = p.processNo;
                            checked = true;
                        }
                    }
                   //probably that process is already in the finished queue
                    if(!checked) {
                        for (Process p : finishedQueue) {
                            if (p.processNo == frameTable[frameEvicted].processNo) {
                                p.runningSum += increaseTime;
                                p.numOfEviction++;
                                victimProcess = p.processNo;
                            }
                        }
                    }
                    current.totalFaults++;

                    //load the page to the frame
                    frameTable[frameEvicted] = new Page(time, page, current.processNo);
                    if(outputMode != 0) {
                        System.out.println(current.processNo + " references word " + word + " (page " + page + ") at time " + time +
                                " : Fault, evicting page " + victimPage + " of " + victimProcess + " from frame " + frameEvicted);
                    }
                    //update the last referenced time attribute
                    frameTable[frameEvicted].lastReferenced = time;
                    //first calculate next word even before context switching
                    current.calculateNextWord(randomGenerator,outputMode);
                }
                    ///update the current process q. if quantum == 3 or numReference == 0, out of this process's loop
                    current.quantum++;
                    current.numReference--;
            }

                //if process finishes, then poll
                if(current.numReference == 0){
                    processQueue.poll();
                    finishedQueue.add(current);
                   // System.out.println("----remove the process" + current.processNo);
                    continue;
                }
                //if it is not finished, but runs up the quantum
                if(current.quantum == Quantum) {
                    processQueue.remove(current);
                    current.quantum = 0;
                    processQueue.add(current);
                }

            }

        }

    //find the highest numbered free frame in the table
    public int highestFreeFrame(){
        int highest = -1;
        for(int i = numOfFrame-1 ; i >= 0; i--){
            if(frameTable[i].processNo == -1){
                highest = i;
                break;
            }
        }
        return highest;
    }

    public void printSummary() {
        //each process summary
        System.out.println();
        int totalEviction = 0;
        for(int i = 0; i < processAmount; i++){
            for(Process p : finishedQueue){
                if(p.processNo == i+1){
                    totalPageFaults += p.totalFaults;
                    totalEviction += p.numOfEviction;
                    residencyTime += p.runningSum;
                    if(p.numOfEviction == 0){
                        System.out.println("Process " + (i+1) + " had " + p.totalFaults + " faults and with no evictions, the average residence is undefined.");
                    }
                    else{
                        System.out.println("Process " + (i+1) + " had " + p.totalFaults + " faults and " + p.getAverageResidencyTime() + " average residency ");

                    }
                    break;
                }
            }
        }
        //program summary
        if(totalEviction == 0){
            System.out.println("\nThe total number of faults is "  + totalPageFaults + " and  with no evictions, the overall average residence is undefined.");

        }
        else{
            System.out.println("\nThe total number of faults is "  + totalPageFaults + " and the overall average residency is " + residencyTime * 1.0 / totalEviction);

        }
    }

}

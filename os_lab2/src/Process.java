/**
 * Created by MonaBest on 2/23/17.
 */
public class Process {
    int arrival;
    int interval;
    int total;
    int m;

    public Process(int a, int b, int c, int m,int i){
        this.arrival = a;
        this.interval = b;
        this.total = c;
        this.m = m;
        this.inputOrder = i;

        cpuRemain = c;
        waitTime = 0;
        ioTime = 0;

        status = 0;

        blockRemain = 0;
        runningRemain = 0;

        finishTime = 0;

        quantum = 0;

    }

    public String numberInfo(){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(arrival + " ");
        sb.append(interval + " ");
        sb.append(total + " ");
        sb.append(m);
        sb.append(") ");
        return sb.toString();
    }


    //record the remaining cpu from the start is C
    int cpuRemain;

    //record the total wait time, is time in the ready state
    int waitTime;

    //record the total I/O time
    int ioTime;

    //status: 0 - unstarted; 1 - ready; 2 -running; 3 - blocked; 4 - finished
    int status;


    int quantum;

    //record the current blockTime
    int blockRemain;

    //record the current running time
    int runningRemain;


    //record the finish time
    int finishTime;


    public int getTurnaroundTime(){
        int turnAroundTime = this.finishTime - this.arrival;
        return turnAroundTime;
    }


    public String getStatus(){
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        switch(status){
            case 0:
                sb.append("unstarted  " + 0); break;
            case 1:
                sb.append("ready  " + 0); break;
            case 2:
                sb.append("running  " + runningRemain); break;
            case 3:
                sb.append("blocked  " + blockRemain); break;
            case 4:
                sb.append("terminated  " + 0); break;
        }


        return sb.toString();
    }


    public String info(){
        StringBuilder sb = new StringBuilder();
        sb.append("\t\t(A, B, C, M) = ");
        sb.append(numberInfo() + "\n");
        sb.append("\t\tFinishing Time: " + finishTime + "\n");
        sb.append("\t\tTurnaround Time: " + getTurnaroundTime() + "\n");
        sb.append("\t\tI/O time: " + ioTime + "\n");
        sb.append("\t\tWaiting time: " + getWaitTime());
        return sb.toString();

    }

    public int getWaitTime(){
        return finishTime - ioTime - total - arrival;
    }

    //record the input order for breaking tie
    int inputOrder;
    public int getInputOrder(){
        return inputOrder;
    }


    public void initlize(){
        cpuRemain = total;
        waitTime = 0;
        ioTime = 0;

        status = 0;

        blockRemain = 0;
        runningRemain = 0;

        finishTime = 0;

        quantum = 0;

    }

}

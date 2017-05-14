import java.util.Scanner;

/**
 * Created by MonaBest on 4/21/17.
 */
public class Process {
    final long RAND_MAX = 2147483648L;
    int numReference;  //number of running
    int size;
    int nextWord;    //next word to reference
    int processNo;   //No. of the process
    int runningSum; //current page's residency time and add it to a running sum.
    int totalFaults;
    double pA;
    double pB;
    double pC;
    int quantum;

    int firstReference; //first reference will cause a page fault
    int numOfEviction;

    public Process( int processNo, int numReference, int s, double a, double b, double c) {
        this.numReference = numReference;
        this.size = s;
        this.processNo = processNo;
        this.pA = a;
        this.pB = b;
        this.pC = c;
        this.quantum = 0;

        firstReference = numReference;
        this.numOfEviction = 0;


        totalFaults = 0;
        runningSum = 0;
        nextWord = 111 * processNo % s;

    }

    public double getAverageResidencyTime() {
        return runningSum * 1.0 / numOfEviction;
    }

    public void calculateNextWord(Scanner random, int outputMode) {
        int r = random.nextInt();
        if(outputMode != 0) {
            System.out.println(processNo + " uses random number: " + r);
        }

        double quotient = r / (RAND_MAX + 1d); //this quotient satisfy 0 <=y < 1.
        if(quotient < pA) {
            nextWord = (nextWord + 1) % size;
        }
        else if(quotient < pA + pB) {
            nextWord = (nextWord -5 + size) % size;
        }
        else if(quotient < pA + pB + pC) {
            nextWord = (nextWord + 4) % size;
        }
        else {
            int randomNumber = random.nextInt();
            if(outputMode != 0){
                System.out.println(processNo + " uses random number: " + randomNumber);
            }
            nextWord = randomNumber % size;
        }
    }

}

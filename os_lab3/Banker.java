import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.*;


/**
 * This is the main class used to process the input txt file from arg[0].
 * This class reads all file first and then processes the data to make task object and resource map.
 * In the main function, it creates two Banker (one is FIFO and the other is Banker), and runs them separately.
 * At last, it calls concise() function of the banker objects to give the short output.
 *
 * Created by MonaBest on 3/30/17.
 */
public class Banker {
    public static void main(String args[]) throws Exception{

        // File file = new File("./input/input-07.txt");
        File file = new File(args[0]);
        HashMap<Integer, Integer> resourceMap = new HashMap<>();
        int numTasks = 0;
        int numResources = 0;

        BufferedReader br = null;

        //used for reading all data at once.
        StringBuilder sb = new StringBuilder();
        try{
            br = new BufferedReader(new FileReader(file));
            String thisLine;
            while((thisLine = br.readLine()) != null) {
                if (thisLine.equals("")) continue;
                sb.append(" ");
                sb.append(thisLine);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try {
                br.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        String[] inputString = sb.toString().trim().split("\\s+");
        numTasks = Integer.parseInt(inputString[0]);
        numResources = Integer.parseInt(inputString[1]);
        for(int i = 0; i < numResources; i++) {
            int numUnit = Integer.parseInt(inputString[i+2]);
            System.out.println(i + ";" + numUnit);   //make offset 1
            resourceMap.put(i+1, numUnit);
        }

        //make two copy of task list for two bankers
        Task[] tasklist1 = new Task[numTasks];
        Task[] tasklist2 = new Task[numTasks];

        for(int i = 0; i< numTasks; i++){
            tasklist1[i] = new Task(i+1, numResources);
            tasklist2[i] = new Task(i+1, numResources);
        }

        //go through the input string to extract the activity and adds to the Task object.
        for(int j = 2 + numResources; j < inputString.length; j+=4){
            String str = inputString[j];
            int taskNo = Integer.parseInt(inputString[j+1]);
            int num1 = Integer.parseInt(inputString[j+2]);
            int num2 = Integer.parseInt(inputString[j+3]);

            tasklist1[taskNo - 1].activities.add(new Activity(str, taskNo, num1, num2));
            tasklist1[taskNo - 1].activitiesCopy.add(new Activity(str, taskNo, num1, num2));
            tasklist2[taskNo - 1].activities.add(new Activity(str, taskNo, num1, num2));
            tasklist2[taskNo - 1].activitiesCopy.add(new Activity(str, taskNo, num1, num2));

        }

        OptimisticBanker banker1 = new OptimisticBanker(numTasks, numResources, resourceMap, tasklist1);
        banker1.run();
        PessimisticBanker banker2 = new PessimisticBanker(numTasks, numResources, resourceMap, tasklist2);
        banker2.run();

        banker1.concise();
        banker2.concise();
    }
}

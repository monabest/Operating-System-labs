/**
 * This Acitivy class is used for creating Activity objects containing name, task number and two following number.
 *
 * Created by MonaBest on 4/1/17.
 */
public class Activity {
    String name;
    int taskNo;
    int num1;
    int num2;

    /**
     * This is a constructor used for initiating the objects.
     * @param name
     * @param taskNo
     * @param num1
     * @param num2
     */
    public Activity(String name, int taskNo, int num1, int num2){
        this.name = name;
        this.taskNo = taskNo;
        this.num1 = num1;
        this.num2 = num2;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("info" + name + taskNo + num1 + num2 );
        return sb.toString();
    }
}

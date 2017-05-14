/**
 * Created by MonaBest on 4/19/17.
 */
public class Page {
    int loadTime;
    int evictTime;
    int pageNumber;
    int processNo;


    int lastReferenced;

    public Page(int loadTime, int pageNumber, int processNo) {
        this.loadTime = loadTime;
        this.pageNumber = pageNumber;
        this.processNo = processNo;

    }

    public void setEvictTime(int time) {
        this.evictTime = time;
    }


    public int getResidencyTime() {
        return evictTime - loadTime;
    }
}

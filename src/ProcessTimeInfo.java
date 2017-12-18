/**
 * Ricart and Agrawala's Algoritm simulaton
 * ----------------------------------------
 * Student : 傅正安
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: ProcessTimeInfo.java
 */
class ProcessTimeInfo {

    // arrivalTime 一個 process 剛產生的時間
    private final double arrivalTime;

    // criticalSectionAdmissionTime 一個 process 允許發出訊息想要進入
    // critical section 的時間
    private double criticalSectionAdmissionTime;

    /**
     * @param arrivalTime 一個 process 剛產生的時間
     */
    public ProcessTimeInfo(double arrivalTime) {
        this.arrivalTime = arrivalTime;
        this.criticalSectionAdmissionTime = 0;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getCriticalSectionAdmissionTime() {
        return criticalSectionAdmissionTime;
    }

    public void setCriticalSectionAdmissionTime(double time) {
        criticalSectionAdmissionTime = time;
    }
}

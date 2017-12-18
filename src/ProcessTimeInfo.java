/**
 * Ricart and Agrawala's Algoritm simulaton
 * ----------------------------------------
 * Student : �ť��w
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: ProcessTimeInfo.java
 */
class ProcessTimeInfo {

    // arrivalTime �@�� process �貣�ͪ��ɶ�
    private final double arrivalTime;

    // criticalSectionAdmissionTime �@�� process ���\�o�X�T���Q�n�i�J
    // critical section ���ɶ�
    private double criticalSectionAdmissionTime;

    /**
     * @param arrivalTime �@�� process �貣�ͪ��ɶ�
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

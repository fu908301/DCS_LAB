package simpack;

public class FACILITY_STATE {
    public SimEvent[] queue = new SimEvent[Const.MAX_TOKENS];
    public int queue_length;
    public int status;
    public String name;
    public int total_servers;
    public int busy_servers;
    public double total_busy_time;
    public double start_busy_time;
    public int preemptions;
    public int server_info[][];

    public FACILITY_STATE() {
        server_info = new int[Const.MAX_SERVERS][2];
    }
}

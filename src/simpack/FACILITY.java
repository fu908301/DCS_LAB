package simpack;

public class FACILITY {
    public LIST_ queue;
    public int status;
    public String name;
    public int total_servers;
    public int busy_servers;
    public double total_busy_time;
    public double start_busy_time;
    public int preemptions;
    public int server_info[][];

    public FACILITY() {
        queue = new LIST_();
        server_info = new int[Const.MAX_SERVERS][2];
    }
}

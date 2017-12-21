/**
 * Ricart and Agrawala's Algoritm simulaton
 * ----------------------------------------
 * Student : 傅正安
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: Richart.java
 */

import simpack.Sim;
import simpack.SimEvent;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Collections;

public class Richart extends DistributedMutualExclusion {

    // 就某個 nodeNumbers 來說，目前傳送出去的 timestamp，Cs = Hs + 1
    private final int[] our_sequence_no = new int[MAX_NODE];

    // 就某個 nodeNumbers 來說，目前所知最大的 timestamp
    private final int[] high_sequence_no = new int[MAX_NODE];

    // 就某個 nodeNumbers 來說，需要等待的 reply 個數
    private final int[] outstand_reply_count = new int[MAX_NODE];

    // 就某個 nodeNumbers 來說，記錄要回哪些 nodeNumbers reply
    private final boolean[][] reply_deferred = new boolean[MAX_NODE][MAX_NODE];

    // 就某個 nodeNumbers 來說，記錄 nodeNumbers 內的 processes
    private final Queue<ProcessTimeInfo>[] wait_queue = new Queue[MAX_NODE];

    // 就某個 nodeNumbers 來說，是否要求進入 critical section
    private final boolean[] req_crit_sect = new boolean[MAX_NODE];

    //記錄上一次進入 critical section 的時間
    private double save_time;

    public Richart() {
        for (int i = 0; i < MAX_NODE; i++) {
            wait_queue[i] = new LinkedList<ProcessTimeInfo>();
        }

    }

    protected void initial() {
        SimEvent event;
        for (int i = 0; i < nodeNumbers; i++) {
            event = new SimEvent();

            // id 記錄 event 的狀態
            event.id = PROCESS_ARRIVAL;

            // attr[0] 記錄產生 process 的 node 的號碼
            event.token.attr[0] = i;

            Sim.schedule(event, Sim.expntl(arrival_rate));
        }
    }

    protected void Process_Arrival(SimEvent event) {
        // attr[0] 記錄 node 的號碼
        int NodeId = (int) event.token.attr[0];

        // 如果 waiting queue 是空的且 node 沒有要求進入 critical section
        // 則將新產生的 process 放入 waiting queue，並由 node 送出 request
        // 要求進入 critical section
        if ((wait_queue[NodeId].peek() == null) && (!req_crit_sect[NodeId])) {
            wait_queue[NodeId].add(new ProcessTimeInfo(Sim.time()));
            Send_Requests(NodeId);
        }
        // 如果 waiting queue 不是空的，表示 node 中至少有一個 process 在
        // 要求進入 critical section，則將目前新產生的 process 放入
        // waiting queue
        else
            wait_queue[NodeId].add(new ProcessTimeInfo(Sim.time()));

        // 呼叫 exponential 函數設定產生下一個新的 process 所要延遲的時間
        double new_process_delay_time = Sim.expntl(arrival_rate);

        // 將產生新的 process 所延遲的時間加入統計
        ran_var_stat.add(new_process_delay_time);

        // 為目前的 node 產生一個新的 process
        event = new SimEvent();
        event.id = PROCESS_ARRIVAL;
        event.token.attr[0] = NodeId;
        Sim.schedule(event, new_process_delay_time);
    }

    void Send_Requests(int Source_Node) {

        // 設定目前 process 開始參與競爭進入 critical section 的時間
        (wait_queue[Source_Node].peek()).setCriticalSectionAdmissionTime(Sim.time());

        // 記下目前的 node 需要等待其他 node 傳回 reply 的總數
        outstand_reply_count[Source_Node] = nodeNumbers - 1;

        // 將此 node 要參與競爭進入 critical section 的號誌 turn on
        req_crit_sect[Source_Node] = true;

        // 設定目前要傳送出去與別人競爭的 timestamp，Cs = Hs + 1
        our_sequence_no[Source_Node] = high_sequence_no[Source_Node] + 1;

        // 將 message 傳送給自己以外的 node
        SimEvent event = new SimEvent();
        for (int receive = 0; receive < nodeNumbers; receive++)
            if (receive != Source_Node) {
                // attr[0] 記下傳送 request_receive 的 node
                event.token.attr[0] = Source_Node;
                // attr[1] 記下 timestamp
                event.token.attr[1] = our_sequence_no[Source_Node];
                // attr[2] 記下接收此 message 的 node
                event.token.attr[2] = receive;
                event.id = REQUEST_RECEIVED;
                Sim.schedule(event, Trans_Time);
            }
    }

    protected void Request_Received(SimEvent event) {
        // 送出 message 的 node
        int Send_Node_Id;

        // 送出 message 的 node 所傳送來的 timestamp
        int Send_Node_Seq_No;

        // 接收 message 的 node
        int Rec_Node_Id;

        // attr[0] 記下傳送 request_receive 的 node
        Send_Node_Id = (int) event.token.attr[0];
        // attr[1] 記下 timestamp
        Send_Node_Seq_No = (int) event.token.attr[1];
        // attr[2] 記下接收此 message 的 node
        Rec_Node_Id = (int) event.token.attr[2];

        // 藉由傳送來的 timestamp 做到時間的同步
        high_sequence_no[Rec_Node_Id] =
                findMaximum(high_sequence_no[Rec_Node_Id], Send_Node_Seq_No);

        // 判別是否要延遲傳送 reply 訊息
        // 如果傳送來的 timestamp 比目前自己所記錄的 timestamp 來的大，則要延遲
        boolean Defer_It, first_test, second_test;
        first_test = (Send_Node_Seq_No > our_sequence_no[Rec_Node_Id]);
        // 如果 timestamp 一樣大，則利用 node 號碼的大小判斷是否延遲，號碼小的優先權較高
        second_test = ((Send_Node_Seq_No == our_sequence_no[Rec_Node_Id])
                && (Send_Node_Id > Rec_Node_Id));
        Defer_It = (req_crit_sect[Rec_Node_Id] && (first_test || second_test));

        if (Defer_It)
            reply_deferred[Rec_Node_Id][Send_Node_Id] = true;
        else {
            // 告訴號碼為 Send_Node_Id 的 node 接收到一個 reply message
            event = new SimEvent();
            event.id = REPLY_RECEIVED;
            event.token.attr[0] = Send_Node_Id;
            Sim.schedule(event, Trans_Time);
        }
    }

    protected void Reply_Received(SimEvent event) {
        int NodeId;
        NodeId = (int) event.token.attr[0];

        // 等到 reply 的個數減為 0，表示可以進入 critical section
        if (--outstand_reply_count[NodeId] == 0) {
            event = new SimEvent();
            event.id = EXECUTE_CS;
            event.token.attr[0] = NodeId;
            Sim.schedule(event, CSTime);
        }
    }

    protected void Execute_CS(SimEvent event) {

        // 從 event queue 中取出 token，設定 token 所記錄的 NodeId
        int NodeId = (int) event.token.attr[0];

        // 從 waiting queue 取出 process 的資料
        ProcessTimeInfo p = wait_queue[NodeId].element();

        // 記錄一個 process 從產生到可以進入 critical section 所等待的時間，並將此時間
        // 加入 reponse_R1 做統計用
        response_R1.add(Sim.time() - p.getArrivalTime());

        // 記錄一個 process 從允許參與競爭進入 critical section 到可以進入
        // critical section 所等待的時間，並將此時間加入 response_R2 做統計用
        response_R2.add(Sim.time() - p.getCriticalSectionAdmissionTime());

        // 記錄 critical section 被前一個 process 進入的時間與目前 process 進入的時間的差
        delay_time_D.add(Sim.time() - save_time);

        // 記下目前 process 要進入 critical section 的時間，作為當下一個 process 要進入
        // critical section 時， delay_time_D 所需要的資訊
        save_time = Sim.time();

        // 號碼為 NodeId 的 node 中的第一個 process 已經順利進入 critical section
        // 所以將它從 node 的 waiting queue 移除
        wait_queue[NodeId].remove();

        // 號碼為 NodeId 的 node 暫時標記為沒有要參與競爭 critical section
        req_crit_sect[NodeId] = false;

        for (int RecNodeId = 0; RecNodeId < nodeNumbers; RecNodeId++)
            // 將延遲的 reply 傳回給其他的 node
            if (reply_deferred[NodeId][RecNodeId]) {
                reply_deferred[NodeId][RecNodeId] = false;
                event = new SimEvent();
                event.id = REPLY_RECEIVED;
                event.token.attr[0] = RecNodeId;
                Sim.schedule(event, Trans_Time);
            }

        // 如果目前的 node 的 waiting queue 中還有 process 想要進入 critical section
        // 則發出訊息參與競爭
        if (wait_queue[NodeId].peek() != null)
            Send_Requests(NodeId);
    }


    private int findMaximum(int i, int j) {
        if (i > j)
            return i;
        else
            return j;
    }
}

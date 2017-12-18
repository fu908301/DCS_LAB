import java.util.LinkedList;
import java.util.Queue;
import simpack.Sim;
import simpack.SimEvent;

public class Raymond extends DistributedMutualExclusion {

	int Token_Node = 0;
	int NEAR[] = new int[MAX_NODE];
    class RequestNode{
    	public int ID;
    	public double TIME;
    }

    // 就某個 nodeNumbers 來說，記錄 nodeNumbers 內的 processes
    private final Queue<RequestNode>[] Wait_Queue = new Queue[MAX_NODE];	//主要是放NODE本身產生的process
    private final Queue<RequestNode>[] Local_Queue = new Queue[MAX_NODE];	//主要是放其他NODE產生的request

    // 就某個 nodeNumbers 來說，是否要求進入 critical section
    private final boolean[] req_crit_sect = new boolean[MAX_NODE];
    private final boolean[] exe_crit_sect = new boolean[MAX_NODE];
    
	public Raymond() {
		for (int i = 0; i < MAX_NODE; i++) {
            Wait_Queue[i] = new LinkedList<RequestNode>();
            Local_Queue[i] = new LinkedList<RequestNode>();
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

            Sim.schedule(event, Sim.poisson(arrival_rate));
        }
    	
		switch(CASE_NUMBER){
        case 1:
        	for(int i=nodeNumbers-1 ; i>=1 ; i--){
        		NEAR[i] = i-1;
        	}
        	NEAR[0] = 0;
        	break;
        case 2:
        	for(int i=nodeNumbers-1 ; i>=1 ; i--){
        		NEAR[i] = 0;
        	}
        	NEAR[0] = 0;
        	break;
        case 3:
        	for(int i=nodeNumbers-1 ; i>=5 ; i--){
        		for(int j=4 ; j>=1 ; j--){
        			if( (i-1)/4 == j)
        				NEAR[i] = j;
        		}
        	}
        	for(int i=4 ; i>=1 ; i--){
        		NEAR[i] = 0;
        	}
        	NEAR[0] = 0;
        	break;
        default:
        	break;
        }
		
    }

    protected void Process_Arrival(SimEvent event) {
        // attr[0] 記錄 node 的號碼
        int NodeId = (int) event.token.attr[0];

        if(Wait_Queue[NodeId].peek() != null){
        	RequestNode Node = new RequestNode();
        	Node.ID = NodeId;
        	Node.TIME = Sim.time();
        	Wait_Queue[NodeId].add(Node);
        }
        else{	//waiting queue裡面沒有東西        	        	
        	if(NodeId != NEAR[NodeId]){
        		RequestNode Node = new RequestNode();
        		Node.ID = NodeId;
        		Node.TIME = Sim.time();
        		Wait_Queue[NodeId].add(Node);
        		Local_Queue[NodeId].add(Node);
        		if(Local_Queue[NodeId].size() == 1){
        			Send_Requests(NodeId);
        		}
        	}
        }

        // 呼叫posson函數設定產生下一個新的 process 所要延遲的時間
        double new_process_delay_time = Sim.poisson(arrival_rate);

        // 將產生新的 process 所延遲的時間加入統計
        ran_var_stat.add(new_process_delay_time);

        // 為目前的 node 產生一個新的 process
        event = new SimEvent();
        event.id = PROCESS_ARRIVAL;
        event.token.attr[0] = NodeId;
        Sim.schedule(event, new_process_delay_time);
    }

    void Send_Requests(int Source_Node) {

        // 將此 node 要參與競爭進入 critical section 的號誌 turn on
        req_crit_sect[Source_Node] = true;

        // 將 message 傳送下一個 node
        SimEvent event = new SimEvent();
        event = new SimEvent();
        // attr[0] 記下傳送 request_receive 的 node
        event.token.attr[0] = Source_Node;
        // attr[2] 記下接收此 message 的 node
        event.token.attr[2] = NEAR[Source_Node];
        event.id = REQUEST_RECEIVED;
        Sim.schedule(event, Trans_Time);
        
    }

    protected void Request_Received(SimEvent event) {
        // 送出 message 的 node
        int Send_Node_Id;

        // 接收 message 的 node
        int Rec_Node_Id;

        // attr[0] 記下傳送 request_receive 的 node
        Send_Node_Id = (int) event.token.attr[0];

        // attr[2] 記下接收此 message 的 node
        Rec_Node_Id = (int) event.token.attr[2];

        RequestNode Node = new RequestNode();
        Node.ID = Send_Node_Id;
        Node.TIME = Sim.time();
        Local_Queue[Rec_Node_Id].add(Node);
        
        if((NEAR[Rec_Node_Id] == Rec_Node_Id) && ( !((req_crit_sect[Rec_Node_Id] ) || exe_crit_sect[Rec_Node_Id]))){	
        	RequestNode NewTokenNode = Local_Queue[Rec_Node_Id].poll();
    		//轉移Token
        	SimEvent event1;;
            event1 = new SimEvent();
            // attr[0] 記下傳送 request_receive 的 node
            event1.token.attr[0] = Rec_Node_Id;
            // attr[2] 記下接收此 message 的 node
            event1.token.attr[2] = NewTokenNode.ID;
            event1.id = REPLY_RECEIVED;
            Sim.schedule(event1, Trans_Time);
            NEAR[Rec_Node_Id] = NewTokenNode.ID;
    		if(Local_Queue[Rec_Node_Id].size() > 0)
    			Send_Requests(Rec_Node_Id);
    	}
        else if((NEAR[Rec_Node_Id]!=Rec_Node_Id)&&(Local_Queue[Rec_Node_Id].size() == 1)){
    		Send_Requests(Rec_Node_Id);
    	}
        
        Tmessage = Tmessage + 1;
    }

    protected void Reply_Received(SimEvent event) {					//Token_received
        int NodeId;
        NodeId = (int) event.token.attr[2];
        NEAR[NodeId] = NodeId;
        Token_Node = NodeId;
        req_crit_sect[Token_Node] = false;
        
        //if(Local_Queue[Token_Node].peek() != null){
        	RequestNode Node = Local_Queue[Token_Node].element();

	        if(Node.ID == Token_Node){
	        	event = new SimEvent();
	            event.id = EXECUTE_CS;
	            event.token.attr[0] = Token_Node;
	            Sim.schedule(event, CSTime);
	            exe_crit_sect[Token_Node] = true;
	        }
	        else{
	        	Local_Queue[NodeId].remove();
	        	//轉移Token
	        	SimEvent event1;
	            event1 = new SimEvent();
	            // attr[0] 記下傳送 request_receive 的 node
	            event1.token.attr[0] = NodeId;
	            // attr[2] 記下接收此 message 的 node
	            event1.token.attr[2] = Node.ID;
	            event1.id = REPLY_RECEIVED;
	            Sim.schedule(event1, Trans_Time);
	            NEAR[NodeId] = Node.ID;
	    		if(Local_Queue[NodeId].size() > 0)
	    			Send_Requests(NodeId);
	        }
	        Tmessage = Tmessage + 1;
        //}
        

    }

    protected void Execute_CS(SimEvent event) {

        // 從 event queue 中取出 token，設定 token 所記錄的 NodeId
        int NodeId = (int) event.token.attr[0];

        // 從Local_Queue 取出 process 的資料
        //if(Local_Queue[NodeId].peek() != null)
        RequestNode p = Local_Queue[NodeId].poll();

        // 記錄一個 process 從允許參與競爭進入 critical section 到可以進入
        // critical section 所等待的時間，並將此時間加入 response_R2 做統計用

        response_R2.add(Sim.time() - p.TIME);


        // 號碼為 NodeId 的 node 中的第一個 process 已經順利進入 critical section
        // 所以將它從 node 的 waiting queue 移除
        //if(Wait_Queue[NodeId].peek() != null)
        	Wait_Queue[NodeId].remove();

        // 號碼為 NodeId 的 node 暫時標記為沒有要參與競爭 critical section
        exe_crit_sect[NodeId] = false;

        if(Local_Queue[NodeId].size() != 0){
        	RequestNode NewTokenNode = Local_Queue[NodeId].poll();
        	//轉移Token
        	SimEvent event1;
            event1 = new SimEvent();
            // attr[0] 記下傳送 request_receive 的 node
            event1.token.attr[0] = NodeId;
            // attr[2] 記下接收此 message 的 node
            event1.token.attr[2] = NewTokenNode.ID;
            event1.id = REPLY_RECEIVED;
            Sim.schedule(event1, Trans_Time);
            NEAR[NodeId] = NewTokenNode.ID;
            
            if(Wait_Queue[NodeId].peek()!=null){
            	RequestNode Process = Wait_Queue[NodeId].peek();
            	Process.TIME = Sim.time();
            	Local_Queue[NodeId].add(Process);
        	}        	
        	if(Local_Queue[NodeId].size()>0){
    			Send_Requests(NodeId);
    		}       	
        }
        else{
        	if(Wait_Queue[NodeId].peek()!=null){
        		RequestNode Process = Wait_Queue[NodeId].peek();
        		Process.TIME = Sim.time();
        		Local_Queue[NodeId].add(Process);
        		event = new SimEvent();
                event.id = EXECUTE_CS;
                event.token.attr[0] = NodeId;
                Sim.schedule(event, CSTime);
                req_crit_sect[NodeId] = true;
        	}
        }
        
        
        Total = Total + Tmessage;
        Tmessage = 0;
    }
    
}

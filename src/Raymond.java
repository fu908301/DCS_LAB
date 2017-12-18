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

    // �N�Y�� nodeNumbers �ӻ��A�O�� nodeNumbers ���� processes
    private final Queue<RequestNode>[] Wait_Queue = new Queue[MAX_NODE];	//�D�n�O��NODE�������ͪ�process
    private final Queue<RequestNode>[] Local_Queue = new Queue[MAX_NODE];	//�D�n�O���LNODE���ͪ�request

    // �N�Y�� nodeNumbers �ӻ��A�O�_�n�D�i�J critical section
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

            // id �O�� event �����A
            event.id = PROCESS_ARRIVAL;

            // attr[0] �O������ process �� node �����X
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
        // attr[0] �O�� node �����X
        int NodeId = (int) event.token.attr[0];

        if(Wait_Queue[NodeId].peek() != null){
        	RequestNode Node = new RequestNode();
        	Node.ID = NodeId;
        	Node.TIME = Sim.time();
        	Wait_Queue[NodeId].add(Node);
        }
        else{	//waiting queue�̭��S���F��        	        	
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

        // �I�sposson��Ƴ]�w���ͤU�@�ӷs�� process �ҭn���𪺮ɶ�
        double new_process_delay_time = Sim.poisson(arrival_rate);

        // �N���ͷs�� process �ҩ��𪺮ɶ��[�J�έp
        ran_var_stat.add(new_process_delay_time);

        // ���ثe�� node ���ͤ@�ӷs�� process
        event = new SimEvent();
        event.id = PROCESS_ARRIVAL;
        event.token.attr[0] = NodeId;
        Sim.schedule(event, new_process_delay_time);
    }

    void Send_Requests(int Source_Node) {

        // �N�� node �n�ѻP�v���i�J critical section �����x turn on
        req_crit_sect[Source_Node] = true;

        // �N message �ǰe�U�@�� node
        SimEvent event = new SimEvent();
        event = new SimEvent();
        // attr[0] �O�U�ǰe request_receive �� node
        event.token.attr[0] = Source_Node;
        // attr[2] �O�U������ message �� node
        event.token.attr[2] = NEAR[Source_Node];
        event.id = REQUEST_RECEIVED;
        Sim.schedule(event, Trans_Time);
        
    }

    protected void Request_Received(SimEvent event) {
        // �e�X message �� node
        int Send_Node_Id;

        // ���� message �� node
        int Rec_Node_Id;

        // attr[0] �O�U�ǰe request_receive �� node
        Send_Node_Id = (int) event.token.attr[0];

        // attr[2] �O�U������ message �� node
        Rec_Node_Id = (int) event.token.attr[2];

        RequestNode Node = new RequestNode();
        Node.ID = Send_Node_Id;
        Node.TIME = Sim.time();
        Local_Queue[Rec_Node_Id].add(Node);
        
        if((NEAR[Rec_Node_Id] == Rec_Node_Id) && ( !((req_crit_sect[Rec_Node_Id] ) || exe_crit_sect[Rec_Node_Id]))){	
        	RequestNode NewTokenNode = Local_Queue[Rec_Node_Id].poll();
    		//�ಾToken
        	SimEvent event1;;
            event1 = new SimEvent();
            // attr[0] �O�U�ǰe request_receive �� node
            event1.token.attr[0] = Rec_Node_Id;
            // attr[2] �O�U������ message �� node
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
	        	//�ಾToken
	        	SimEvent event1;
	            event1 = new SimEvent();
	            // attr[0] �O�U�ǰe request_receive �� node
	            event1.token.attr[0] = NodeId;
	            // attr[2] �O�U������ message �� node
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

        // �q event queue �����X token�A�]�w token �ҰO���� NodeId
        int NodeId = (int) event.token.attr[0];

        // �qLocal_Queue ���X process �����
        //if(Local_Queue[NodeId].peek() != null)
        RequestNode p = Local_Queue[NodeId].poll();

        // �O���@�� process �q���\�ѻP�v���i�J critical section ��i�H�i�J
        // critical section �ҵ��ݪ��ɶ��A�ñN���ɶ��[�J response_R2 ���έp��

        response_R2.add(Sim.time() - p.TIME);


        // ���X�� NodeId �� node �����Ĥ@�� process �w�g���Q�i�J critical section
        // �ҥH�N���q node �� waiting queue ����
        //if(Wait_Queue[NodeId].peek() != null)
        	Wait_Queue[NodeId].remove();

        // ���X�� NodeId �� node �ȮɼаO���S���n�ѻP�v�� critical section
        exe_crit_sect[NodeId] = false;

        if(Local_Queue[NodeId].size() != 0){
        	RequestNode NewTokenNode = Local_Queue[NodeId].poll();
        	//�ಾToken
        	SimEvent event1;
            event1 = new SimEvent();
            // attr[0] �O�U�ǰe request_receive �� node
            event1.token.attr[0] = NodeId;
            // attr[2] �O�U������ message �� node
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

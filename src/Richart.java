/**
 * Ricart and Agrawala's Algoritm simulaton
 * ----------------------------------------
 * Student : �ť��w
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

    // �N�Y�� nodeNumbers �ӻ��A�ثe�ǰe�X�h�� timestamp�ACs = Hs + 1
    private final int[] our_sequence_no = new int[MAX_NODE];

    // �N�Y�� nodeNumbers �ӻ��A�ثe�Ҫ��̤j�� timestamp
    private final int[] high_sequence_no = new int[MAX_NODE];

    // �N�Y�� nodeNumbers �ӻ��A�ݭn���ݪ� reply �Ӽ�
    private final int[] outstand_reply_count = new int[MAX_NODE];

    // �N�Y�� nodeNumbers �ӻ��A�O���n�^���� nodeNumbers reply
    private final boolean[][] reply_deferred = new boolean[MAX_NODE][MAX_NODE];

    // �N�Y�� nodeNumbers �ӻ��A�O�� nodeNumbers ���� processes
    private final Queue<ProcessTimeInfo>[] wait_queue = new Queue[MAX_NODE];

    // �N�Y�� nodeNumbers �ӻ��A�O�_�n�D�i�J critical section
    private final boolean[] req_crit_sect = new boolean[MAX_NODE];

    //�O���W�@���i�J critical section ���ɶ�
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

            // id �O�� event �����A
            event.id = PROCESS_ARRIVAL;

            // attr[0] �O������ process �� node �����X
            event.token.attr[0] = i;

            Sim.schedule(event, Sim.expntl(arrival_rate));
        }
    }

    protected void Process_Arrival(SimEvent event) {
        // attr[0] �O�� node �����X
        int NodeId = (int) event.token.attr[0];

        // �p�G waiting queue �O�Ū��B node �S���n�D�i�J critical section
        // �h�N�s���ͪ� process ��J waiting queue�A�å� node �e�X request
        // �n�D�i�J critical section
        if ((wait_queue[NodeId].peek() == null) && (!req_crit_sect[NodeId])) {
            wait_queue[NodeId].add(new ProcessTimeInfo(Sim.time()));
            Send_Requests(NodeId);
        }
        // �p�G waiting queue ���O�Ū��A��� node ���ܤ֦��@�� process �b
        // �n�D�i�J critical section�A�h�N�ثe�s���ͪ� process ��J
        // waiting queue
        else
            wait_queue[NodeId].add(new ProcessTimeInfo(Sim.time()));

        // �I�s exponential ��Ƴ]�w���ͤU�@�ӷs�� process �ҭn���𪺮ɶ�
        double new_process_delay_time = Sim.expntl(arrival_rate);

        // �N���ͷs�� process �ҩ��𪺮ɶ��[�J�έp
        ran_var_stat.add(new_process_delay_time);

        // ���ثe�� node ���ͤ@�ӷs�� process
        event = new SimEvent();
        event.id = PROCESS_ARRIVAL;
        event.token.attr[0] = NodeId;
        Sim.schedule(event, new_process_delay_time);
    }

    void Send_Requests(int Source_Node) {

        // �]�w�ثe process �}�l�ѻP�v���i�J critical section ���ɶ�
        (wait_queue[Source_Node].peek()).setCriticalSectionAdmissionTime(Sim.time());

        // �O�U�ثe�� node �ݭn���ݨ�L node �Ǧ^ reply ���`��
        outstand_reply_count[Source_Node] = nodeNumbers - 1;

        // �N�� node �n�ѻP�v���i�J critical section �����x turn on
        req_crit_sect[Source_Node] = true;

        // �]�w�ثe�n�ǰe�X�h�P�O�H�v���� timestamp�ACs = Hs + 1
        our_sequence_no[Source_Node] = high_sequence_no[Source_Node] + 1;

        // �N message �ǰe���ۤv�H�~�� node
        SimEvent event = new SimEvent();
        for (int receive = 0; receive < nodeNumbers; receive++)
            if (receive != Source_Node) {
                // attr[0] �O�U�ǰe request_receive �� node
                event.token.attr[0] = Source_Node;
                // attr[1] �O�U timestamp
                event.token.attr[1] = our_sequence_no[Source_Node];
                // attr[2] �O�U������ message �� node
                event.token.attr[2] = receive;
                event.id = REQUEST_RECEIVED;
                Sim.schedule(event, Trans_Time);
            }
    }

    protected void Request_Received(SimEvent event) {
        // �e�X message �� node
        int Send_Node_Id;

        // �e�X message �� node �Ҷǰe�Ӫ� timestamp
        int Send_Node_Seq_No;

        // ���� message �� node
        int Rec_Node_Id;

        // attr[0] �O�U�ǰe request_receive �� node
        Send_Node_Id = (int) event.token.attr[0];
        // attr[1] �O�U timestamp
        Send_Node_Seq_No = (int) event.token.attr[1];
        // attr[2] �O�U������ message �� node
        Rec_Node_Id = (int) event.token.attr[2];

        // �ǥѶǰe�Ӫ� timestamp ����ɶ����P�B
        high_sequence_no[Rec_Node_Id] =
                findMaximum(high_sequence_no[Rec_Node_Id], Send_Node_Seq_No);

        // �P�O�O�_�n����ǰe reply �T��
        // �p�G�ǰe�Ӫ� timestamp ��ثe�ۤv�ҰO���� timestamp �Ӫ��j�A�h�n����
        boolean Defer_It, first_test, second_test;
        first_test = (Send_Node_Seq_No > our_sequence_no[Rec_Node_Id]);
        // �p�G timestamp �@�ˤj�A�h�Q�� node ���X���j�p�P�_�O�_����A���X�p���u���v����
        second_test = ((Send_Node_Seq_No == our_sequence_no[Rec_Node_Id])
                && (Send_Node_Id > Rec_Node_Id));
        Defer_It = (req_crit_sect[Rec_Node_Id] && (first_test || second_test));

        if (Defer_It)
            reply_deferred[Rec_Node_Id][Send_Node_Id] = true;
        else {
            // �i�D���X�� Send_Node_Id �� node ������@�� reply message
            event = new SimEvent();
            event.id = REPLY_RECEIVED;
            event.token.attr[0] = Send_Node_Id;
            Sim.schedule(event, Trans_Time);
        }
    }

    protected void Reply_Received(SimEvent event) {
        int NodeId;
        NodeId = (int) event.token.attr[0];

        // ���� reply ���Ӽƴ 0�A��ܥi�H�i�J critical section
        if (--outstand_reply_count[NodeId] == 0) {
            event = new SimEvent();
            event.id = EXECUTE_CS;
            event.token.attr[0] = NodeId;
            Sim.schedule(event, CSTime);
        }
    }

    protected void Execute_CS(SimEvent event) {

        // �q event queue �����X token�A�]�w token �ҰO���� NodeId
        int NodeId = (int) event.token.attr[0];

        // �q waiting queue ���X process �����
        ProcessTimeInfo p = wait_queue[NodeId].element();

        // �O���@�� process �q���ͨ�i�H�i�J critical section �ҵ��ݪ��ɶ��A�ñN���ɶ�
        // �[�J reponse_R1 ���έp��
        response_R1.add(Sim.time() - p.getArrivalTime());

        // �O���@�� process �q���\�ѻP�v���i�J critical section ��i�H�i�J
        // critical section �ҵ��ݪ��ɶ��A�ñN���ɶ��[�J response_R2 ���έp��
        response_R2.add(Sim.time() - p.getCriticalSectionAdmissionTime());

        // �O�� critical section �Q�e�@�� process �i�J���ɶ��P�ثe process �i�J���ɶ����t
        delay_time_D.add(Sim.time() - save_time);

        // �O�U�ثe process �n�i�J critical section ���ɶ��A�@����U�@�� process �n�i�J
        // critical section �ɡA delay_time_D �һݭn����T
        save_time = Sim.time();

        // ���X�� NodeId �� node �����Ĥ@�� process �w�g���Q�i�J critical section
        // �ҥH�N���q node �� waiting queue ����
        wait_queue[NodeId].remove();

        // ���X�� NodeId �� node �ȮɼаO���S���n�ѻP�v�� critical section
        req_crit_sect[NodeId] = false;

        for (int RecNodeId = 0; RecNodeId < nodeNumbers; RecNodeId++)
            // �N���� reply �Ǧ^����L�� node
            if (reply_deferred[NodeId][RecNodeId]) {
                reply_deferred[NodeId][RecNodeId] = false;
                event = new SimEvent();
                event.id = REPLY_RECEIVED;
                event.token.attr[0] = RecNodeId;
                Sim.schedule(event, Trans_Time);
            }

        // �p�G�ثe�� node �� waiting queue ���٦� process �Q�n�i�J critical section
        // �h�o�X�T���ѻP�v��
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

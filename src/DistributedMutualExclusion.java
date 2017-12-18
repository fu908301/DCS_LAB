import simpack.Const;
import simpack.Sim;
import simpack.SimEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Raymond's Algoritm simulaton
 * ----------------------------------------
 * Student :�ť��w
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: DistributedMutualExclusion.java
 */
public abstract class DistributedMutualExclusion {

	static int CASE_NUMBER; //�𪺧Ϊ�
	static int K;//after ? CS executions, see output
	double Tmessage = 0, Total = 0;
    /**
     * state constant�A�ΨӪ�� nodeNumbers ���ͤ@�ӷs�� process
     */
    static final int PROCESS_ARRIVAL = 1;

    /**
     * state constant�A�ΨӪ�� nodeNumbers ����@�� request
     */
    static final int REQUEST_RECEIVED = 2;

    /**
     * state constant�A�ΨӪ�� nodeNumbers �����L nodes �Ǧ^�� reply
     */
    static final int REPLY_RECEIVED = 3;

    /**
     * state constant�A�ΨӪ�� process �i�J critical section
     */
    static final int EXECUTE_CS = 4;

    /**
     * ���\�ѻP�����̦h�� nodeNumbers �Ӽ�
     */
    static final int MAX_NODE = 25;

    /**
     * ���� process ���W�v
     */
    double arrival_rate;

    /**
     * process ���� critical section �һݪ��ɶ�
     */
    double CSTime;

    /**
     * �q�@�� nodeNumbers �ǰe�@�� message �� �t�@�� nodeNumbers �һݭn���ɶ�
     */
    double Trans_Time;

    /**
     * simulation �ɪ� nodes ���`��
     */
    int nodeNumbers;

    /**
     * process �q���ͨ�i�H�i�J critical section �ҵ��ݪ��ɶ����έp��T
     */
    final Statistics response_R1 = new Statistics();

    /**
     * process �q���\�ѻP�v���i�J critical section ��i�H�i�J critical section
     * �ҵ��ݪ��ɶ����έp��T
     */
    final Statistics response_R2 = new Statistics();
    final Statistics ran_var_stat = new Statistics();

    /**
     * �O�� critical section �Q�e�@�� process �i�J���ɶ��P�ثe process �i�J���ɶ����t���έp��T
     */
    final Statistics delay_time_D = new Statistics();


    // �O���i�J�L critical section �� processes ���Ӽ�
    private int departures;

    // �ݭn���檺 processes ���`��
    private int num_left;

    protected abstract void initial();

    protected abstract void Process_Arrival(SimEvent event);

    protected abstract void Request_Received(SimEvent event);

    protected abstract void Reply_Received(SimEvent event);

    protected abstract void Execute_CS(SimEvent event);

    /**
     * �p�G�l���O����L�ѼƭnŪ�J�Aoverrides this method
     */
    void getSpecificParameters() {
    }

    /**
     * �p�G�l���O����L�Ѽƭn��X�Aoverrides this method
     */
    void displaySpecificParameters() {
    }

    /**
     * �p�G�l���O����L simulation results �n��X�Aoverrides this method
     */
    void showSpecificResults() {
    }

    protected DistributedMutualExclusion() {
        Sim.init(0, Const.HEAP);
    }

    /**
     * �Ұʺt��k
     */
    public void run() {
        getParameters();
        displayParameters();
        runSimulation();
        showSimulationResults();
    }

    private void runSimulation() {
        // �}�l���� simulation
        SimEvent event;
        int temp=0,i=1;
        initial();
        while (departures < num_left) {
            event = Sim.next_event(0.0, Const.ASYNC);
            switch (event.id) {
                case PROCESS_ARRIVAL:
                    Process_Arrival(event);
                    break;
                case REQUEST_RECEIVED:
                    Request_Received(event);
                    break;
                case REPLY_RECEIVED:
                    Reply_Received(event);
                    break;
                case EXECUTE_CS:
                    Execute_CS(event);
                    departures++;
                    break;
            }
        	if(((departures % K)==0) && (departures >= K) && (temp != departures)){
        		temp = departures;
        		System.out.println(" For " + K*i + "��, Total = " + Total+ ", average = " + Total/departures);
        		i++;
        	}
        }
    }

    private void showSimulationResults() {
        // simulation ������L�X�έp��T

        // �L�X R1 ����T
        // �Ҧ� processes �q���ͨ�i�H�i�J critical section
        // �ҵ��ݪ��ɶ����έp��T
        //System.out.print("\nR1:\n");
        //System.out.println("Mean: " + response_R1.getMean());
        //System.out.println(" Max: " + response_R1.getMaximum());
        //System.out.println(" Min: " + response_R1.getMinimum());
        //System.out.println(" Variance: " + response_R1.getVariance());

        // �L�X R2 ����T:
        // �Ҧ� processes �q���\�ѻP�v���i�J critical section ��i�H�i�J
        // critical section �ҵ��ݪ��ɶ����έp��T
        //System.out.print("\nR2:\n");
        System.out.println("\n R2 Mean: " + response_R2.getMean());
        //System.out.println(" Max: " + response_R2.getMaximum());
        //System.out.println(" Min: " + response_R2.getMinimum());
        //System.out.println(" Variance: " + response_R2.getVariance());

        // �L�X delay_time_D ����T
        // �e�@�� process �i�J critical section ���ɶ��P�ثe process �i�J���ɶ�
        // ���t���έp��T
        //System.out.print("\nD:\n");
        //System.out.println("Mean: " + delay_time_D.getMean());
        //System.out.println(" Max: " + delay_time_D.getMaximum());
        //System.out.println(" Min: " + delay_time_D.getMinimum());
        //System.out.println(" Variance: " + delay_time_D.getVariance());

        // �L�X���ͨC�@�� process �ҩ���ɶ���������
        //System.out.println("\nMean of Ran Var: " + ran_var_stat.getMean());
        
        System.out.println(" Average message: " + Total/departures);
        System.out.println(" Total message: " + Total);

        // ��X�P�l���O�������ƾ�
        showSpecificResults();
    }



    private void displayParameters() {
        System.out.print("\nProcessing, please wait...\n");

        // �L�X�ϥΪ̿�J����T
        System.out.println("Parameters for this run:");
        System.out.println("after # of CS executons, see output:" + K);
        System.out.println(" # of sites: " + nodeNumbers);
        System.out.println(" Arrival Rate: " + arrival_rate);
        System.out.println(" CSTime: " + CSTime);
        System.out.println(" Max Process: " + num_left);
        System.out.println(" Transmit Time: " + Trans_Time);
        switch(CASE_NUMBER){
        case 1:
        	System.out.println(" Case: line");
        	break;
        case 2:
        	System.out.println(" Case: star");
        	break;
        case 3:
        	System.out.println(" Case: R-star");
        	break;
        default:
        	break;
        }
        System.out.println();
        // ��X�P�l���O�������Ѽ�
        displaySpecificParameters();
    }


    private void getParameters() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("\nEnter the simulation parameters\n\n");
        
        /*
        System.out.print("After ? CS executions, see output: ");
        try {
            K = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
        */
        /*
        // Ū�� nodeNumbers �Ӽ�
        System.out.print("Enter the # of sites (<=50): ");
        try {
            nodeNumbers = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }

        // �p�G�ϥΪ̿�J�� nodeNumbers �ӼƶW�L�{���w�]���̤j�ȡA�h�ШϥΪ̭��s��J
        try {
            while (nodeNumbers > MAX_NODE) {
                System.out.print("\nPlease re-enter the # of sites (<=50): ");
                nodeNumbers = Integer.valueOf(in.readLine());
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        */
        
        // Ū���C�@�� process ���ͪ��W�v
        System.out.print("\nEnter the arrival rate: ");
        try {
            arrival_rate = Double.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
		
        /*
        // Ū�� process ���� critical section �һݪ��ɶ�
        System.out.print("\nEnter the time taken for executing CS: ");
        try {
            CSTime = Double.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
         */
        /*
        // Ū���|�i�J critical section �� processes �`��
        System.out.print("\nEnter the max # of processes: ");
        try {
            num_left = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
		*/
        /*
        // Ū���ǰe�T������L nodeNumbers �ɩһݩ��𪺮ɶ�
        System.out.print("\nEnter the transmission time: ");
        try {
            Trans_Time = Double.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
		*/
        
        // ��ܾ𪺧Ϊ�
        System.out.print("\nEntter the number of case (1=line,2=star,3=R-star) you want: ");
        try {
            CASE_NUMBER = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
        
        //�ѼƳ]�w
        K = 500;			//after ? CS executions, see output
        nodeNumbers = 21;	//node��
        //arrival_rate = 1;//rate
        CSTime = 0.01;		//CS�ɶ�
        num_left = 5000;	//processes��
        Trans_Time = 0.1;	//transmission time
        //CASE_NUMBER = 3;	//�𪺧Ϊ�
        
        // Ū���P�l���O�������Ѽ�
        getSpecificParameters();
    }
}

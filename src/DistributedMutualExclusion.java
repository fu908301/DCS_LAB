import simpack.Const;
import simpack.Sim;
import simpack.SimEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Raymond's Algoritm simulaton
 * ----------------------------------------
 * Student :傅正安
 * Date    : 2017/12/18
 * Major   : Distributed System
 * Filename: DistributedMutualExclusion.java
 */
public abstract class DistributedMutualExclusion {

	static int CASE_NUMBER; //樹的形狀
	static int K;//after ? CS executions, see output
	double Tmessage = 0, Total = 0;
    /**
     * state constant，用來表示 nodeNumbers 產生一個新的 process
     */
    static final int PROCESS_ARRIVAL = 1;

    /**
     * state constant，用來表示 nodeNumbers 收到一個 request
     */
    static final int REQUEST_RECEIVED = 2;

    /**
     * state constant，用來表示 nodeNumbers 收到其他 nodes 傳回的 reply
     */
    static final int REPLY_RECEIVED = 3;

    /**
     * state constant，用來表示 process 進入 critical section
     */
    static final int EXECUTE_CS = 4;

    /**
     * 允許參與模擬最多的 nodeNumbers 個數
     */
    static final int MAX_NODE = 25;

    /**
     * 產生 process 的頻率
     */
    double arrival_rate;

    /**
     * process 佔用 critical section 所需的時間
     */
    double CSTime;

    /**
     * 從一個 nodeNumbers 傳送一個 message 到 另一個 nodeNumbers 所需要的時間
     */
    double Trans_Time;

    /**
     * simulation 時的 nodes 的總數
     */
    int nodeNumbers;

    /**
     * process 從產生到可以進入 critical section 所等待的時間的統計資訊
     */
    final Statistics response_R1 = new Statistics();

    /**
     * process 從允許參與競爭進入 critical section 到可以進入 critical section
     * 所等待的時間的統計資訊
     */
    final Statistics response_R2 = new Statistics();
    final Statistics ran_var_stat = new Statistics();

    /**
     * 記錄 critical section 被前一個 process 進入的時間與目前 process 進入的時間的差的統計資訊
     */
    final Statistics delay_time_D = new Statistics();


    // 記錄進入過 critical section 的 processes 的個數
    private int departures;

    // 需要執行的 processes 的總數
    private int num_left;

    protected abstract void initial();

    protected abstract void Process_Arrival(SimEvent event);

    protected abstract void Request_Received(SimEvent event);

    protected abstract void Reply_Received(SimEvent event);

    protected abstract void Execute_CS(SimEvent event);

    /**
     * 如果子類別有其他參數要讀入，overrides this method
     */
    void getSpecificParameters() {
    }

    /**
     * 如果子類別有其他參數要輸出，overrides this method
     */
    void displaySpecificParameters() {
    }

    /**
     * 如果子類別有其他 simulation results 要輸出，overrides this method
     */
    void showSpecificResults() {
    }

    protected DistributedMutualExclusion() {
        Sim.init(0, Const.HEAP);
    }

    /**
     * 啟動演算法
     */
    public void run() {
        getParameters();
        displayParameters();
        runSimulation();
        showSimulationResults();
    }

    private void runSimulation() {
        // 開始執行 simulation
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
        		System.out.println(" For " + K*i + "次, Total = " + Total+ ", average = " + Total/departures);
        		i++;
        	}
        }
    }

    private void showSimulationResults() {
        // simulation 結束後印出統計資訊

        // 印出 R1 的資訊
        // 所有 processes 從產生到可以進入 critical section
        // 所等待的時間之統計資訊
        //System.out.print("\nR1:\n");
        //System.out.println("Mean: " + response_R1.getMean());
        //System.out.println(" Max: " + response_R1.getMaximum());
        //System.out.println(" Min: " + response_R1.getMinimum());
        //System.out.println(" Variance: " + response_R1.getVariance());

        // 印出 R2 的資訊:
        // 所有 processes 從允許參與競爭進入 critical section 到可以進入
        // critical section 所等待的時間之統計資訊
        //System.out.print("\nR2:\n");
        System.out.println("\n R2 Mean: " + response_R2.getMean());
        //System.out.println(" Max: " + response_R2.getMaximum());
        //System.out.println(" Min: " + response_R2.getMinimum());
        //System.out.println(" Variance: " + response_R2.getVariance());

        // 印出 delay_time_D 的資訊
        // 前一個 process 進入 critical section 的時間與目前 process 進入的時間
        // 的差之統計資訊
        //System.out.print("\nD:\n");
        //System.out.println("Mean: " + delay_time_D.getMean());
        //System.out.println(" Max: " + delay_time_D.getMaximum());
        //System.out.println(" Min: " + delay_time_D.getMinimum());
        //System.out.println(" Variance: " + delay_time_D.getVariance());

        // 印出產生每一個 process 所延遲時間的平均值
        //System.out.println("\nMean of Ran Var: " + ran_var_stat.getMean());
        
        System.out.println(" Average message: " + Total/departures);
        System.out.println(" Total message: " + Total);

        // 輸出與子類別有關的數據
        showSpecificResults();
    }



    private void displayParameters() {
        System.out.print("\nProcessing, please wait...\n");

        // 印出使用者輸入的資訊
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
        // 輸出與子類別有關的參數
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
        // 讀取 nodeNumbers 個數
        System.out.print("Enter the # of sites (<=50): ");
        try {
            nodeNumbers = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }

        // 如果使用者輸入的 nodeNumbers 個數超過程式預設的最大值，則請使用者重新輸入
        try {
            while (nodeNumbers > MAX_NODE) {
                System.out.print("\nPlease re-enter the # of sites (<=50): ");
                nodeNumbers = Integer.valueOf(in.readLine());
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        */
        
        // 讀取每一個 process 產生的頻率
        System.out.print("\nEnter the arrival rate: ");
        try {
            arrival_rate = Double.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
		
        /*
        // 讀取 process 佔用 critical section 所需的時間
        System.out.print("\nEnter the time taken for executing CS: ");
        try {
            CSTime = Double.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
         */
        /*
        // 讀取會進入 critical section 的 processes 總數
        System.out.print("\nEnter the max # of processes: ");
        try {
            num_left = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
		*/
        /*
        // 讀取傳送訊息給其他 nodeNumbers 時所需延遲的時間
        System.out.print("\nEnter the transmission time: ");
        try {
            Trans_Time = Double.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
		*/
        
        // 選擇樹的形狀
        System.out.print("\nEntter the number of case (1=line,2=star,3=R-star) you want: ");
        try {
            CASE_NUMBER = Integer.valueOf(in.readLine());
        } catch (IOException e) {
            System.err.println(e);
        }
        
        //參數設定
        K = 500;			//after ? CS executions, see output
        nodeNumbers = 21;	//node數
        //arrival_rate = 1;//rate
        CSTime = 0.01;		//CS時間
        num_left = 5000;	//processes數
        Trans_Time = 0.1;	//transmission time
        //CASE_NUMBER = 3;	//樹的形狀
        
        // 讀取與子類別有關的參數
        getSpecificParameters();
    }
}

package simpack;

public class STATE {
    public static double time;
    public static SimEvent[] future_events;
    public static int future_events_length;
    public static FACILITY_STATE[] facility;
    public static int facility_length;
    public static int heap_count;
    public static SimEvent[] heap;

    public STATE() {
        future_events = new SimEvent[200];
        facility = new FACILITY_STATE[100];
        heap = new SimEvent[Const.HEAP_SIZE];
    }

    public static void print() {
        LIST_ list_ptr = new LIST_();
        NODE node = new NODE();
        int i, j;
        char any_char;


        STATE state = Sim.state();

        System.out.println("## TIME: " + state.time);
        System.out.println();

        if (Sim.event_list_type == Const.LINKED) {
            System.out.println("## EVENT LIST");
            list_ptr = Sim.event_list;
            System.out.print("      ");
            for (i = 0; i < state.future_events_length; i++) {
                System.out.print("  ");
                System.out.print("+----+");
            }
            System.out.println();

            System.out.print("Token ");
            for (i = 0; i < state.future_events_length; i++) {
                System.out.print("  ");
                System.out.print("|   " + (int) state.future_events[i].token.attr[0] + "|");
            }
            System.out.println();

            System.out.print("Time  ");
            for (i = 0; i < state.future_events_length; i++) {
                System.out.print("<=");
                System.out.print("|   " + (int) state.future_events[i].time + "|");
            }
            System.out.println();

            System.out.print("Event ");
            for (i = 0; i < state.future_events_length; i++) {
                System.out.print("  ");
                System.out.print("|   " + (int) state.future_events[i].id + "|");
            }
            System.out.println();

            System.out.print("      ");
            for (i = 0; i < state.future_events_length; i++) {
                System.out.print("  ");
                System.out.print("+----+");
            }
            System.out.println();
        } else {
            System.out.println("## PRIORITY QUEUE");
            Sim.print_heap();
        }

        for (i = 1; i <= state.facility_length; i++) {
            System.out.println("## FACILITY " + i + ": (" + state.facility[i].name + "), " +
                    state.facility[i].total_servers + " Server(s), " + state.facility[i].busy_servers + " Busy.");
            System.out.print("Server(s): ");
            for (j = 1; j <= state.facility[i].total_servers; j++)
                System.out.print("(" + j + ") TK " + state.facility[i].server_info[j][0] + " PR " + state.facility[i].server_info[j][1]);
            System.out.println();

            System.out.print("         ");
            node = list_ptr.front;
            for (j = 0; j < state.facility[i].queue_length; j++) {
                System.out.print("  ");
                System.out.print("+----+");
            }
            System.out.println();

            System.out.print("Token    ");
            for (j = 0; j < state.facility[i].queue_length; j++) {
                System.out.print("  ");
                System.out.print("|   " + (int) state.facility[i].queue[j].token.attr[0] + "|");
            }
            System.out.println();

            System.out.print("Time     ");
            for (j = 0; j < state.facility[i].queue_length; j++) {
                System.out.print("<=");
                System.out.print("|   " + (int) state.facility[i].queue[j].time + "|");
            }
            System.out.println();

            System.out.print("Event    ");
            for (j = 0; j < state.facility[i].queue_length; j++) {
                System.out.print("  ");
                System.out.print("|   " + (int) state.facility[i].queue[j].id + "|");
            }
            System.out.println();

            System.out.print("Priority ");
            for (j = 0; j < state.facility[i].queue_length; j++) {
                System.out.print("  ");
                System.out.print("|   " + (int) state.facility[i].queue[j].priority + "|");
            }
            System.out.println();

            System.out.print("         ");
            for (j = 0; j < state.facility[i].queue_length; j++) {
                System.out.print("  ");
                System.out.print("+----+");
            }
            System.out.println();
        }
    }

}
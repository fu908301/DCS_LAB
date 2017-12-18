package simpack;

import java.util.ArrayList;
import java.util.List;

class NODE {
    SimEvent event;
    NODE next;

    NODE() {
        event = new SimEvent();
    }
}

class LIST_ {
    NODE front;
    int size;

    LIST_() {
        front = new NODE();
    }
}

class tokenstruct {
    int id;
    double time;
    double first_arg;
    double second_arg;
}

public class Sim {

    public static List<Integer> current_event_id_list = new ArrayList<Integer>();
    public static STATE state_tmp = new STATE();
    public static int calfirstsub, nbuckets, calqsize, lastbucket, calresize_enable;
    public static double caltop_threshold, calbot_threshold, lastprio;
    public static double buckettop, calendar_width;
    public static tokenstruct[] token_list = new tokenstruct[Const.MAX_TOKENS + 1];
    public static double current_time, last_event_time;
    public static double total_token_time;
    public static int current_event_id, facilities, arrivals, completions;
    public static int tokens_in_system, trace_flag, trace_type, heap_count;
    public static int event_list_type, remove_duplicates;
    public static String current_operation;
    public static LIST_ event_list = new LIST_();
    public static SimEvent[] heap = new SimEvent[Const.HEAP_SIZE];
    public static FACILITY[] facility = new FACILITY[Const.MAX_FACILITIES];

    public static double utilization, idle, arrival_rate, throughput, total_sim_time;
    public static double total_busy_time;
    public static double total_utilization;
    public static double mean_service_time, mean_num_tokens, mean_residence_time;

    public static double[] In = {0,
            1973272912, 747177549, 20464843, 640830765, 1098742207,
            78126602, 84743774, 831312807, 124667236, 1172177002,
            1124933064, 1223960546, 1878892440, 1449793615, 553303732};

    public static int strm = 1;
    public static int rn_stream;

    public static void init(double set_time, int flags) {
        int i, type;
        int ranmark;
        type = flags & 15;
        ranmark = flags & 32;
        remove_duplicates = flags & 16;
        for (i = 0; i < Const.MAX_TOKENS; i++) token_list[i] = new tokenstruct();
        for (i = 0; i < Const.HEAP_SIZE; i++) heap[i] = new SimEvent();
        for (i = 0; i < Const.MAX_FACILITIES; i++) facility[i] = new FACILITY();
        for (i = 0; i < Const.MAX_TOKENS; i++) token_list[i].id = 99999;

        event_list_type = type;
        create_list(event_list);
        heap_count = 0;
        trace_flag = Const.OFF;
        current_time = set_time;
        last_event_time = current_time;
        facilities = 0;
        arrivals = 0;
        completions = 0;
        total_token_time = 0;
        tokens_in_system = 0;

        calresize_enable = Const.true_;
        rn_stream++;

        if (rn_stream > 15) rn_stream = 1;
    }

    public static void create_list(LIST_ tmp_list_ptr) {
        LIST_ list_ptr = new LIST_();
        list_ptr = tmp_list_ptr;
        list_ptr.front = null;
        list_ptr.size = 0;
    }

    public static void schedule(SimEvent event, double inter_time) {//(int event,double inter_time,TOKEN token)

        double event_time;
        SimEvent an_event = new SimEvent();

        int i, token_id;
        token_id = (int) event.token.attr[0] % Const.MAX_TOKENS;
        event_time = current_time + inter_time;
        if ((token_list[token_id].id != event.id) ||
                (token_list[token_id].time != event_time) ||
                (token_list[token_id].first_arg != event.token.attr[1]) ||
                (token_list[token_id].second_arg != event.token.attr[2])) {
            // If the same event with the same attributes exists, then not schedule
            token_list[token_id].id = event.id;
            token_list[token_id].time = event_time;
            token_list[token_id].first_arg = event.token.attr[1];
            token_list[token_id].second_arg = event.token.attr[2];
        }
        an_event.time = event_time;
        an_event.id = event.id;

        for (i = 0; i < Const.MAX_NUM_ATTR; i++) {
            an_event.token.attr[i] = event.token.attr[i];
        }
        switch (event_list_type) {
            case Const.LINKED:
                insert_list(event_list, an_event, Const.TIME_KEY);
                break;
            case Const.HEAP:
                heap_insert(an_event);
                break;
        }
    }

    public static void heap_insert(SimEvent tmp_event) {
        int parent, child;
        SimEvent event = new SimEvent();

        event_copy(event, tmp_event);

        heap_count++;
        heap[heap_count] = event;

        if (heap_count > 1) {
            child = heap_count;
            parent = child / 2;
            while ((heap[parent].time > heap[child].time) && (child > 1)) {
                heap_swap(heap[parent], heap[child]);
                child = parent;
                if (child > 1)
                    parent = child / 2;
            }
        }
    }

    public static void heap_swap(SimEvent tmp_event1, SimEvent tmp_event2) {
        SimEvent temp = new SimEvent();
        SimEvent event1 = new SimEvent();
        SimEvent event2 = new SimEvent();

        event1 = tmp_event1;
        event2 = tmp_event2;
        event_copy(temp, event1);
        event_copy(event1, event2);
        event_copy(event2, temp);
    }

    public static void heap_delete(int token_id, SimEvent tmp_event) {
        SimEvent event = new SimEvent();
        event = tmp_event;

        int i, j, parent, child;

        i = 1;
        while (heap[i].token.attr[0] != token_id) i++;

        heap_swap(heap[i], heap[heap_count]);
        event = heap[heap_count];
        heap_count--;

        heap_up_adjust(i);
        heap_down_adjust(i);
    }

    public static int heap_remove(SimEvent tmp_event_addr) {
        int parent, child;
        if (tmp_event_addr != null && heap_count > 0) {
            SimEvent event_addr = new SimEvent();
            event_addr = tmp_event_addr;
            event_copy(event_addr, heap[1]);
            heap_swap(heap[1], heap[heap_count]);
            heap_count--;
            heap_down_adjust(1);
            return 0;
        } else
            return -1;
    }

    /*    public static int heap_read(SimEvent tmp_event_addr) {
        ItemPointer event_addr = new ItemPointer();
        event_addr.ptr = tmp_event_addr;
	if (event_addr.ptr != null && heap_count > 0) {
	    event_copy(event_addr.ptr,heap[1]);
	    return 0;
	}
	else
	    return -1;
	    } */

    public static int heap_read(SimEvent tmp_event_addr) {
        if (tmp_event_addr != null && heap_count > 0) {
            SimEvent event_addr = new SimEvent();
            event_addr = tmp_event_addr;
            event_copy(event_addr, heap[1]);
            return 0;
        } else
            return -1;
    }

    public static void heap_up_adjust(int child) {
        int parent = child / 2;

        while (parent > 0) {
            if (heap[parent].time < heap[child].time) break;
            heap_swap(heap[parent], heap[child]);
            child = parent;
            parent /= 2;
        }
    }

    public static void heap_down_adjust(int parent) {
        int child;
        int temp_true = 1;

        while (temp_true == 1) {
            if (2 * parent > heap_count) {
                temp_true = 0;
                break;
            } else
                child = 2 * parent;

            if (child + 1 <= heap_count)
                if (heap[child + 1].time < heap[child].time)
                    child++;

            if (heap[parent].time < heap[child].time) {
                temp_true = 0;
                break;
            }

            heap_swap(heap[parent], heap[child]);
            parent = child;
        }
    }

    public static void print_heap() {
        int i;
        for (i = 1; i <= heap_count; i++)
            System.out.print(heap[i].time + " ");
        System.out.println();
    }

    public static int create_facility(String tmp_name, int num_servers) {
        int i;

        facilities++;
        create_list(facility[facilities].queue);
        facility[facilities].status = Const.FREE;
        facility[facilities].name = new String(tmp_name);
        facility[facilities].total_servers = num_servers;
        facility[facilities].busy_servers = 0;

        for (i = 1; i <= num_servers; i++) {
            facility[facilities].server_info[i][0] = 0;
            facility[facilities].server_info[i][1] = 0;
        }

        facility[facilities].preemptions = 0;
        facility[facilities].total_busy_time = 0.0;
        return (facilities);
    }

    public static List next_event_dup(double ptime, int mode) {
        List<SimEvent> return_list = new ArrayList<SimEvent>();
        NODE current_node = new NODE();
        LIST_ list_ptr = new LIST_();
        int i, status, j = 0, exit_loop = Const.false_;
        double vtime, compared_time = 0;

        SimEvent empty_event = new SimEvent();
        empty_event.time = -1.0;
        empty_event.id = -1;
        empty_event.priority = -1;

        list_ptr = event_list;
        current_node = list_ptr.front;

        if (current_node == null) {
            return_list.add(empty_event);
            return return_list;
        }

        while (exit_loop == Const.false_ && current_node != null) {
            SimEvent an_event = new SimEvent();
            switch (event_list_type) {
                case Const.LINKED:
                    status = list_read(event_list, an_event);
                    if (status == -1) {
                        return_list.add(empty_event);
                        return return_list;
                    }
                    break;
                case Const.HEAP:
                    status = heap_read(an_event);
                    if (status == -1) {
                        return_list.add(empty_event);
                        return return_list;
                    }
                    break;
            } // end of switch
            vtime = an_event.time;

            if (j == 0) compared_time = an_event.time;

            if ((compared_time == vtime && j != 0) || j == 0) {
                if (mode == Const.ASYNC || (mode == Const.SYNC && ptime >= vtime)) {
                    switch (event_list_type) {
                        case Const.LINKED:
                            status = remove_front_list(event_list, an_event);
                            if (status == -1) {
                                return_list.add(empty_event);
                                return return_list;
                            }

                            break;
                        case Const.HEAP:
                            status = heap_remove(an_event);
                            if (status == -1) {
                                return_list.add(empty_event);
                                return return_list;
                            }
                            break;
                    }

                    current_time = an_event.time;
                    current_event_id_list.add(new Integer(an_event.id));
                    return_list.add(an_event);
                } else {
                    return_list.add(empty_event);
                    return return_list;
                }
            } else {
                exit_loop = Const.true_;
            }

            current_node = current_node.next;
            j = j + 1;
        } // end of while    

        total_token_time += tokens_in_system * (time() - last_event_time);
        last_event_time = time();
        return return_list;

    }

    public static SimEvent next_event(double ptime, int mode) {
        int i, status;
        double vtime;
        SimEvent an_event = new SimEvent();

        SimEvent empty_event = new SimEvent();
        empty_event.time = -1.0;
        empty_event.id = -1;
        empty_event.priority = -1;

        switch (event_list_type) {
            case Const.LINKED:
                status = list_read(event_list, an_event);
                if (status == -1) return empty_event;
                break;
            case Const.HEAP:
                status = heap_read(an_event);
                if (status == -1) return empty_event;
                break;
        }

        vtime = an_event.time;

        if (mode == Const.ASYNC || (mode == Const.SYNC && ptime >= vtime)) {
            switch (event_list_type) {
                case Const.LINKED:
                    status = remove_front_list(event_list, an_event);
                    if (status == -1) return empty_event;
                    break;
                case Const.HEAP:
                    status = heap_remove(an_event);
                    if (status == -1) return empty_event;
                    break;
            }
            current_time = an_event.time;
            current_event_id = an_event.id;

            total_token_time += tokens_in_system * (time() - last_event_time);
            last_event_time = time();

            return (an_event);
        } else {
            return empty_event;
        }
    }


    public static int cancel_event(int event_id) {
        NODE current_node = new NODE();
        NODE previous_node = new NODE();
        NODE temp_ptr = new NODE();
        LIST_ list_ptr = new LIST_();

        list_ptr = event_list;

        current_node = list_ptr.front;
        previous_node = list_ptr.front;

        while ((current_node != null) && (current_node.event.id != event_id)) {
            previous_node = current_node;
            current_node = current_node.next;
        }
        if (current_node == null)
            return (Const.NOT_FOUND);
        else {
            if (previous_node == current_node) {
                temp_ptr = list_ptr.front;
                list_ptr.front = current_node.next;
            } else {
                temp_ptr = current_node;
                previous_node.next = current_node.next;
            }
            list_ptr.size--;
            return ((int) current_node.event.token.attr[0]);
        }
    }

    public static int cancel_token(TOKEN token) {
        NODE current_node = new NODE();
        NODE previous_node = new NODE();
        NODE temp_ptr = new NODE();
        LIST_ list_ptr = new LIST_();

        list_ptr = event_list;
        current_node = list_ptr.front;
        previous_node = list_ptr.front;
        while ((current_node != null) && (current_node.event.token.attr[0] != token.attr[0])) {
            previous_node = current_node;
            current_node = current_node.next;
        }
        if (current_node == null) {
            return (Const.NOT_FOUND);
        } else {
            if (previous_node == current_node) {
                temp_ptr = list_ptr.front;
                list_ptr.front = current_node.next;
            } else {
                temp_ptr = current_node;
                previous_node.next = current_node.next;
            }
            list_ptr.size--;
            return (current_node.event.id);
        }
    }

    public static int request(int facility_id, TOKEN token, int priority) {
        SimEvent an_event = new SimEvent();
        int i, server_num;

        if (facility[facility_id].busy_servers == 0)
            facility[facility_id].start_busy_time = time();

        if (facility[facility_id].status == Const.FREE) {
            facility[facility_id].busy_servers++;

            server_num = 1;
            while (facility[facility_id].server_info[server_num][0] != 0)
                server_num++;
            facility[facility_id].server_info[server_num][0] = (int) token.attr[0];
            facility[facility_id].server_info[server_num][1] = priority;
            if (facility[facility_id].busy_servers == facility[facility_id].total_servers)
                facility[facility_id].status = Const.BUSY;
            return (Const.FREE);
        } else {
            an_event.time = current_time;
            for (i = 0; i < Const.MAX_NUM_ATTR; i++)
                an_event.token.attr[i] = token.attr[i];
            an_event.priority = priority;
            an_event.id = current_event_id;
            insert_list(facility[facility_id].queue, an_event, Const.BEHIND_PRIORITY_KEY);
            return (Const.BUSY);
        }
    }

    public static void insert_list(LIST_ tmp_list_ptr, SimEvent tmp_event_ptr, int key) {
        LIST_ list_ptr = new LIST_();
        SimEvent event_ptr = new SimEvent();

        list_ptr = tmp_list_ptr;
        event_ptr = tmp_event_ptr;

        NODE current_node = new NODE();
        NODE previous_node = new NODE();
        NODE new_node = new NODE();

        new_node.event = event_ptr;
        new_node.next = null;
        list_ptr.size += 1;

        current_node = list_ptr.front;
        previous_node = list_ptr.front;

        if (key == Const.TIME_KEY)
            while ((current_node != null) &&
                    (current_node.event.time <= event_ptr.time)) {
                previous_node = current_node;
                current_node = current_node.next;
            }
        else if (key == Const.BEHIND_PRIORITY_KEY)
            while ((current_node != null) &&
                    (current_node.event.priority >= event_ptr.priority)) {
                previous_node = current_node;
                current_node = current_node.next;
            }
        else
            while ((current_node != null) &&
                    (current_node.event.priority > event_ptr.priority)) {
                previous_node = current_node;
                current_node = current_node.next;
            }

        if (current_node == null) {
            if (previous_node == null)
                list_ptr.front = new_node;
            else
                previous_node.next = new_node;
        } else {
            if (previous_node != current_node) {
                previous_node.next = new_node;
                new_node.next = current_node;
            } else {
                list_ptr.front = new_node;
                new_node.next = current_node;
            }
        }
    }

    public static int preempt(int facility_id, TOKEN token, int priority) {
        SimEvent an_event = new SimEvent();
        SimEvent heap_event = new SimEvent();
        NODE current_node = new NODE();
        NODE previous_node = new NODE();
        NODE temp_ptr = new NODE();
        LIST_ list_ptr = new LIST_();

        int server_num, do_preempt, i, preempted_token;
        int num_servers, minimum_priority, server_with_min = 0;
        int preempted_token_priority;

        if (facility[facility_id].busy_servers == 0)
            facility[facility_id].start_busy_time = time();

        if (facility[facility_id].status == Const.FREE) {
            facility[facility_id].busy_servers++;

            server_num = 1;
            while (facility[facility_id].server_info[server_num][0] != 0) {
                server_num++;
            }
            facility[facility_id].server_info[server_num][0] = (int) token.attr[0];
            facility[facility_id].server_info[server_num][1] = priority;
            if (facility[facility_id].busy_servers == facility[facility_id].total_servers)
                facility[facility_id].status = Const.BUSY;
            return (Const.FREE);
        } else {
            minimum_priority = 9999;
            num_servers = facility[facility_id].total_servers;

            for (i = 1; i <= num_servers; i++)
                if (facility[facility_id].server_info[i][1] < minimum_priority) {
                    minimum_priority = facility[facility_id].server_info[i][1];
                    server_with_min = i;
                }

            if (priority > minimum_priority)
                do_preempt = Const.true_;
            else
                do_preempt = Const.false_;

            if (do_preempt == Const.true_) {
                facility[facility_id].preemptions++;
                preempted_token = facility[facility_id].server_info[server_with_min][0];

                preempted_token_priority = facility[facility_id].server_info[server_with_min][1];
                facility[facility_id].server_info[server_with_min][0] = (int) token.attr[0];
                facility[facility_id].server_info[server_with_min][1] = priority;

                switch (event_list_type) {
                    case Const.LINKED:
                        an_event = listrmqueue(preempted_token, event_list);
                        an_event.priority = preempted_token_priority;
                        an_event.time -= time();
                        an_event.time = -an_event.time;
                        insert_list(facility[facility_id].queue, an_event, Const.AHEAD_PRIORITY_KEY);
                        break;
                    case Const.HEAP:
                        heap_delete(preempted_token, heap_event);
                        an_event.id = heap_event.id;
                        an_event.priority = preempted_token_priority;
                        an_event.time = heap_event.time - time();
                        an_event.time = -an_event.time;
                        an_event.token = heap_event.token;
                        insert_list(facility[facility_id].queue, an_event, Const.AHEAD_PRIORITY_KEY);
                        break;
                }
                return (Const.FREE);
            } else {
                an_event.time = current_time;
                for (i = 0; i < Const.MAX_NUM_ATTR; i++)
                    an_event.token.attr[i] = token.attr[i];
                an_event.priority = priority;
                an_event.id = current_event_id;
                insert_list(facility[facility_id].queue, an_event, Const.BEHIND_PRIORITY_KEY);
                return (Const.BUSY);
            }
        }
    }

    public static SimEvent listrmqueue(int n, LIST_ tmp_list_ptr) {
        LIST_ list_ptr = new LIST_();
        list_ptr = tmp_list_ptr;
        SimEvent temp = new SimEvent();
        NODE current_node = new NODE();
        NODE previous_node = new NODE();
        NODE temp_ptr = new NODE();
        current_node = list_ptr.front;
        previous_node = list_ptr.front;
        while ((current_node != null) && ((int) current_node.event.token.attr[0] != n)) {
            previous_node = current_node;
            current_node = current_node.next;
        }
        if (current_node == null) {
            System.out.println("PREEMPT : Attempt to preempt a non-existent token<P>");
            System.out.println("Token # " + n);
            return temp;
        } else {
            temp = current_node.event;
            if (previous_node == current_node) {
                temp_ptr = list_ptr.front;
                list_ptr.front = current_node.next;
            } else {
                temp_ptr = current_node;
                previous_node.next = current_node.next;
            }
            list_ptr.size--;
            return temp;
        }
    }

    public static void release(int facility_id, TOKEN token) {
        SimEvent an_event = new SimEvent();
        int server_num, i, found;

        server_num = facility[facility_id].total_servers;
        found = Const.false_;
        i = 1;
        while ((found == 0) && i <= server_num) {
            if (facility[facility_id].server_info[i][0] == (int) token.attr[0]) {
                facility[facility_id].server_info[i][0] = 0;
                facility[facility_id].server_info[i][1] = 0;
                found = Const.true_;
            }
            i++;
        }

        if (found == Const.true_) {
            facility[facility_id].status = Const.FREE;
            facility[facility_id].busy_servers--;
            if (facility[facility_id].busy_servers == 0) {
                facility[facility_id].total_busy_time += time() - facility[facility_id].start_busy_time;
                facility[facility_id].start_busy_time = time();
            }

            if (facility[facility_id].queue.size > 0) {
                remove_front_list(facility[facility_id].queue, an_event);
                if (an_event.time < 0) {
                    an_event.time = current_time - an_event.time;

                    switch (event_list_type) {
                        case Const.LINKED:
                            insert_list(event_list, an_event, Const.TIME_KEY);
                            break;
                        case Const.HEAP:
                            heap_insert(an_event);
                            break;
                    }

                    facility[facility_id].status = Const.BUSY;
                    facility[facility_id].busy_servers++;

                    server_num = 1;
                    while (facility[facility_id].server_info[server_num][0] != 0)
                        server_num++;
                    facility[facility_id].server_info[server_num][0] = (int) an_event.token.attr[0];
                    facility[facility_id].server_info[server_num][1] = an_event.priority;
                } else {
                    an_event.time = current_time;
                    switch (event_list_type) {
                        case Const.LINKED:
                            add_front_list(event_list, an_event);
                            break;
                        case Const.HEAP:
                            heap_insert(an_event);
                            break;
                    }
                }
            }
        }
    }

    public static void add_front_list(LIST_ tmp_list_ptr, SimEvent tmp_event_ptr) {
        LIST_ list_ptr = new LIST_();
        SimEvent event_ptr = new SimEvent();
        list_ptr = tmp_list_ptr;
        event_ptr = tmp_event_ptr;

        NODE new_node = new NODE();

        event_copy(new_node.event, event_ptr);
        new_node.next = null;
        if (list_ptr.size == 0)
            list_ptr.front = new_node;
        else {
            new_node.next = list_ptr.front;
            list_ptr.front = new_node;
        }
        list_ptr.size += 1;
    }

    /*    public static int remove_front_list(LIST_ tmp_list_ptr,SimEvent tmp_event_ptr) {
         ListPointer list_ptr = new ListPointer();
         ItemPointer event_ptr = new ItemPointer();
         list_ptr.ptr = tmp_list_ptr;
         event_ptr.ptr = tmp_event_ptr;
         if (list_ptr.ptr != null && list_ptr.ptr.front.ptr != null) {
         NodePointer temp_ptr = new NodePointer();
         event_copy(event_ptr.ptr, list_ptr.ptr.front.ptr.event);
             temp_ptr.ptr = list_ptr.ptr.front.ptr;
         list_ptr.ptr.front.ptr = list_ptr.ptr.front.ptr.next.ptr;
         list_ptr.ptr.size -= 1;
         return 0;
             } else
         return -1;
         } */

    public static int remove_front_list(LIST_ tmp_list_ptr, SimEvent tmp_event_ptr) {
        if (tmp_list_ptr != null && tmp_list_ptr.front != null) {
            NODE temp_ptr = new NODE();
            event_copy(tmp_event_ptr, tmp_list_ptr.front.event);
            temp_ptr = tmp_list_ptr.front;
            tmp_list_ptr.front = tmp_list_ptr.front.next;
            tmp_list_ptr.size -= 1;
            return 0;
        } else
            return -1;
    }

    /*    public static int list_read(LIST_ tmp_list_ptr,SimEvent tmp_event_ptr) {
            ListPointer list_ptr = new ListPointer();
	    ItemPointer event_ptr = new ItemPointer();
	    list_ptr.ptr = tmp_list_ptr;
	    event_ptr.ptr = tmp_event_ptr;
	    if (list_ptr.ptr != null && list_ptr.ptr.front.ptr !=null) {
		event_copy(event_ptr.ptr, list_ptr.ptr.front.ptr.event);
		return 0;
	    }
	    else
		return -1;
		} */

    public static int list_read(LIST_ tmp_list_ptr, SimEvent tmp_event_ptr) {
        if (tmp_list_ptr != null && tmp_list_ptr.front != null) {
            event_copy(tmp_event_ptr, tmp_list_ptr.front.event);

            return 0;
        } else
            return -1;
    }


    public static void trace_facility(int facility_id) {
        NODE node = new NODE();
        LIST_ list_ptr = new LIST_();
        int i;

        list_ptr = facility[facility_id].queue;
        System.out.println("Time: " + time());
        System.out.print("Queue " + facility_id + ": ");
        node = list_ptr.front;
        while (node != null) {
            System.out.print("(TM " + node.event.time + " ");
            System.out.print("EV " + node.event.id + " ");
            System.out.print("TK " + (int) node.event.token.attr[0] + ")");
            node = node.next;
        }
        System.out.println();
        for (i = 0; i < 60; i++) System.out.print("-");
        System.out.println();
    }

    public static void trace_eventlist() {
        NODE node = new NODE();
        LIST_ list_ptr = new LIST_();
        int i;

        list_ptr = event_list;
        System.out.println("Time: " + time());
        System.out.print("Events: ");
        node = list_ptr.front;
        while (node != null) {
            System.out.print("(TM " + node.event.time + " ");
            System.out.print("EV " + node.event.id + " ");
            System.out.print("TK " + (int) node.event.token.attr[0] + ") ");
            node = node.next;
        }
        System.out.println();
        for (i = 0; i < 60; i++) System.out.print("-");
        System.out.println();
    }

    public static STATE state() {
        LIST_ list_ptr = new LIST_();
        NODE node = new NODE();
        int i, j;
        char any_char;

        state_tmp.time = current_time;
        if (event_list_type == Const.LINKED) {
            list_ptr = event_list;
            node = list_ptr.front;
            i = 0;
            while (node != null) {
                state_tmp.future_events[i] = new SimEvent();
                event_copy(state_tmp.future_events[i++], node.event);
                node = node.next;
            }
            state_tmp.future_events_length = i;

        } else {
            state_tmp.heap_count = heap_count;
            for (i = 0; i < heap_count; i++) {
                state_tmp.heap[i] = new SimEvent();
                event_copy(state_tmp.heap[i], heap[i]);
            }
        }
        state_tmp.facility_length = facilities;
        for (i = 1; i <= facilities; i++) {
            state_tmp.facility[i] = new FACILITY_STATE();
            state_tmp.facility[i].name = new String(facility[i].name);
            state_tmp.facility[i].total_servers = facility[i].total_servers;
            state_tmp.facility[i].busy_servers = facility[i].busy_servers;

            for (j = 1; j <= facility[i].total_servers; j++) {
                state_tmp.facility[i].server_info[j][0] = facility[i].server_info[j][0];
                state_tmp.facility[i].server_info[j][1] = facility[i].server_info[j][1];
            }

            list_ptr = facility[i].queue;
            node = list_ptr.front;

            j = 0;
            while (node != null) {
                state_tmp.facility[i].queue[j] = new SimEvent();
                event_copy(state_tmp.facility[i].queue[j++], node.event);
                node = node.next;
            }
            state_tmp.facility[i].queue_length = j;

        }
        return state_tmp;
    }


    public static int random(int i, int n) {
        n -= i;
        //java.util.Random rn = new java.util.Random(java.lang.System.currentTimeMillis());
        java.util.Random rn = new java.util.Random();
        n = (int) java.lang.Math.round(n * rn.nextDouble());
        return (i + n);
    }

    public static double expntl(double x) {
        //java.util.Random rn = new java.util.Random(java.lang.System.currentTimeMillis());
        java.util.Random rn = new java.util.Random();
        return (-x * java.lang.Math.log(rn.nextDouble()));
    }
    
    public static double poisson(double arrival_rate) {
        //java.util.Random rn = new java.util.Random(java.lang.System.currentTimeMillis());
    	double k=0;                          			//Counter
    	int max_k = 1000;           					//k upper limit
    	java.util.Random rn = new java.util.Random();
    	double p = rn.nextDouble(); 					//uniform random number
    	double P = Math.exp(-arrival_rate);          	//probability
    	double sum=P;                     				//cumulant
    	if (sum>=p) 
    		return 0;             						//done allready
    	for (k=1; k<max_k; ++k) {         				//Loop over all k:s
    	    P*=arrival_rate/(double)k;            		//Calc next prob
    	    sum+=P;                         			//Increase cumulant
    	    if (sum>=p) 
    	    	break;              					//Leave loop
    	}

    	return (k); 
    }
    
    public static double factorial(double x) {
        if(x==1)
        	return 1;
        else
        	return (x * factorial(x-1));
    }

    public static double normal(double x, double s) {
        double v1, v2, w, z1;
        double z2 = 0.0;
        //java.util.Random rn = new java.util.Random(java.lang.System.currentTimeMillis());
        java.util.Random rn = new java.util.Random();

        if (z2 != 0.0) {
            z1 = z2;
            z2 = 0.0;
        } else {
            do {
                v1 = 2.0 * rn.nextDouble() - 1.0;
                v2 = 2.0 * rn.nextDouble() - 1.0;
                w = v1 * v1 + v2 * v2;
            } while (w >= 1.0);
            w = java.lang.Math.sqrt((-2.0 * java.lang.Math.log(w)) / w);
            z1 = v1 * w;
            z2 = v2 * w;
        }
        return (x + z1 * s);
    }

    public static double uniform(double a, double b) {
        //java.util.Random rn = new java.util.Random(java.lang.System.currentTimeMillis());
        java.util.Random rn = new java.util.Random();
        return (a + (b - a) * rn.nextDouble());
    }

    public static double erlang(double x, double s) {
        int i, k;
        double z;
        //java.util.Random rn = new java.util.Random(java.lang.System.currentTimeMillis());
        java.util.Random rn = new java.util.Random();

        z = x / s;
        k = (int) (z * z);
        z = 1.0;
        for (i = 0; i < k; i++)
            z *= rn.nextDouble();
        return (-(x / k) * java.lang.Math.log(z));
    }

    public static double time() {
        return (current_time);
    }

    public static double busy_time(int facility_id) {
        return (facility[facility_id].total_busy_time);
    }

    public static void update_arrivals() {
        arrivals++;
        tokens_in_system++;
    }

    public static void update_completions() {
        completions++;
        tokens_in_system--;
    }

    public static void report_stats() {
        int i;

        System.out.println("+-----------------------------+");
        System.out.println("| SimPackJS SIMULATION REPORT |");
        System.out.println("+-----------------------------+");

        if (completions == 0) completions = 1;
        total_sim_time = time();
        total_busy_time = 0.0;
        for (i = 1; i <= facilities; i++) {
            if (facility[i].busy_servers > 0)
                facility[i].total_busy_time += time() - facility[i].start_busy_time;
            total_busy_time += busy_time(i);
        }
        total_busy_time /= facilities;
        total_utilization = total_busy_time / total_sim_time;
        arrival_rate = arrivals / total_sim_time;
        throughput = completions / total_sim_time;
        mean_service_time = total_busy_time / completions;
        mean_num_tokens = total_token_time / total_sim_time;
        mean_residence_time = mean_num_tokens / throughput;

        System.out.println("Total Simulation Time: " + total_sim_time);
        System.out.println("Total System Arrivals: " + arrivals);
        System.out.println("Total System Completions: " + completions);

        System.out.println("System Wide Statistics");
        System.out.println("----------------------");
        System.out.println("System Utilization: " + 100.0 * total_utilization);
        System.out.println("Arrival Rate: " + arrival_rate + "  Throughput: " + throughput);
        System.out.println("Mean Service Time per Token: " + mean_service_time);
        System.out.println("Mean # of Tokens in System: " + mean_num_tokens);
        System.out.println("Mean Residence Time for each Token: " + mean_residence_time);

        System.out.println("Facility Statistics");
        System.out.println("-------------------");
        for (i = 1; i <= facilities; i++) {
            utilization = 100.0 * busy_time(i) / time();
            idle = 100.0 * (time() - busy_time(i)) / time();
            System.out.println("F " + i + " (" + facility[i].name + ") : Idle: " + idle + "%, Util: " + utilization + "%, Preemptions: " + facility[i].preemptions);
        }
    }

    public static int facility_size(int facility_id) {
        return (facility[facility_id].queue.size);
    }

    public static void event_copy(SimEvent target, SimEvent source) {
        target.time = source.time;
        target.id = source.id;
        TOKEN_copy(target.token, source.token);
        target.priority = source.priority;
    }

    public static void TOKEN_copy(TOKEN target, TOKEN source) {
        int i;
        for (i = 0; i < Const.MAX_NUM_ATTR; i++)
            target.attr[i] = source.attr[i];
    }
}

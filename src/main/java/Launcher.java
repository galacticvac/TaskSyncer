import com.eysman.tasksync.google.GoogleTaskClient;
import com.eysman.tasksync.wunderlist.WunderlistClient;
import com.eysman.tasksync.wunderlist.model.WList;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

public class Launcher {
    public static void main(String[] args) throws Exception {
        WunderlistClient wClient = new WunderlistClient();

        List<TaskList> googleTaskLists = GoogleTaskClient.getLists();

        List<WList> wunderlistLists = wClient.getLists();

    }
}
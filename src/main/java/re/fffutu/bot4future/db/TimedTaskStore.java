package re.fffutu.bot4future.db;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TimedTaskStore {
    private final Gson gson = new Gson();

    public void saveTimedTask(TimedTask task) {
        Jedis jedis = Database.create();
        jedis.set("timedtask:" + task.id.toString(), gson.toJson(task));
        jedis.sadd("timedtask:index", task.id.toString());
        Database.close(jedis);
    }

    public void deleteTimedTask(UUID id) {
        Jedis jedis = Database.create();
        jedis.del("timedtask:" + id.toString());
        jedis.srem("timedtask:index", id.toString());
        Database.close(jedis);
    }

    public List<TimedTask> getTimedTasks() {
        Jedis jedis = Database.create();
        List<TimedTask> tasks = jedis.smembers("timedtask:index").stream()
                                     .map(taskId ->
                                                  gson.fromJson(jedis.get("timedtask:" + taskId),
                                                                TimedTask.class))
                                     .collect(Collectors.toList());
        Database.close(jedis);
        return tasks;
    }

    public static enum TimedTaskType {
        UNMUTE,
        UNBAN
    }

    public static class TimedTask {
        public UUID id = UUID.randomUUID();
        public TimedTaskType type;
        public long time = 0;
        public UUID relatedAudit;
    }
}

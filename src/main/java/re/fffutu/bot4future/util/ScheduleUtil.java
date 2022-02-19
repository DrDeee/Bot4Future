package re.fffutu.bot4future.util;

import org.javacord.api.entity.message.Message;
import re.fffutu.bot4future.DiscordBot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScheduleUtil<T> {
    public final static ScheduleUtil<Message> MSG = new ScheduleUtil<>();

    public void schedule(CompletableFuture<T> future, Consumer<T> runnable, long delay, TimeUnit timeUnit) {
        future.thenAccept((result) -> {
            DiscordBot.POOL.schedule(() -> runnable.accept(result), delay, timeUnit);
        });
    }
}

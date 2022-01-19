package re.fffutu.bot4future.logging;

import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import org.javacord.api.entity.message.MessageBuilder;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RollingPolicy extends FixedWindowRollingPolicy {
    private static RollingPolicy policy;
    private boolean isStartup = true;

    private Map<Long, File> pendingFiles = new HashMap<>();

    public RollingPolicy() {
        policy = this;
    }

    public static String getFileName() {
        return policy.getActiveFileName();
    }


    @Override
    public void rollover() throws RolloverFailure {
        super.rollover();
        FileNamePattern pattern = new FileNamePattern(fileNamePatternStr, this.context);
        File file = new File(pattern.convertInt(getMinIndex()));
        if (file.exists()) {
            sendOrSchedule(file, Instant.now().getEpochSecond());
        }
    }

    private void sendOrSchedule(File file, long epochSecond) {
        if (DiscordBot.INSTANCE.api == null) {
            DiscordBot.POOL.schedule(() -> {
                sendOrSchedule(file, Instant.now().getEpochSecond());
            }, 5, TimeUnit.SECONDS);
        } else {
            uploadFile(file, epochSecond);
        }
    }

    private void uploadFile(File file, long epochSecond) {
        new MessageBuilder()
                .addEmbed(EmbedTemplate
                                  .success()
                                  .setTitle("Log")
                                  .addField("Endzeitpunkt", "<t:" + epochSecond + ">")
                                  .addField("Hinweis",
                                            "Die Logs können sensible Daten enthalten. Gebe diese NIE an" +
                                                    " Personen weiter, die nicht selber an die Logs kommen," +
                                                    " oder keinen triftigen Grund dafür haben."))
                .addAttachment(file)
                .send(DiscordBot.INSTANCE.api.getServerTextChannelById(DiscordBot.INSTANCE.config.getLong(
                        "dev_server.log_channel")).get());
    }
}

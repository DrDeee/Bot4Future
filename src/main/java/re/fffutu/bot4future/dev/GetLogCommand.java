package re.fffutu.bot4future.dev;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.logging.RollingPolicy;
import re.fffutu.bot4future.util.CommandManager.Command;
import re.fffutu.bot4future.util.CommandManager.CommandHandler;

import java.io.File;
import java.time.Instant;
import java.util.List;

public class GetLogCommand implements Command {

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("getlog")
                .setDescription("Bekomme das jetzige Log")
                .setDefaultPermission(false);
    }

    @Override
    public List<RoleType> getAllowed() {
        return List.of();
    }

    @Override
    public CommandHandler getHandler() {
        return (event) -> {
            if (DiscordBot.INSTANCE.config.getList("devs").contains(event.getInteraction().getUser().getId())) {
                File file = new File(RollingPolicy.getFileName());
                event.getInteraction()
                     .createImmediateResponder()
                     .addEmbed(EmbedTemplate.info()
                                            .setDescription(file.exists() && file.isFile() ? "Die Log-Datei wird dir " +
                                                    "sofort gesendet." :
                                                                    "Auf die Log-Datei kann zurzeit leider nicht " +
                                                                            "zugegriffen werden."))
                     .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                     .respond();
                if (file.exists() && file.isFile()) {
                    new MessageBuilder()
                            .addEmbed(EmbedTemplate.success()
                                                   .setTitle("Aktuelles Log")
                                                   .setDescription(
                                                           "Aktuelle Log-Datei vom <t:" +
                                                                   Instant.now().getEpochSecond() + ">." +
                                                                   "\n\nDie älteren Logs findest du im" +
                                                                   " Log-Channel.")
                                                   .addField("Hinweis",
                                                             "Die Logs können sensible Daten enthalten. Gebe " +
                                                                     "diese NIE an Personen weiter, die nicht selber an die Logs " +
                                                                     "kommen, oder keinen triftigen Grund dafür haben.")
                            )
                            .addAttachment(file)
                            .send(event.getInteraction().getChannel().get());

                }
            } else {
                event.getInteraction().createImmediateResponder()
                     .addEmbed(EmbedTemplate.error().setDescription("Du musst ein Bot-Entwickler sein, um auf die " +
                                                                            "Logs zugreifen zu können."))
                     .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                     .respond();
            }
        };
    }

    @Override
    public List<Long> getAllowedServers() {
        return List.of();
    }
}

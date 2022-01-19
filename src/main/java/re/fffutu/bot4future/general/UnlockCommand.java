package re.fffutu.bot4future.general;

import com.arkoisystems.captcha.Captcha;
import com.arkoisystems.captcha.CaptchaGenerator;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.listener.message.MessageCreateListener;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.util.CommandManager.Command;
import re.fffutu.bot4future.util.CommandManager.CommandHandler;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class UnlockCommand implements Command, MessageCreateListener {

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("freischalten")
                .setDescription("Nutze diesen Command, um dich hier freizuschalten.");
    }

    @Override
    public List<RoleType> getAllowed() {
        return List.of();
    }

    @Override
    public List<RoleType> getDisallowed() {
        return List.of(RoleType.MEMBER);
    }

    @Override
    public CommandHandler getHandler() {
        return event -> {
            User user = event.getSlashCommandInteraction().getUser();
            try {
                Captcha captcha = Captcha.builder(300, 100)
                                         .gaussianNoise(true)
                                         .gaussianNoiseMean(0.1f)
                                         .gaussianNoiseSigma(15f)
                                         .strokeNoise(true)
                                         .strokeNoiseAmount(5)
                                         .build();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                CaptchaGenerator.generateGIF(captcha, 4, 400, out);
                event.getSlashCommandInteraction().createImmediateResponder().respond();
                event.getInteraction().getChannel().ifPresent(channel -> {
                    channel.sendMessage(EmbedTemplate.success()
                                                     .setTitle("Captcha")
                                                     .setDescription(user.getMentionTag() + ", bitte schreibe die " +
                                                                             "Zeichenkette von diesem Bild hier unter diese Nachricht, um dich " +
                                                                             "freizuschalten.")
                                                     .setImage(out.toByteArray(),
                                                               "captcha.gif")
                                                     .addField("Code", captcha.getText())
                                                     .setFooter("Dieses Captcha wird in 5 Minuten ungültig."));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

    }
}

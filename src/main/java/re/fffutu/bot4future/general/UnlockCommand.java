package re.fffutu.bot4future.general;

import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.message.MessageCreateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.logging.UserLogListener;
import re.fffutu.bot4future.util.CommandManager.Command;
import re.fffutu.bot4future.util.CommandManager.CommandHandler;
import re.fffutu.bot4future.util.NumberToTextEnglish;
import re.fffutu.bot4future.util.NumberToTextGerman;
import re.fffutu.bot4future.util.ScheduleUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UnlockCommand implements Command, MessageCreateListener {
    private static final Random                        RANDOM           = new Random();
    private static final Logger                        LOGGER           = LoggerFactory.getLogger("User Unlock");
    private static final Map<Long, Long>               WELCOME_CHANNELS = new HashMap<>();
    public static UnlockCommand                 LISTENER;
    private final Map<Long, Map<Long, Integer>> attempts = new HashMap<>();

    public UnlockCommand() {
        LISTENER = this;
        reloadWelcomeChannels();
    }

    public static void reloadWelcomeChannels() {
        WELCOME_CHANNELS.clear();
        DiscordBot.INSTANCE.api.getServers().forEach(server -> {
            Database.CHANNELS.getChannel(server.getId(), ChannelStore.ChannelType.WELCOME_CHANNEL)
                             .ifPresent(channel -> {
                                 WELCOME_CHANNELS.put(server.getId(), channel.getId());
                             });
        });
    }

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder().setName("freischalten")
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
            Server server = event.getSlashCommandInteraction().getServer().get();
            User user = event.getSlashCommandInteraction().getUser();
            if (attempts.getOrDefault(server.getId(), new HashMap<>()).containsKey(user.getId())) {
                event.getSlashCommandInteraction()
                     .createImmediateResponder()
                     .addEmbed(EmbedTemplate.error()
                                            .setDescription(
                                                    "Du hast bereits eine Nummer erhalten, die du eingeben musst!"
                                                    + " Du findest diese nicht mehr? In spätestens 5 Minuten wurde diese Nummer zurückgesetzt,"
                                                    + " und du kannst den Prozess erneut beginnen.")
                                            .addField("English description",
                                                      "You have already received a number that you need to enter!"
                                                      + " You can't find it anymore? In 5 minutes at the latest,"
                                                      + " this number has been reset and you can start the process again."))
                     .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                     .respond()
                     .join();
            } else {
                int number = getRandomNumber();
                attempts.computeIfAbsent(server.getId(), (id) -> new HashMap<>()).put(user.getId(), number);
                InteractionOriginalResponseUpdater updater = event.getSlashCommandInteraction()
                                                                  .createImmediateResponder()
                                                                  .addEmbed(EmbedTemplate.info()
                                                                                         .setDescription("Hey "
                                                                                                         + user.getMentionTag()
                                                                                                         + ", du hast es fast geschafft! Gebe nun noch `"
                                                                                                         + NumberToTextGerman.intToText(
                                                                                                 number)
                                                                                                         + "` in Ziffern (0-9) hier in den Chat. Du hast dafür 5 Minuten Zeit.")
                                                                                         .addField("English translation",
                                                                                                   "You almost made it! Now enter `"
                                                                                                   + NumberToTextEnglish.intToText(
                                                                                                           number)
                                                                                                   + "` in digits (0-9) here in the chat. You have 5 minutes to do this."))
                                                                  .respond()
                                                                  .join();
                DiscordBot.POOL.schedule(() -> {
                    attempts.getOrDefault(server.getId(), new HashMap<>()).remove(user.getId());
                    updater.delete().join();
                }, 5, TimeUnit.MINUTES);
            }
        };
    }

    private int getRandomNumber() {
        return RANDOM.ints(10000, 99999).findFirst().getAsInt();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getServer().isPresent()
            && WELCOME_CHANNELS.getOrDefault(event.getServer().get().getId(), 123L) == event.getChannel().getId()) {
            if (!event.getMessageAuthor().isUser()) return;
            User user = event.getMessageAuthor().asUser().get();
            Long serverId = event.getServer().get().getId();
            if (attempts.containsKey(serverId) && attempts.get(serverId).containsKey(user.getId())) {
                int answer = attempts.get(serverId).get(user.getId());
                try {
                    int sent = Integer.parseInt(event.getMessageContent());
                    if (sent == answer) {
                        UserLogListener.INSTANCE.logUserUnlock(event.getServer().get(), user);
                        Optional<Role> optRole = Database.ROLES.getRole(event.getServer().get().getId(),
                                                                        RoleType.MEMBER);

                        if (optRole.isPresent()) {
                            optRole.get().addUser(user).join();
                            attempts.getOrDefault(serverId, new HashMap<>()).remove(user.getId());
                            event.getMessage()
                                 .reply(EmbedTemplate.success()
                                                     .setDescription(user.getNicknameMentionTag()
                                                                     + " hat sich erfolgreich freigeschaltet.")
                                                     .addField("English translation",
                                                               user.getNicknameMentionTag()
                                                               + " has successfully unlocked."));
                        } else {
                            LOGGER.error("Die Mitglieds-Rolle ist für den Server "
                                         + event.getServer().get().getName()
                                         + " nicht gesetzt!");
                            event.getMessage()
                                 .reply(EmbedTemplate.error()
                                                     .setDescription(
                                                             "Es ist keine Mitglieds-Rolle für diesen Server gesetzt. Dies ist ein Fehler der Administration."));

                        }
                        return;

                    }
                    if (sent > answer) {
                        event.getMessage().addReaction("⬇️").join();
                    } else {
                        event.getMessage().addReaction("⬆️").join();
                    }
                    String msg = event.getMessageContent();
                    String noticeGerman = getNoticeString(msg, sent, answer, false);
                    String noticeEnglish = getNoticeString(msg, sent, answer, true);
                    ScheduleUtil.MSG.schedule(event.getMessage()
                                                   .reply(EmbedTemplate.info()
                                                                       .setDescription(noticeGerman)
                                                                       .addField("English translation", noticeEnglish)),
                                              (m) -> {
                                                  m.delete();
                                                  event.getMessage().delete();
                                              },
                                              3,
                                              TimeUnit.MINUTES);
                } catch (NumberFormatException e) {
                    ScheduleUtil.MSG.schedule(event.getMessage()
                                                   .reply(EmbedTemplate.error()
                                                                       .setDescription(
                                                                               "Bitte gebe die dir gesendete Nummer in Ziffern (0-9) ein: `"
                                                                               + NumberToTextGerman.intToText(answer)
                                                                               + "`")
                                                                       .addField("English translation",
                                                                                 "Please enter the number sent to you in digits (0-9): `"
                                                                                 + NumberToTextEnglish.intToText(answer)
                                                                                 + "`")), (m) -> {
                        m.delete();
                        event.getMessage().delete();
                    }, 3, TimeUnit.MINUTES);
                }
            }
        }
    }

    private String getNoticeString(String message, int sent, int answer, boolean english) {
        int answerLength = Integer.toString(answer).length();
        if (answerLength == message.length()) {
            String sentStr = english ? NumberToTextEnglish.intToText(sent) : NumberToTextGerman.intToText(sent);
            String answerStr = english ? NumberToTextEnglish.intToText(answer) : NumberToTextGerman.intToText(answer);
            String difference = StringUtils.difference(sentStr, answerStr);

            String reverseAnswerStr = StringUtils.reverse(answerStr);
            if (difference.equals(answerStr)) {
                String reverseSentStr = StringUtils.reverse(sentStr);
                String reverseDifference = StringUtils.difference(reverseSentStr, reverseAnswerStr);
                if (reverseDifference.equals(reverseAnswerStr)) {
                    return (english ? "The number is: *" : "Die Zahl lautet: *") + answerStr + "*";
                } else {
                    String reverseDifferenceReversed = StringUtils.reverse(reverseDifference);
                    return (english ? "Note: " : "Hinweis: ") + answerStr.replaceFirst(reverseDifferenceReversed,
                                                                                       reverseDifferenceReversed.toUpperCase());

                }
            } else {
                String differenceReversed = StringUtils.reverse(difference);
                return (english ? "Note: " : "Hinweis: ") + StringUtils.reverse(reverseAnswerStr.replaceFirst(
                        differenceReversed,
                        differenceReversed.toUpperCase()));
            }

        } else {
            return english ? "The number is " + answerLength + " digits long." : "Die Zahl ist "
                                                                                 + answerLength
                                                                                 + " Ziffern lang.";
        }
    }
}

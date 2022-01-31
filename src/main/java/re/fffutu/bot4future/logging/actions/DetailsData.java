package re.fffutu.bot4future.logging.actions;

import org.javacord.api.entity.message.Message;

public class DetailsData {
    public int version = 0;
    public long msgId = 0;
    public long channelId = 0;
    public long serverId= 0;
    public String serverName;

    public static DetailsData fromMessage(Message msg) {
        DetailsData data = new DetailsData();
        data.version = Integer.parseInt(msg.getEmbeds().get(0).getFooter().get()
                .getText().get().substring(8).split("/")[0]) - 1;
        data.msgId = Long.parseLong(msg.getEmbeds().get(0).getFields().stream()
                .filter(field -> field.getName().equals("Message-ID"))
                .findAny().get().getValue());

        data.channelId = Long.parseLong(msg.getEmbeds().get(0).getFields().stream()
                .filter(field -> field.getName().equals("Channel-ID"))
                .findAny().get().getValue());

        data.serverId = Long.parseLong(msg.getEmbeds().get(0).getFields().stream()
                .filter(field -> field.getName().equals("Server-ID"))
                .findAny().get().getValue());

        data.serverName = msg.getEmbeds().get(0).getFields().stream()
                .filter(field -> field.getName().equals("Server"))
                .findAny().get().getValue();
        return data;
    }
}

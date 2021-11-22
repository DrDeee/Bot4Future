package re.fffutu.bot4future.logging;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.List;

public class MessageData {
    @SerializedName("d")
    public String content;
    @SerializedName("t")
    public long timeStamp = Instant.now().toEpochMilli();
    @SerializedName("u")
    public long userId = 0;
    @SerializedName("g")
    public long guildId = 0;
    @SerializedName("c")
    public long channelId = 0;
    @SerializedName("m")
    public long msgId = 0;
    @SerializedName("f")
    public List<String> files;
    @SerializedName("r")
    public boolean deleted = false;
}

package re.fffutu.bot4future.db;

import com.google.gson.Gson;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.logging.MessageData;
import re.fffutu.bot4future.util.Crypto;
import re.fffutu.bot4future.util.StringUtil;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MessageStore {
    private Crypto crypto = new Crypto();
    private Gson gson = new Gson();

    public CompletableFuture saveMessage(String message, long guildId, long channelId, long messageId, long userId, List<String> files) {
        return CompletableFuture.runAsync(() -> {
            MessageData messageData = new MessageData();
            messageData.deleted = message == null;
            messageData.content = message == null ? "deleted" : message;
            messageData.guildId = guildId;
            messageData.channelId = channelId;
            messageData.msgId = messageId;
            messageData.files = files;
            messageData.userId = userId;
            String password = (guildId + "").substring(10) + (channelId + "").substring(10);
            addMessage(crypto.encryptText(gson.toJson(messageData), password), messageId);
        });
    }

    public CompletableFuture updateMessage(String message, long guildId, long channelId,
                                           long messageId, long userId, List<String> files) {
        return saveMessage(message, guildId, channelId, messageId, userId, files);
    }

    public CompletableFuture<MessageData> getMessage(long guildId, long channelId, long messageId) {
        return CompletableFuture.supplyAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            byte[] cryptoText = getMessage(messageId);
            if (cryptoText == null) return null;
            return gson.fromJson(crypto.trimZeros(crypto.decryptText(cryptoText, password)), MessageData.class);
        });
    }

    public CompletableFuture<List<MessageData>> getMessageVersions(long guildId, long channelId, long messageId) {
        return CompletableFuture.supplyAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            List<byte[]> cryptoTexts = getMessages(messageId);
            return cryptoTexts.stream().map(bytes -> {
                if (bytes == null) return null;
                return gson.fromJson(crypto.trimZeros(crypto.decryptText(bytes, password)), MessageData.class);
            }).collect(Collectors.toList());
        });
    }

    private void addMessage(byte[] encrypted, long messageId) {
        Jedis jedis = Database.create();
        jedis.rpush("message:" + messageId, Base64.getEncoder().encodeToString(encrypted));
        Database.close(jedis);
    }

    private byte[] getMessage(long messageId) {
        Jedis jedis = Database.create();
        String key = "message:" + messageId;
        String s = jedis.lindex(key, -1);
        Database.close(jedis);
        return Base64.getDecoder().decode(s);
    }

    private List<byte[]> getMessages(long messageId) {
        Jedis jedis = Database.create();
        List<String> msgs = jedis.lrange("message:" + messageId, 0, -1);

        Database.close(jedis);
        return msgs.stream().map(s -> Base64.getDecoder().decode(s)).collect(Collectors.toList());
    }
}

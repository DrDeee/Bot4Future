package re.fffutu.bot4future.logging;

import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.util.Crypto;
import re.fffutu.bot4future.util.StringUtil;
import redis.clients.jedis.Jedis;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MessageStore {
    private Crypto crypto = new Crypto();

    public CompletableFuture saveMessage(String message, long guildId, long channelId, long messageId, long userId) {
        return CompletableFuture.runAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String timeStamp = StringUtil.padStart(System.currentTimeMillis() + "", 22, '0');

            String password = guildStr.substring(10) + channelStr.substring(10);
            addMessage(crypto.encryptText(StringUtil.padStart(userId + "", 22, '0')
                    + StringUtil.padStart(channelId + "", 22, '0')
                    + timeStamp + message, password), messageId);
        });
    }

    public CompletableFuture updateMessage(String message, long guildId, long channelId, long messageId, long userId) {
        return saveMessage(message, guildId, channelId, messageId, userId);
    }


    public CompletableFuture<String> getMessage(long guildId, long channelId, long messageId) {
        return CompletableFuture.supplyAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            byte[] cryptoText = getMessage(messageId);
            if (cryptoText == null) return null;
            return crypto.trimZeros(crypto.decryptText(cryptoText, password).substring(66));
        });
    }

    public CompletableFuture<List<String>> getMessageVersions(long guildId, long channelId, long messageId) {
        return CompletableFuture.supplyAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            List<byte[]> cryptoTexts = getMessages(messageId);
            return cryptoTexts.stream().map(bytes -> {
                if (bytes == null) return null;
                return crypto.trimZeros(crypto.decryptText(bytes, password));
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

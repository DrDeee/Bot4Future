package re.fffutu.bot4future.logging;

import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.util.Crypto;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MessageStore {
    private Crypto crypto = new Crypto();

    public CompletableFuture saveMessageById(String message, long guildId, long channelId, long messageId, long userId) {
        return CompletableFuture.runAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            setMessage(crypto.encryptText(userId + message, password), messageId);
        });
    }

    public CompletableFuture updateMessageById(String message, long guildId, long channelId, long messageId, long userId) {
        return CompletableFuture.runAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            setMessage(crypto.encryptText(userId + message, password), messageId);
        });
    }

    public CompletableFuture<String> getMessageById(long guildId, long channelId, long messageId) {
        return getMessageAndUserIdById(guildId, channelId, messageId).thenApply(s -> {
            if(s == null) return null;
            return s.substring(18);
        });
    }

    public CompletableFuture<String> getMessageAndUserIdById(long guildId, long channelId, long messageId) {
        return CompletableFuture.supplyAsync(() -> {
            String guildStr = "" + guildId;
            String channelStr = "" + channelId;

            String password = guildStr.substring(10) + channelStr.substring(10);
            byte[] cryptoText = getMessage(messageId);
            if (cryptoText == null) return null;
            return crypto.decryptText(cryptoText, password);
        });
    }

    public CompletableFuture deleteMessageById(long messageId) {
        return CompletableFuture.runAsync(() -> {
            deleteMessage(messageId);
        });
    }

    private void setMessage(byte[] encrypted, long messageId) {
        Jedis jedis = Database.POOL.getResource();
        jedis.set("message:" + messageId, Base64.getEncoder().encodeToString(encrypted));
        Database.POOL.returnResource(jedis);
    }

    private byte[] getMessage(long messageId) {
        Jedis jedis = Database.POOL.getResource();
        String s = jedis.get("message:" + messageId);
        Database.POOL.returnResource(jedis);
        return Base64.getDecoder().decode(s);
    }

    private void deleteMessage(long messageId) {
        Jedis jedis = Database.POOL.getResource();
        jedis.set("message:" + messageId, null);
        Database.POOL.returnResource(jedis);
    }
}

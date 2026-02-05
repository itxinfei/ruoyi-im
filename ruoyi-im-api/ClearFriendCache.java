import redis.clients.jedis.Jedis;

public class ClearFriendCache {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("172.168.20.222", 6379);
        try {
            jedis.auth("123456");
            // 清除所有好友列表缓存
            for (long userId : new long[]{23L, 24L}) {
                String key = "contact:list:" + userId;
                jedis.del(key);
                System.out.println("已清除缓存: " + key);
            }
            // 也可以清除所有匹配的缓存
            java.util.Set<String> keys = jedis.keys("contact:list:*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    jedis.del(key);
                }
                System.out.println("共清除 " + keys.size() + " 个缓存键");
            }
        } finally {
            jedis.close();
        }
    }
}

package com.xxx.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
class SeckillApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testLock01() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位。如果key不存在才可以设置成功
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1");
        // 如果占位成功，进行正常操作
        if(isLock) {
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name=" + name);
            // 操作结束，删除锁
            redisTemplate.delete("k1");
        } else {
            System.out.println("有线程在使用，请稍后");
        }
    }

}

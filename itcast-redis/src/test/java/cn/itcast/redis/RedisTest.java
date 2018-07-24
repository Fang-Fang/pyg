package cn.itcast.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testString() throws  Exception{
        redisTemplate.boundValueOps("str").set("传智播客");
        Object str = redisTemplate.boundValueOps("str").get();
        System.out.println(str);
    }

    /**
     * 测试散列类型
     * @throws Exception
     */
    @Test
    public void testHash() throws  Exception{
        redisTemplate.boundHashOps("h_key").put("f1", "v1");
        redisTemplate.boundHashOps("h_key").put("f2", "v2");
        List list = redisTemplate.boundHashOps("h_key").values();
        System.out.println(list);
    }

    /**
     * 测试列表
     * @throws Exception
     */
    @Test
    public void testList() throws  Exception{
        redisTemplate.boundListOps("l_key").leftPush("b");
        redisTemplate.boundListOps("l_key").leftPush("a");
        redisTemplate.boundListOps("l_key").rightPush("c");

        List list = redisTemplate.boundListOps("l_key").range(0, -1);

        System.out.println(list);
    }

    /**
     * 测试无序集合
     * @throws Exception
     */
    @Test
    public void testSet() throws  Exception{
        redisTemplate.boundSetOps("s_key").add("a", 1, "b", 2);
        Set set = redisTemplate.boundSetOps("s_key").members();
        System.out.println(set);
    }

    /**
     * 测试有序集合;默认按照分数升序排序
     * @throws Exception
     */
    @Test
    public void testSortedSet() throws  Exception{
        redisTemplate.boundZSetOps("z_key").add("d", 10);
        redisTemplate.boundZSetOps("z_key").add("e", 8);
        redisTemplate.boundZSetOps("z_key").add("f", 15);
        Set set = redisTemplate.boundZSetOps("z_key").range(0, -1);
        System.out.println(set);
    }
}

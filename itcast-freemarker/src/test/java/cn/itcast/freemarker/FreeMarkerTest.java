package cn.itcast.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FreeMarkerTest {

    @Test
    public void test() throws Exception {
        //创建一个配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //设置模版的路径
        configuration.setClassForTemplateLoading(FreeMarkerTest.class, "/ftl");
        //设置编码
        configuration.setDefaultEncoding("utf-8");

        //获取模版
        Template template = configuration.getTemplate("test.ftl");

        //获取数据
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "传智播客");
        dataModel.put("message", "欢迎使用FreeMarker");

        List<Map<String, Object>> goodsList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "苹果");
        map1.put("price", "4.5");
        goodsList.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "橘子");
        map2.put("price", "2.5");
        goodsList.add(map2);

        dataModel.put("goodsList", goodsList);

        dataModel.put("today", new Date());

        dataModel.put("number", 123456789L);

        //输出对象
        FileWriter fileWriter = new FileWriter("D:\\itcast\\test\\test.html");

        //输出，参数1：输出的数据map，参数2：输出媒介
        template.process(dataModel, fileWriter);

        fileWriter.close();
    }
}

package cn.itcast.springboot;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 引导类：在spring boot工程中必须要存在；是启动和访问的入口
 * 它是一个组合注解：其中就包含了组件扫描注解，也就是会对当前包及其子包下的类进行扫描
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        //SpringApplication.run(Application.class, args);
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.setBannerMode(Banner.Mode.OFF);//启动的时候不显示logo
        springApplication.run(args);
    }
}

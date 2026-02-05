import com.ruoyi.im.util.TestDataInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 好友关系数据初始化启动器
 * 用于独立运行来初始化张三、李四的好友关系数据
 *
 * 运行方式：
 * mvn exec:java -Dexec.mainClass="InitFriendData" -Dexec.classpathScope=compile
 * 或者在 IDE 中直接运行此类
 *
 * @author ruoyi
 */
@SpringBootApplication(scanBasePackages = "com.ruoyi")
public class InitFriendData {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  好友关系数据初始化工具");
        System.out.println("========================================");
        SpringApplication.run(InitFriendData.class, args);
    }

    @Bean
    public CommandLineRunner init(TestDataInitializer testDataInitializer) {
        return args -> {
            System.out.println("开始初始化好友关系测试数据...");

            // 初始化好友数据
            testDataInitializer.initFriendData();

            // 打印统计信息
            testDataInitializer.printFriendStatistics();

            System.out.println("========================================");
            System.out.println("  初始化完成！请调用清除缓存API:");
            System.out.println("  POST /api/im/contact/cache/clear");
            System.out.println("========================================");

            // 退出程序
            System.exit(0);
        };
    }
}

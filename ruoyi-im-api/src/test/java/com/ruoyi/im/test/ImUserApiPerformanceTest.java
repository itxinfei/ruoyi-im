package com.ruoyi.im.test;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户API性能测试
 * 
 * 对IM用户相关的API进行性能测试，包括：
 * - 用户列表查询
 * - 用户详情查询
 * - 用户注册
 * - 用户登录
 * - 好友列表查询
 */
@SpringBootTest
@ActiveProfiles("test")
public class ImUserApiPerformanceTest extends PerformanceTestBase {

    // API基础路径
    private static final String BASE_URL = "http://localhost:8080/api";
    
    // API路径
    private static final String USER_LIST_PATH = "/im/user/list";
    private static final String USER_DETAIL_PATH = "/im/user/";
    private static final String USER_ADD_PATH = "/im/user";
    private static final String USER_LOGIN_PATH = "/im/auth/login";
    
    // 测试用户参数
    private static final String TEST_USERNAME_PREFIX = "test_user_";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME_PREFIX = "测试用户_";
    
    // 测试线程数和请求数
    private static final int CONCURRENT_USERS = 10;
    private static final int REQUESTS_PER_USER = 100;
    
    // 测试用户认证令牌
    private String authToken;
    
    @Override
    public void setUp() {
        super.initTestEnvironment();
        
        // 执行登录获取认证令牌
        try {
            authToken = performLogin();
        } catch (Exception e) {
            System.err.println("登录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void tearDown() {
        super.cleanupTestEnvironment();
    }
    
    /**
     * 执行用户登录并获取令牌
     */
    private String performLogin() throws Exception {
        try {
            String username = "admin";
            String password = "admin123";
            JSONObject loginRequest = new JSONObject();
            loginRequest.put("username", username);
            loginRequest.put("password", password);
            
            StringEntity entity = new StringEntity(loginRequest.toString(), "UTF-8");
            entity.setContentType("application/json");
            
            HttpPost httpPost = new HttpPost(BASE_URL + USER_LOGIN_PATH);
            httpPost.setEntity(entity);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (jsonResponse.getInt("code") == 200) {
                JSONObject data = jsonResponse.getJSONObject("data");
                return data.getString("token");
            } else {
                throw new Exception("登录失败: " + jsonResponse.getString("message"));
            }
        } catch (JSONException | IOException e) {
            throw new Exception("登录请求失败", e);
        }
    }
    
    /**
     * 测试用户列表API性能
     */
    public PerformanceTestResult testUserListPerformance() {
        System.out.println("开始测试用户列表API性能...");
        
        return runConcurrentTest(CONCURRENT_USERS, REQUESTS_PER_USER, () -> {
            try {
                URI uri = new URIBuilder(BASE_URL + USER_LIST_PATH)
                        .setParameter("username", "test")
                        .build();
                
                HttpGet httpGet = new HttpGet(uri);
                httpGet.setHeader("Authorization", "Bearer " + authToken);
                
                long startTime = System.currentTimeMillis();
                org.apache.http.HttpResponse response = httpClient.execute(httpGet);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 200) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.getInt("code") == 200) {
                        // 记录成功请求
                        recordMetric("userList", true, responseTime);
                    } else {
                        // 记录错误请求
                        recordMetric("userList", false, responseTime);
                    }
                } else {
                    // 记录错误请求
                    recordMetric("userList", false, responseTime);
                }
            } catch (Exception e) {
                // 记录错误请求
                recordMetric("userList", false, 0);
                throw new RuntimeException("用户列表API测试失败", e);
            }
        });
    }
    
    /**
     * 测试用户详情API性能
     */
    public PerformanceTestResult testUserDetailPerformance() {
        System.out.println("开始测试用户详情API性能...");
        
        // 获取用户ID列表
        List<Long> userIds = getUserIds();
        if (userIds.isEmpty()) {
            System.out.println("未找到测试用户，无法进行测试");
            return null;
        }
        
        return runConcurrentTest(CONCURRENT_USERS, REQUESTS_PER_USER, () -> {
            try {
                // 随机选择一个用户ID
                Long userId = userIds.get(generateRandomInt(0, userIds.size() - 1));
                
                HttpGet httpGet = new HttpGet(BASE_URL + USER_DETAIL_PATH + userId);
                httpGet.setHeader("Authorization", "Bearer " + authToken);
                
                long startTime = System.currentTimeMillis();
                org.apache.http.HttpResponse response = httpClient.execute(httpGet);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 200) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.getInt("code") == 200) {
                        // 记录成功请求
                        recordMetric("userDetail", true, responseTime);
                    } else {
                        // 记录错误请求
                        recordMetric("userDetail", false, responseTime);
                    }
                } else {
                    // 记录错误请求
                    recordMetric("userDetail", false, responseTime);
                }
            } catch (Exception e) {
                // 记录错误请求
                recordMetric("userDetail", false, 0);
                throw new RuntimeException("用户详情API测试失败", e);
            }
        });
    }
    
    /**
     * 测试用户注册API性能
     */
    public PerformanceTestResult testUserAddPerformance() {
        System.out.println("开始测试用户注册API性能...");
        
        return runConcurrentTest(CONCURRENT_USERS, REQUESTS_PER_USER, () -> {
            try {
                // 生成随机测试用户数据
                String username = TEST_USERNAME_PREFIX + generateRandomString(8);
                String nickname = TEST_NICKNAME_PREFIX + generateRandomString(5);
                String password = TEST_PASSWORD;
                
                JSONObject userRequest = new JSONObject();
                userRequest.put("username", username);
                userRequest.put("nickname", nickname);
                userRequest.put("password", password);
                userRequest.put("email", username + "@test.com");
                userRequest.put("phone", "138" + generateRandomString(8));
                
                StringEntity entity = new StringEntity(userRequest.toString(), "UTF-8");
                entity.setContentType("application/json");
                
                HttpPost httpPost = new HttpPost(BASE_URL + USER_ADD_PATH);
                httpPost.setEntity(entity);
                httpPost.setHeader("Authorization", "Bearer " + authToken);
                
                long startTime = System.currentTimeMillis();
                org.apache.http.HttpResponse response = httpClient.execute(httpPost);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 200) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.getInt("code") == 200) {
                        // 记录成功请求
                        recordMetric("userAdd", true, responseTime);
                    } else {
                        // 记录错误请求
                        recordMetric("userAdd", false, responseTime);
                    }
                } else {
                    // 记录错误请求
                    recordMetric("userAdd", false, responseTime);
                }
            } catch (Exception e) {
                // 记录错误请求
                recordMetric("userAdd", false, 0);
                throw new RuntimeException("用户注册API测试失败", e);
            }
        });
    }
    
    /**
     * 获取用户ID列表
     */
    private List<Long> getUserIds() {
        List<Long> userIds = new ArrayList<>();
        
        try {
            URI uri = new URIBuilder(BASE_URL + USER_LIST_PATH)
                    .setParameter("pageNum", "1")
                    .setParameter("pageSize", "100")
                    .build();
            
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Authorization", "Bearer " + authToken);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (jsonResponse.getInt("code") == 200) {
                JSONObject data = jsonResponse.getJSONObject("data");
                JSONArray rows = data.getJSONArray("rows");
                
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject user = rows.getJSONObject(i);
                    userIds.add(user.getLong("userId"));
                }
            }
        } catch (Exception e) {
            System.err.println("获取用户列表失败: " + e.getMessage());
        }
        
        return userIds;
    }
    
    /**
     * 记录性能指标
     */
    private void recordMetric(String apiName, boolean success, long responseTime) {
        PerformanceMetric metric = performanceMetrics.computeIfAbsent(apiName, k -> new PerformanceMetric());
        metric.recordRequest(success, responseTime);
    }
    
    /**
     * 运行所有性能测试
     */
    public Map<String, PerformanceTestResult> runAllTests() {
        Map<String, PerformanceTestResult> results = new HashMap<>();
        
        // 测试用户列表API
        results.put("userList", testUserListPerformance());
        
        // 测试用户详情API
        results.put("userDetail", testUserDetailPerformance());
        
        // 测试用户注册API
        results.put("userAdd", testUserAddPerformance());
        
        return results;
    }
    
    /**
     * 打印性能测试结果
     */
    public void printResults(Map<String, PerformanceTestResult> results) {
        System.out.println("\n===== API性能测试结果 =====");
        
        for (Map.Entry<String, PerformanceTestResult> entry : results.entrySet()) {
            String apiName = entry.getKey();
            PerformanceTestResult result = entry.getValue();
            
            System.out.println("\n" + apiName + " API性能测试:");
            System.out.println(result.toReportString());
        }
    }
    
    public static void main(String[] args) {
        ImUserApiPerformanceTest test = new ImUserApiPerformanceTest();
        test.setUp();
        
        try {
            Map<String, PerformanceTestResult> results = test.runAllTests();
            test.printResults(results);
            
            // 保存结果到文件
            PerformanceReportGenerator.generateReport(results, "im-api-performance-report.json");
            System.out.println("\n性能测试报告已生成: im-api-performance-report.json");
        } catch (Exception e) {
            System.err.println("性能测试执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            test.tearDown();
        }
    }
}
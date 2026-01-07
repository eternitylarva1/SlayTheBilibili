package chronometry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 模拟 Twitch 连接的辅助类
 * 用于在没有真实 Twitch 连接时提供默认值和模拟行为
 */
public class MockTwitchHelper {
    private static final String[] MOCK_USERS = {
        "TestUser1", "TestUser2", "TestUser3", "TestUser4", "TestUser5",
        "ViewerA", "ViewerB", "ViewerC", "ViewerD", "ViewerE",
        "ChatMember1", "ChatMember2", "ChatMember3", "ChatMember4", "ChatMember5"
    };
    
    private static Set<String> mockVotedUsernames = new HashSet<>();
    private static boolean isMockMode = false;
    private static boolean isVoting = false;
    private static int secondsRemaining = 30;
    private static String[] currentOptions;
    private static int[] mockVoteCounts;
    private static Consumer<Integer> currentCallback;
    
    /**
     * 启用模拟模式
     */
    public static void enableMockMode() {
        isMockMode = true;
        // 初始化模拟用户
        for (String user : MOCK_USERS) {
            mockVotedUsernames.add(user);
        }
        SlayTheStreamer.log("Mock Twitch mode enabled");
    }
    
    /**
     * 禁用模拟模式
     */
    public static void disableMockMode() {
        isMockMode = false;
        SlayTheStreamer.log("Mock Twitch mode disabled");
    }
    
    /**
     * 检查是否处于模拟模式
     */
    public static boolean isMockMode() {
        return isMockMode;
    }
    
    /**
     * 获取模拟的投票用户名集合
     */
    public static Set<String> getMockVotedUsernames() {
        return mockVotedUsernames;
    }
    
    /**
     * 模拟是否已连接到 Twitch
     */
    public static boolean isVotingConnected() {
        return isMockMode;
    }
    
    /**
     * 模拟发起投票
     */
    public static boolean initiateSimpleNumberVote(String[] options, Consumer<Integer> callback) {
        if (!isMockMode) {
            return false;
        }
        
        isVoting = true;
        secondsRemaining = 30;
        currentOptions = options;
        currentCallback = callback;
        
        // 初始化模拟票数
        mockVoteCounts = new int[options.length];
        for (int i = 0; i < options.length; i++) {
            // 随机分配 0-10 票
            mockVoteCounts[i] = (int)(Math.random() * 10);
        }
        
        SlayTheStreamer.log("Mock voting started with " + options.length + " options");
        
        // 模拟投票结束：30秒后自动选择第一个选项
        new Thread(() -> {
            try {
                for (int i = 30; i >= 0; i--) {
                    secondsRemaining = i;
                    Thread.sleep(1000);
                }
                isVoting = false;
                // 默认选择第一个选项
                if (currentCallback != null) {
                    currentCallback.accept(0);
                    SlayTheStreamer.log("Mock voting ended, selected option 0: " + currentOptions[0]);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        return true;
    }
    
    /**
     * 模拟结束投票
     */
    public static void endVoting(boolean cancel) {
        isVoting = false;
        currentCallback = null;
        SlayTheStreamer.log("Mock voting " + (cancel ? "cancelled" : "ended"));
    }
    
    /**
     * 获取剩余秒数
     */
    public static int getSecondsRemaining() {
        return secondsRemaining;
    }
    
    /**
     * 获取当前选项
     */
    public static String[] getCurrentOptions() {
        return currentOptions;
    }
    
    /**
     * 获取模拟票数
     */
    public static int[] getMockVoteCounts() {
        return mockVoteCounts;
    }
    
    /**
     * 检查是否正在投票
     */
    public static boolean isVoting() {
        return isVoting;
    }
    
    /**
     * 从模拟用户列表中移除用户名
     */
    public static void removeUsername(String username) {
        mockVotedUsernames.remove(username);
    }
    
    /**
     * 添加用户名到模拟列表
     */
    public static void addUsername(String username) {
        mockVotedUsernames.add(username);
    }
}

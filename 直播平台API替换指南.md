# 直播平台 API 替换指南

## 概述

本文档说明如何将 Slay the Streamer 模组从 Twitch API 替换到其他直播平台（如 YouTube Live、Bilibili 直播等）。

---

## 1. 需要获取的核心功能

### 1.1 连接管理

| 功能 | Twitch 实现 | 替换要求 |
|------|-------------|----------|
| 连接到直播间 | `TwitchConnection` | 建立与直播平台的 WebSocket/IRC 连接 |
| 连接状态检查 | `TwitchVoter.isVotingConnected()` | 检查连接是否活跃 |
| 断线重连 | Twitch Integration 自动处理 | 实现自动重连机制 |

### 1.2 用户信息

| 功能 | Twitch 实现 | 替换要求 |
|------|-------------|----------|
| 获取用户名 | `TwitchConfig.username` | 获取当前登录用户/主播的用户名 |
| 获取显示名称 | `TwitchUser.getDisplayName()` | 获取用户的显示名称（可能包含特殊字符） |
| 获取用户 ID | `TwitchUser.getUserName()` | 获取用户的唯一标识符 |

### 1.3 聊天消息

| 功能 | Twitch 实现 | 替换要求 |
|------|-------------|----------|
| 接收聊天消息 | `TwitchMessageListener.onMessage()` | 监听并接收实时聊天消息 |
| 发送消息到聊天 | `connection.sendMessage()` | 向直播间发送消息 |
| 消息格式 | `String msg, String user` | 消息内容和发送者用户名 |

### 1.4 投票功能

| 功能 | Twitch 实现 | 替换要求 |
|------|-------------|----------|
| 发起投票 | `initiateSimpleNumberVote()` | 创建并启动投票 |
| 获取投票选项 | `getOptions()` | 获取当前投票的选项列表 |
| 获取票数 | `TwitchVoteOption.voteCount` | 获取每个选项的票数 |
| 获取剩余时间 | `getSecondsRemaining()` | 获取投票倒计时 |
| 结束投票 | `endVoting()` | 结束当前投票 |
| 投票状态监听 | `TwitchVoteListener` | 监听投票开始/结束事件 |

### 1.5 用户投票记录

| 功能 | Twitch 实现 | 替换要求 |
|------|-------------|----------|
| 获取已投票用户 | `votedUsernames` (私有字段) | 获取参与投票的用户列表 |
| 移除已投票用户 | `votedUsernames.remove()` | 从列表中移除用户 |

---

## 2. 需要替换的类和接口

### 2.1 核心接口定义

需要创建以下接口来替代 Twitch 接口：

```java
// 直播平台连接接口
public interface LiveStreamConnection {
    boolean isConnected();
    void sendMessage(String message);
    void addMessageListener(MessageListener listener);
    void removeMessageListener(MessageListener listener);
}

// 消息监听器接口
public interface MessageListener {
    void onMessage(String message, String user);
}

// 投票器接口
public interface LiveStreamVoter {
    boolean isVotingConnected();
    boolean initiateSimpleNumberVote(String[] options, Consumer<Integer> callback);
    void endVoting(boolean cancel);
    VoteOption[] getOptions();
    int getSecondsRemaining();
    void addVoteListener(VoteListener listener);
    void removeVoteListener(VoteListener listener);
}

// 投票选项类
public class VoteOption {
    public String displayName;
    public int voteCount;
}

// 投票监听器接口
public interface VoteListener {
    void onVoteAvailable();
    void onVoteUnavailable();
}

// 配置接口
public interface LiveStreamConfig {
    String getUsername();
}
```

### 2.2 需要修改的文件

| 文件 | 修改内容 |
|------|----------|
| `SlayTheStreamer.java` | 替换 `TwitchMessageListener`、`TwitchVoteListener`、`TwitchPanel` |
| `BossSelectScreen.java` | 替换 `TwitchPanel`、`TwitchVoter`、`TwitchVoteOption` |
| `CardRewardPatch.java` | 替换 `TwitchPanel`、`TwitchVoter`、`TwitchVoteOption` |
| `NoSkipBossRelicPatch.java` | 替换 `TwitchPanel`、`TwitchVoter`、`TwitchVoteOption` |
| `StartGamePatch.java` | 替换 `TwitchPanel`、`TwitchVoter`、`TwitchVoteOption` |
| `MonsterNamesPatch.java` | 替换 `TwitchPanel`、`TwitchVoter`、`Twirk`、`TwitchUser` |
| `MainMenuDisplayPatch.java` | 替换 `TwitchPanel`、`TwitchConfig` |
| `TwirkPatch.java` | 删除或替换为平台特定的补丁 |

---

## 3. 具体替换步骤

### 3.1 步骤 1：创建平台适配器

创建一个新的包来存放平台适配器：

```
chronometry/
├── livestream/
│   ├── LiveStreamConnection.java      (接口)
│   ├── LiveStreamVoter.java           (接口)
│   ├── MessageListener.java           (接口)
│   ├── VoteListener.java              (接口)
│   ├── VoteOption.java                (类)
│   ├── LiveStreamConfig.java          (接口)
│   └── adapters/
│       ├── TwitchAdapter.java         (Twitch 实现)
│       ├── YouTubeAdapter.java        (YouTube 实现)
│       └── BilibiliAdapter.java       (Bilibili 实现)
```

### 3.2 步骤 2：实现 Twitch 适配器（保持兼容）

```java
package chronometry.livestream.adapters;

import de.robojumper.ststwitch.*;
import chronometry.livestream.*;

public class TwitchAdapter implements LiveStreamConnection, LiveStreamVoter, LiveStreamConfig {
    private TwitchPanel twitchPanel;
    private TwitchVoter twitchVoter;
    
    public TwitchAdapter(TwitchPanel panel) {
        this.twitchPanel = panel;
        this.twitchVoter = TwitchPanel.getDefaultVoter().orElse(null);
    }
    
    // LiveStreamConnection 实现
    @Override
    public boolean isConnected() {
        return twitchPanel != null && twitchVoter != null && twitchVoter.isVotingConnected();
    }
    
    @Override
    public void sendMessage(String message) {
        if (twitchPanel != null) {
            twitchPanel.connection.sendMessage(message);
        }
    }
    
    @Override
    public void addMessageListener(MessageListener listener) {
        // 使用反射添加监听器
        List<TwitchMessageListener> listeners = (List)ReflectionHacks.getPrivate(
            twitchPanel.connection, 
            TwitchConnection.class, 
            "listeners"
        );
        listeners.add(new TwitchMessageListener() {
            @Override
            public void onMessage(String msg, String user) {
                listener.onMessage(msg, user);
            }
        });
    }
    
    // LiveStreamVoter 实现
    @Override
    public boolean isVotingConnected() {
        return twitchVoter != null && twitchVoter.isVotingConnected();
    }
    
    @Override
    public boolean initiateSimpleNumberVote(String[] options, Consumer<Integer> callback) {
        return twitchVoter != null && twitchVoter.initiateSimpleNumberVote(options, callback);
    }
    
    @Override
    public void endVoting(boolean cancel) {
        if (twitchVoter != null) {
            twitchVoter.endVoting(cancel);
        }
    }
    
    @Override
    public VoteOption[] getOptions() {
        if (twitchVoter == null) return new VoteOption[0];
        TwitchVoteOption[] twitchOptions = twitchVoter.getOptions();
        VoteOption[] options = new VoteOption[twitchOptions.length];
        for (int i = 0; i < twitchOptions.length; i++) {
            options[i] = new VoteOption();
            options[i].displayName = twitchOptions[i].displayName;
            options[i].voteCount = twitchOptions[i].voteCount;
        }
        return options;
    }
    
    @Override
    public int getSecondsRemaining() {
        return twitchVoter != null ? twitchVoter.getSecondsRemaining() : 0;
    }
    
    @Override
    public void addVoteListener(VoteListener listener) {
        TwitchVoter.registerListener(new TwitchVoteListener() {
            @Override
            public void onTwitchAvailable() {
                listener.onVoteAvailable();
            }
            
            @Override
            public void onTwitchUnavailable() {
                listener.onVoteUnavailable();
            }
        });
    }
    
    // LiveStreamConfig 实现
    @Override
    public String getUsername() {
        if (twitchPanel == null) return "";
        TwitchConfig config = twitchPanel.connection.getTwitchConfig();
        return (String)ReflectionHacks.getPrivate(config, TwitchConfig.class, "username");
    }
}
```

### 3.3 步骤 3：创建平台管理器

```java
package chronometry.livestream;

import java.util.Optional;

public class LiveStreamManager {
    private static LiveStreamConnection connection;
    private static LiveStreamVoter voter;
    private static LiveStreamConfig config;
    
    public static void initialize(String platform) {
        switch (platform.toLowerCase()) {
            case "twitch":
                initializeTwitch();
                break;
            case "youtube":
                initializeYouTube();
                break;
            case "bilibili":
                initializeBilibili();
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }
    
    private static void initializeTwitch() {
        // 使用现有的 Twitch Integration
        if (AbstractDungeon.topPanel != null && AbstractDungeon.topPanel.twitch.isPresent()) {
            TwitchAdapter adapter = new TwitchAdapter(
                AbstractDungeon.topPanel.twitch.get()
            );
            connection = adapter;
            voter = adapter;
            config = adapter;
        }
    }
    
    private static void initializeYouTube() {
        // 实现 YouTube 初始化
        YouTubeAdapter adapter = new YouTubeAdapter();
        connection = adapter;
        voter = adapter;
        config = adapter;
    }
    
    private static void initializeBilibili() {
        // 实现 Bilibili 初始化
        BilibiliAdapter adapter = new BilibiliAdapter();
        connection = adapter;
        voter = adapter;
        config = adapter;
    }
    
    public static Optional<LiveStreamConnection> getConnection() {
        return Optional.ofNullable(connection);
    }
    
    public static Optional<LiveStreamVoter> getVoter() {
        return Optional.ofNullable(voter);
    }
    
    public static Optional<LiveStreamConfig> getConfig() {
        return Optional.ofNullable(config);
    }
}
```

### 3.4 步骤 4：修改现有代码使用新接口

#### 示例：修改 SlayTheStreamer.java

```java
// 原代码：
import de.robojumper.ststwitch.TwitchMessageListener;
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteListener;
import de.robojumper.ststwitch.TwitchVoter;

// 新代码：
import chronometry.livestream.LiveStreamManager;
import chronometry.livestream.MessageListener;
import chronometry.livestream.VoteListener;

// 原代码：
TwitchVoter.registerListener(new TwitchVoteListener() {
    public void onTwitchAvailable() {
        StartGamePatch.updateVote();
    }
    public void onTwitchUnavailable() {
        StartGamePatch.updateVote();
    }
});

// 新代码：
LiveStreamManager.getVoter().ifPresent(voter -> {
    voter.addVoteListener(new VoteListener() {
        @Override
        public void onVoteAvailable() {
            StartGamePatch.updateVote();
        }
        
        @Override
        public void onVoteUnavailable() {
            StartGamePatch.updateVote();
        }
    });
});
```

#### 示例：修改 BossSelectScreen.java

```java
// 原代码：
public Optional<TwitchVoter> getVoter() { 
    return TwitchPanel.getDefaultVoter(); 
}

// 新代码：
public Optional<LiveStreamVoter> getVoter() { 
    return LiveStreamManager.getVoter(); 
}
```

### 3.5 步骤 5：实现新平台适配器

#### YouTube Live 适配器示例

```java
package chronometry.livestream.adapters;

import chronometry.livestream.*;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveChatMessage;
import com.google.api.services.youtube.model.LiveChatMessageListResponse;

public class YouTubeAdapter implements LiveStreamConnection, LiveStreamVoter, LiveStreamConfig {
    private YouTube youtubeService;
    private String liveChatId;
    private String nextPageToken;
    private boolean connected = false;
    private List<MessageListener> messageListeners = new ArrayList<>();
    private List<VoteListener> voteListeners = new ArrayList<>();
    
    // 投票状态
    private boolean voting = false;
    private VoteOption[] currentOptions;
    private int secondsRemaining;
    private Consumer<Integer> voteCallback;
    
    public YouTubeAdapter() {
        // 初始化 YouTube API 客户端
        // 需要 OAuth 2.0 认证
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public void sendMessage(String message) {
        // 使用 YouTube Live Chat API 发送消息
        // POST https://www.googleapis.com/youtube/v3/liveChat/messages
    }
    
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    // 启动消息轮询
    public void startMessagePolling() {
        new Thread(() -> {
            while (connected) {
                try {
                    LiveChatMessageListResponse response = youtubeService.liveChatMessages()
                        .list(liveChatId, "snippet,authorDetails")
                        .setPageToken(nextPageToken)
                        .execute();
                    
                    for (LiveChatMessage message : response.getItems()) {
                        String msg = message.getSnippet().getDisplayMessage();
                        String user = message.getAuthorDetails().getDisplayName();
                        
                        for (MessageListener listener : messageListeners) {
                            listener.onMessage(msg, user);
                        }
                    }
                    
                    nextPageToken = response.getNextPageToken();
                    Thread.sleep(response.getPollingIntervalMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    @Override
    public boolean isVotingConnected() {
        return connected;
    }
    
    @Override
    public boolean initiateSimpleNumberVote(String[] options, Consumer<Integer> callback) {
        // YouTube 没有原生投票 API，需要使用第三方服务或自定义实现
        // 可以使用 Super Chat 或自定义聊天命令
        this.currentOptions = new VoteOption[options.length];
        for (int i = 0; i < options.length; i++) {
            this.currentOptions[i] = new VoteOption();
            this.currentOptions[i].displayName = options[i];
            this.currentOptions[i].voteCount = 0;
        }
        this.voteCallback = callback;
        this.voting = true;
        this.secondsRemaining = 30;
        
        // 启动投票倒计时
        startVoteTimer();
        return true;
    }
    
    private void startVoteTimer() {
        new Thread(() -> {
            while (voting && secondsRemaining > 0) {
                try {
                    Thread.sleep(1000);
                    secondsRemaining--;
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (voting && voteCallback != null) {
                // 选择票数最多的选项
                int maxVotes = -1;
                int selectedOption = 0;
                for (int i = 0; i < currentOptions.length; i++) {
                    if (currentOptions[i].voteCount > maxVotes) {
                        maxVotes = currentOptions[i].voteCount;
                        selectedOption = i;
                    }
                }
                voteCallback.accept(selectedOption);
                voting = false;
            }
        }).start();
    }
    
    @Override
    public void endVoting(boolean cancel) {
        voting = false;
        if (cancel) {
            voteCallback = null;
        }
    }
    
    @Override
    public VoteOption[] getOptions() {
        return currentOptions;
    }
    
    @Override
    public int getSecondsRemaining() {
        return secondsRemaining;
    }
    
    @Override
    public void addVoteListener(VoteListener listener) {
        voteListeners.add(listener);
    }
    
    @Override
    public void removeVoteListener(VoteListener listener) {
        voteListeners.remove(listener);
    }
    
    @Override
    public String getUsername() {
        // 从 YouTube API 获取频道名称
        return "YouTubeChannel";
    }
}
```

#### Bilibili 直播适配器示例

```java
package chronometry.livestream.adapters;

import chronometry.livestream.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class BilibiliAdapter implements LiveStreamConnection, LiveStreamVoter, LiveStreamConfig {
    private WebSocket webSocket;
    private boolean connected = false;
    private List<MessageListener> messageListeners = new ArrayList<>();
    private List<VoteListener> voteListeners = new ArrayList<>();
    
    // 投票状态
    private boolean voting = false;
    private VoteOption[] currentOptions;
    private int secondsRemaining;
    private Consumer<Integer> voteCallback;
    
    private String roomId;
    private String userId;
    
    public BilibiliAdapter() {
        // 初始化 Bilibili 直播连接
        // 需要获取房间 ID 和用户认证
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public void sendMessage(String message) {
        // 使用 Bilibili 直播 API 发送消息
        // 需要登录凭证
    }
    
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    // 连接到 Bilibili 直播间 WebSocket
    public void connect(String roomId) {
        this.roomId = roomId;
        HttpClient client = HttpClient.newHttpClient();
        
        webSocket = client.newWebSocketBuilder()
            .buildAsync(URI.create("wss://broadcastlv.chat.bilibili.com/sub"), new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    connected = true;
                    // 发送认证包
                    sendAuthPacket();
                    WebSocket.Listener.super.onOpen(webSocket);
                }
                
                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    // 解析弹幕消息
                    parseDanmaku(data.toString());
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }
                
                @Override
                public void onClose(WebSocket webSocket, int statusCode, String reason) {
                    connected = false;
                    WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                }
            })
            .join();
    }
    
    private void sendAuthPacket() {
        // 发送 Bilibili 认证包
        // 包含房间 ID 和用户信息
    }
    
    private void parseDanmaku(String data) {
        try {
            JSONObject json = JSON.parseObject(data);
            String cmd = json.getString("cmd");
            
            if ("DANMU_MSG".equals(cmd)) {
                JSONObject info = json.getJSONArray("info").getJSONObject(0);
                String msg = info.getJSONArray(1).getString(1);
                JSONObject user = json.getJSONArray("info").getJSONObject(2);
                String username = user.getString(1);
                
                for (MessageListener listener : messageListeners) {
                    listener.onMessage(msg, username);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean isVotingConnected() {
        return connected;
    }
    
    @Override
    public boolean initiateSimpleNumberVote(String[] options, Consumer<Integer> callback) {
        // Bilibili 没有原生投票 API，需要使用弹幕投票
        this.currentOptions = new VoteOption[options.length];
        for (int i = 0; i < options.length; i++) {
            this.currentOptions[i] = new VoteOption();
            this.currentOptions[i].displayName = options[i];
            this.currentOptions[i].voteCount = 0;
        }
        this.voteCallback = callback;
        this.voting = true;
        this.secondsRemaining = 30;
        
        // 启动投票倒计时
        startVoteTimer();
        return true;
    }
    
    private void startVoteTimer() {
        new Thread(() -> {
            while (voting && secondsRemaining > 0) {
                try {
                    Thread.sleep(1000);
                    secondsRemaining--;
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (voting && voteCallback != null) {
                // 选择票数最多的选项
                int maxVotes = -1;
                int selectedOption = 0;
                for (int i = 0; i < currentOptions.length; i++) {
                    if (currentOptions[i].voteCount > maxVotes) {
                        maxVotes = currentOptions[i].voteCount;
                        selectedOption = i;
                    }
                }
                voteCallback.accept(selectedOption);
                voting = false;
            }
        }).start();
    }
    
    @Override
    public void endVoting(boolean cancel) {
        voting = false;
        if (cancel) {
            voteCallback = null;
        }
    }
    
    @Override
    public VoteOption[] getOptions() {
        return currentOptions;
    }
    
    @Override
    public int getSecondsRemaining() {
        return secondsRemaining;
    }
    
    @Override
    public void addVoteListener(VoteListener listener) {
        voteListeners.add(listener);
    }
    
    @Override
    public void removeVoteListener(VoteListener listener) {
        voteListeners.remove(listener);
    }
    
    @Override
    public String getUsername() {
        // 从 Bilibili API 获取用户名
        return "BilibiliUser";
    }
}
```

---

## 4. 配置文件修改

### 4.1 添加平台选择配置

在 `SlayTheStreamer.java` 的 `setDefaultPrefs()` 方法中添加：

```java
if (!config.has("LiveStreamPlatform")) 
    config.setString("LiveStreamPlatform", "twitch"); // twitch, youtube, bilibili
```

### 4.2 初始化平台

在 `receivePostInitialize()` 方法中添加：

```java
String platform = config.getString("LiveStreamPlatform");
LiveStreamManager.initialize(platform);
```

---

## 5. 依赖项

### 5.1 YouTube Live

需要添加以下依赖到 `pom.xml`：

```xml
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>1.35.2</version>
</dependency>
<dependency>
    <groupId>com.google.oauth-client</groupId>
    <artifactId>google-oauth-client-jetty</artifactId>
    <version>1.34.1</version>
</dependency>
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-youtube</artifactId>
    <version>v3-rev20231215-2.0.0</version>
</dependency>
```

### 5.2 Bilibili 直播

需要添加以下依赖到 `pom.xml`：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.43</version>
</dependency>
```

---

## 6. 测试清单

- [ ] 连接建立和断开
- [ ] 接收聊天消息
- [ ] 发送消息到聊天
- [ ] 发起投票
- [ ] 接收投票
- [ ] 获取投票结果
- [ ] 投票倒计时
- [ ] 用户名显示
- [ ] 断线重连
- [ ] 多平台切换

---

## 7. 注意事项

1. **投票功能差异：**
   - Twitch 有原生投票 API
   - YouTube 需要使用 Super Chat 或自定义实现
   - Bilibili 需要使用弹幕投票

2. **认证方式：**
   - Twitch: OAuth 令牌
   - YouTube: OAuth 2.0
   - Bilibili: Cookie/Session

3. **API 限制：**
   - 注意各平台的 API 调用频率限制
   - 处理 API 配额耗尽的情况

4. **消息格式：**
   - 不同平台的聊天消息格式可能不同
   - 需要统一处理表情、特殊字符等

5. **投票实现：**
   - 对于没有原生投票 API 的平台，需要实现自定义投票逻辑
   - 可以使用聊天命令、弹幕计数等方式

---

## 8. 总结

替换直播平台 API 的核心步骤：

1. **创建抽象接口层** - 定义统一的接口
2. **实现平台适配器** - 为每个平台创建适配器
3. **创建管理器** - 统一管理平台切换
4. **修改现有代码** - 使用新接口替代 Twitch 特定代码
5. **实现新平台** - 根据平台 API 实现适配器
6. **测试验证** - 确保所有功能正常工作

通过这种方式，可以轻松支持多个直播平台，同时保持代码的可维护性和可扩展性。

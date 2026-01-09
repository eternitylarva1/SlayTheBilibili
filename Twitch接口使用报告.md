# Twitch 相关类接口使用报告

## 概述

本报告详细说明了 Slay the Streamer 模组中所有使用 Twitch 相关类接口的地方。该模组主要依赖 `de.robojumper.ststwitch` 包中的 Twitch Integration 模组提供的接口。

---

## 0. 直播间连接获取方式

### 0.1 核心机制

**该模组不直接创建或管理 Twitch 连接，而是依赖 Twitch Integration 模组提供的连接。**

### 0.2 获取连接的方式

所有 Twitch 连接都通过以下方式获取：

```java
AbstractDungeon.topPanel.twitch
```

这是一个 `Optional<TwitchPanel>`，由 Twitch Integration 模组在游戏启动时初始化。

### 0.3 获取投票器

```java
Optional<TwitchVoter> voter = TwitchPanel.getDefaultVoter();
```

`TwitchPanel.getDefaultVoter()` 是一个静态方法，返回当前活动的投票器。

### 0.4 获取连接配置

在 [`MainMenuDisplayPatch.java`](src/main/java/chronometry/patches/MainMenuDisplayPatch.java:26-29) 中，代码获取 Twitch 配置信息：

```java
if (AbstractDungeon.topPanel.twitch.isPresent()) {
    TwitchConfig t = ((TwitchPanel)AbstractDungeon.topPanel.twitch.get()).connection.getTwitchConfig();
    String username = (String)ReflectionHacks.getPrivate(t, TwitchConfig.class, "username");
    // 显示用户名
}
```

### 0.5 连接初始化流程

1. **Twitch Integration 模组负责：**
   - 读取 Twitch OAuth 令牌
   - 连接到指定的 Twitch 频道
   - 初始化 `TwitchPanel` 和 `TwitchConnection`
   - 设置 IRC 连接（通过 Twirk 库）

2. **Slay the Streamer 模组负责：**
   - 检查 `AbstractDungeon.topPanel.twitch.isPresent()` 确认连接可用
   - 通过 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 注册监听器（`TwitchVoteListener`、`TwitchMessageListener`）
   - 使用连接发送消息和发起投票

### 0.6 关键点

- **频道配置：** 由 Twitch Integration 模组在其配置文件中管理，不在本模组中设置
- **OAuth 令牌：** 由 Twitch Integration 模组处理
- **连接状态：** 通过 `TwitchVoter.isVotingConnected()` 检查
- **用户名获取：** 通过反射从 `TwitchConfig` 获取私有字段 `username`

### 0.7 使用的 Twitch 相关类（连接相关）

| 类名 | 用途 |
|------|------|
| `TwitchPanel` | Twitch 面板，包含连接和投票器 |
| `TwitchConnection` | Twitch 连接对象，提供 IRC 功能 |
| `TwitchConfig` | Twitch 配置，包含用户名等信息 |
| `TwitchVoter` | 投票器，管理投票功能 |

---

---

## 1. 使用的 Twitch 相关类

### 1.1 来自 `de.robojumper.ststwitch` 包的类

| 类名 | 用途 |
|------|------|
| `TwitchPanel` | Twitch 面板，提供默认投票器 |
| `TwitchVoter` | 投票器，管理投票功能 |
| `TwitchVoteListener` | 投票监听器接口 |
| `TwitchVoteOption` | 投票选项类 |
| `TwitchMessageListener` | 消息监听器接口 |
| `TwitchConnection` | Twitch 连接类 |

### 1.2 来自 `com.gikk.twirk` 包的类

| 类名 | 用途 |
|------|------|
| `Twirk` | Twitch IRC 连接库 |
| `TwitchUser` | Twitch 用户信息类 |

---

## 2. 文件级别的使用情况

### 2.1 [`SlayTheStreamer.java`](src/main/java/chronometry/SlayTheStreamer.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchMessageListener;
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteListener;
import de.robojumper.ststwitch.TwitchVoter;
```

**使用位置：**

1. **第 93-103 行：注册投票监听器**
   - 使用 `TwitchVoter.registerListener()` 注册 `TwitchVoteListener`
   - 监听 `onTwitchAvailable()` 和 `onTwitchUnavailable()` 事件
   - 当 Twitch 可用/不可用时调用 `StartGamePatch.updateVote()`

2. **第 105-116 行：注册消息监听器**
   - 检查 `AbstractDungeon.topPanel.twitch` 是否存在
   - 使用反射获取 `TwitchConnection` 的 `listeners` 列表
   - 创建 `TwitchMessageListener` 监听聊天消息
   - 将消息传递给 `MonsterMessageRepeater.parseMessage()`

---

### 2.2 [`BossSelectScreen.java`](src/main/java/chronometry/BossSelectScreen.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteListener;
import de.robojumper.ststwitch.TwitchVoteOption;
import de.robojumper.ststwitch.TwitchVoter;
```

**使用位置：**

1. **第 81-90 行：构造函数中注册投票监听器**
   - 使用 `TwitchVoter.registerListener()` 注册 `TwitchVoteListener`
   - 监听 Twitch 可用性变化，调用 `updateVote()`

2. **第 283 行：检查 Twitch 是否可用**
   - 检查 `AbstractDungeon.topPanel.twitch.isPresent()` 或模拟模式

3. **第 330-354 行：渲染真实 Twitch 投票**
   - 使用 `getVoter()` 获取 `TwitchVoter`
   - 调用 `twitchVoter.getOptions()` 获取投票选项
   - 调用 `twitchVoter.getSecondsRemaining()` 获取剩余时间
   - 访问 `TwitchVoteOption.voteCount` 获取票数

4. **第 367 行：获取投票器**
   - `getVoter()` 方法返回 `TwitchPanel.getDefaultVoter()`

5. **第 384-396 行：更新投票状态**
   - 使用 `getVoter()` 获取 `TwitchVoter`
   - 调用 `twitchVoter.isVotingConnected()` 检查连接状态
   - 调用 `twitchVoter.initiateSimpleNumberVote()` 发起投票
   - 调用 `twitchVoter.endVoting()` 结束投票

6. **第 407-410 行：完成投票**
   - 使用 `getVoter()` 获取 `TwitchVoter`
   - 通过 `AbstractDungeon.topPanel.twitch` 发送消息到聊天
   - 调用 `twitchPanel.connection.sendMessage()`

---

### 2.3 [`CardRewardPatch.java`](src/main/java/chronometry/patches/CardRewardPatch.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteListener;
import de.robojumper.ststwitch.TwitchVoteOption;
import de.robojumper.ststwitch.TwitchVoter;
```

**使用位置：**

1. **第 61-75 行：渲染真实 Twitch 投票**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 调用 `twitchVoter.getOptions()` 获取选项
   - 调用 `twitchVoter.getSecondsRemaining()` 获取剩余时间
   - 访问 `TwitchVoteOption.voteCount` 获取票数

2. **第 96-104 行：构造函数中注册投票监听器**
   - 使用 `TwitchVoter.registerListener()` 注册 `TwitchVoteListener`
   - 监听 Twitch 可用性变化

3. **第 122-136 行：更新投票状态**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 调用 `twitchVoter.isVotingConnected()` 检查连接
   - 调用 `twitchVoter.initiateSimpleNumberVote()` 发起投票
   - 调用 `twitchVoter.endVoting()` 结束投票

4. **第 147-149 行：完成投票**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 通过 `AbstractDungeon.topPanel.twitch` 发送消息

---

### 2.4 [`NoSkipBossRelicPatch.java`](src/main/java/chronometry/patches/NoSkipBossRelicPatch.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteListener;
import de.robojumper.ststwitch.TwitchVoteOption;
import de.robojumper.ststwitch.TwitchVoter;
```

**使用位置：**

1. **第 54-63 行：构造函数中注册投票监听器**
   - 使用 `TwitchVoter.registerListener()` 注册 `TwitchVoteListener`

2. **第 72-96 行：渲染投票**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 调用 `twitchVoter.getOptions()` 获取选项
   - 调用 `twitchVoter.getSecondsRemaining()` 获取剩余时间

3. **第 115-129 行：更新投票状态**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 调用 `twitchVoter.isVotingConnected()` 检查连接
   - 调用 `twitchVoter.initiateSimpleNumberVote()` 发起投票
   - 调用 `twitchVoter.endVoting()` 结束投票

4. **第 140-142 行：完成投票**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取投票器
   - 通过 `AbstractDungeon.topPanel.twitch` 发送消息

---

### 2.5 [`StartGamePatch.java`](src/main/java/chronometry/patches/StartGamePatch.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteOption;
import de.robojumper.ststwitch.TwitchVoter;
```

**使用位置：**

1. **第 118-120 行：获取投票器**
   - `getVoter()` 方法返回 `TwitchPanel.getDefaultVoter()`

2. **第 127-138 行：更新投票状态**
   - 使用 `getVoter()` 获取 `TwitchVoter`
   - 调用 `twitchVoter.isVotingConnected()` 检查连接
   - 调用 `twitchVoter.initiateSimpleNumberVote()` 发起投票
   - 调用 `twitchVoter.endVoting()` 结束投票

3. **第 164-166 行：完成投票**
   - 使用 `getVoter()` 获取 `TwitchVoter`
   - 通过 `AbstractDungeon.topPanel.twitch` 发送消息

4. **第 211-228 行：渲染投票**
   - 使用 `getVoter()` 获取 `TwitchVoter`
   - 调用 `twitchVoter.getOptions()` 获取选项
   - 调用 `twitchVoter.getSecondsRemaining()` 获取剩余时间

---

### 2.6 [`MonsterNamesPatch.java`](src/main/java/chronometry/patches/MonsterNamesPatch.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoter;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.types.users.TwitchUser;
```

**使用位置：**

1. **第 51-56 行：获取投票用户名**
   - 使用 `TwitchPanel.getDefaultVoter()` 获取 `TwitchVoter`
   - 使用反射获取 `TwitchVoter` 的私有字段 `votedUsernames`

2. **第 153-158 行：存储 Twitch 显示名称**
   - 使用 `@SpirePatch` 修补 `Twirk.incommingMessage` 方法
   - 获取 `TwitchUser` 对象
   - 调用 `user.getUserName()` 和 `user.getDisplayName()`
   - 将用户名和显示名存储到 `SlayTheStreamer.displayNames`

---

### 2.7 [`MainMenuDisplayPatch.java`](src/main/java/chronometry/patches/MainMenuDisplayPatch.java)

**导入的 Twitch 类：**
```java
import de.robojumper.ststwitch.TwitchConfig;
import de.robojumper.ststwitch.TwitchPanel;
```

**使用位置：**

1. **第 26-29 行：获取 Twitch 配置和用户名**
   - 检查 `AbstractDungeon.topPanel.twitch.isPresent()`
   - 获取 `TwitchPanel` 对象
   - 调用 `connection.getTwitchConfig()` 获取配置
   - 使用反射获取 `TwitchConfig` 的私有字段 `username`
   - 在主菜单显示用户名

---

### 2.8 [`TwirkPatch.java`](src/main/java/chronometry/patches/TwirkPatch.java)

**导入的 Twitch 类：**
```java
import com.gikk.twirk.Twirk;
```

**使用位置：**

1. **第 14-23 行：修补 Twirk 资源创建**
   - 使用 `@SpirePatch` 修补 `Twirk.createResources` 方法
   - 检查 `bettertwitchmod` 是否已加载
   - 如果未加载，手动创建 `BufferedWriter` 和 `BufferedReader`
   - 用于避免与 Better Twitch Mod 冲突

---

### 2.9 [`MockTwitchHelper.java`](src/main/java/chronometry/MockTwitchHelper.java)

**说明：** 此文件不直接使用 Twitch 类，而是提供模拟接口，用于在没有真实 Twitch 连接时测试功能。

---

### 2.10 [`MonsterMessageRepeater.java`](src/main/java/chronometry/MonsterMessageRepeater.java)

**说明：** 此文件不直接使用 Twitch 类，但处理从 Twitch 消息监听器接收的消息。

---

## 3. 接口使用模式总结

### 3.1 投票功能模式

所有投票相关代码都遵循以下模式：

1. **获取投票器：**
   ```java
   Optional<TwitchVoter> voter = TwitchPanel.getDefaultVoter();
   ```

2. **检查连接状态：**
   ```java
   if (voter.isPresent() && voter.get().isVotingConnected()) {
       // 投票已连接
   }
   ```

3. **发起投票：**
   ```java
   String[] options = {"选项1", "选项2", "选项3"};
   boolean success = voter.get().initiateSimpleNumberVote(options, callback);
   ```

4. **获取投票数据：**
   ```java
   TwitchVoteOption[] options = voter.get().getOptions();
   int seconds = voter.get().getSecondsRemaining();
   int votes = options[i].voteCount;
   ```

5. **结束投票：**
   ```java
   voter.get().endVoting(true);
   ```

### 3.2 消息监听模式

1. **注册消息监听器：**
   ```java
   List<TwitchMessageListener> listeners = (List)ReflectionHacks.getPrivate(
       ((TwitchPanel)AbstractDungeon.topPanel.twitch.get()).connection,
       de.robojumper.ststwitch.TwitchConnection.class,
       "listeners"
   );
   listeners.add(new TwitchMessageListener() {
       public void onMessage(String msg, String user) {
           // 处理消息
       }
   });
   ```

### 3.3 投票监听模式

1. **注册投票监听器：**
   ```java
   TwitchVoter.registerListener(new TwitchVoteListener() {
       public void onTwitchAvailable() {
           // Twitch 可用时的处理
       }
       public void onTwitchUnavailable() {
           // Twitch 不可用时的处理
       }
   });
   ```

### 3.4 发送消息到聊天

```java
AbstractDungeon.topPanel.twitch.ifPresent(twitchPanel -> 
    twitchPanel.connection.sendMessage("消息内容")
);
```

---

## 4. 反射使用情况

代码中大量使用 `basemod.ReflectionHacks` 访问 Twitch 类的私有字段：

| 文件 | 行号 | 目标类 | 私有字段 | 用途 |
|------|------|--------|----------|------|
| `SlayTheStreamer.java` | 106 | `TwitchConnection` | `listeners` | 获取消息监听器列表 |
| `MonsterNamesPatch.java` | 54 | `TwitchVoter` | `votedUsernames` | 获取已投票用户名集合 |
| `MainMenuDisplayPatch.java` | 28 | `TwitchConfig` | `username` | 获取 Twitch 用户名 |

---

## 5. 依赖关系

### 5.1 外部依赖

- **Twitch Integration 模组** (`de.robojumper.ststwitch`)
  - 提供主要的投票和消息功能
  - 必须在 ModTheSpire 中加载

- **Twirk 库** (`com.gikk.twirk`)
  - 提供 Twitch IRC 连接功能
  - 用于获取用户显示名称

### 5.2 内部依赖

- `MockTwitchHelper` - 提供模拟模式，用于测试
- `MonsterMessageRepeater` - 处理聊天消息
- `SlayTheStreamer` - 主类，管理全局状态

---

## 6. 关键注意事项

1. **Twitch Integration 模组必须加载**
   - 所有代码都假设 Twitch Integration 模组已加载
   - 使用 `AbstractDungeon.topPanel.twitch.isPresent()` 检查可用性

2. **反射访问私有字段**
   - 使用 `ReflectionHacks` 访问私有字段
   - 这种方式脆弱，可能在 Twitch Integration 模组更新时失效

3. **模拟模式支持**
   - 所有投票功能都支持模拟模式
   - 通过 `MockTwitchHelper.isMockMode()` 检查

4. **避免与 Better Twitch Mod 冲突**
   - `TwirkPatch` 检查 `bettertwitchmod` 是否已加载
   - 避免重复修补 Twirk 类

---

## 7. 总结

该模组在以下 8 个文件中使用了 Twitch 相关接口：

1. **SlayTheStreamer.java** - 主类，注册全局监听器
2. **BossSelectScreen.java** - Boss 选择投票
3. **CardRewardPatch.java** - 卡牌奖励投票
4. **NoSkipBossRelicPatch.java** - Boss 遗物投票
5. **StartGamePatch.java** - Neow 祝福投票
6. **MonsterNamesPatch.java** - 怪物命名和用户名存储
7. **MainMenuDisplayPatch.java** - 主菜单显示 Twitch 用户名
8. **TwirkPatch.java** - Twirk 兼容性修补

主要使用的接口包括：
- `TwitchVoter` - 投票管理
- `TwitchVoteListener` - 投票状态监听
- `TwitchMessageListener` - 聊天消息监听
- `TwitchPanel` - 获取默认投票器
- `Twirk` - IRC 连接和用户信息

所有接口都通过 `de.robojumper.ststwitch` 包提供，依赖 Twitch Integration 模组。

# 修改 StartGamePatch.java 以添加模拟模式支持

# 读取文件内容
$content = Get-Content "c:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar.src\src\main\java\chronometry\patches\StartGamePatch.java"

# 在 updateVote() 方法中添加模拟模式支持
# 找到 "if (getVoter().isPresent()) {" 后面添加模拟模式代码
$oldCode = "    } `r`n    } `r`n  "
$newCode = "    } `r`n    // 如果没有真实的 TwitchVoter，使用模拟模式`r`n    else if (MockTwitchHelper.isMockMode()) {`r`n      if (mayVote && MockTwitchHelper.isVotingConnected() && !isVoting) {`r`n        isVoting = MockTwitchHelper.initiateSimpleNumberVote(neowOptions, StartGamePatch::completeVoting);`r`n      }`r`n      else if (isVoting && (!mayVote || !MockTwitchHelper.isVotingConnected())) {`r`n        MockTwitchHelper.endVoting(true);`r`n        isVoting = false;`r`n      }`r`n    }`r`n  "

$content = $content.Replace($oldCode, $newCode)

# 在 renderTwitchVotes() 方法中添加模拟模式支持
$oldRenderCode = "      .cpy());`r`n    } `r`n  } `r`n  "
$newRenderCode = "      .cpy());`r`n    } `r`n    // 如果没有真实的 TwitchVoter，使用模拟模式`r`n    else if (MockTwitchHelper.isMockMode()) {`r`n      String[] options = MockTwitchHelper.getCurrentOptions();`r`n      int[] voteCounts = MockTwitchHelper.getMockVoteCounts();`r`n      if (options != null && voteCounts != null) {`r`n        int sum = 0;`r`n        for (int count : voteCounts) {`r`n          sum += count;`r`n        }`r`n        for (int i = 0; i < options.length && i < 4; i++) {`r`n          String s = `"#`" + i + `": "` + voteCounts[i];`r`n          if (sum > 0) {`r`n            s = s + `" ("` + (voteCounts[i] * 100 / sum) + `"%)";`r`n          }`r`n          `r`n          float y = Settings.OPTION_Y - 500.0F * Settings.scale;`r`n          y += i * -82.0F * Settings.scale;`r`n          y -= -328.0F * Settings.scale;`r`n          `r`n          `r`n          FontHelper.renderFontRightAligned(sb, FontHelper.panelEndTurnFont, s, 160.0F * Settings.scale, y, Color.WHITE.cpy());`r`n        } `r`n        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, (CardCrawlGame.languagePack.getUIString(`"versus:ForPlayer`")).TEXT[2] + MockTwitchHelper.getSecondsRemaining() + (CardCrawlGame.languagePack.getUIString(`"versus:ForPlayer`")).TEXT[3], 340.0F * Settings.scale, 77.0F * Settings.scale + 328.0F * Settings.scale, Color.WHITE`r`n          .cpy());`r`n      }`r`n    }`r`n  "

$content = $content.Replace($oldRenderCode, $newRenderCode)

# 写回文件
Set-Content -Path "c:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar.src\src\main\java\chronometry\patches\StartGamePatch.java" -Value $content

Write-Host "StartGamePatch.java 修改完成"

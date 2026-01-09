package chronometry.patches;

import basemod.ReflectionHacks;
import chronometry.MockTwitchHelper;
import chronometry.SlayTheStreamer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.neow.NeowReward;
import com.megacrit.cardcrawl.neow.NeowRoom;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import de.robojumper.ststwitch.TwitchPanel;
import de.robojumper.ststwitch.TwitchVoteOption;
import de.robojumper.ststwitch.TwitchVoter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class StartGamePatch {
  public static boolean mayVote = false;
  public static boolean isVoting = false;
  public static String[] neowOptions;
  public static int option;
  
  @SpirePatch(clz = NeowEvent.class, method = "blessing")
  public static class SetDeckBlessing {
    public static SpireReturn Prefix(NeowEvent self) {
      if (!SlayTheStreamer.config.getBool("VoteOnNeow")) {
        StartGamePatch.chooseCards(self);
        return SpireReturn.Return(null);
      }
      return SpireReturn.Continue();
    }
    
    public static void Postfix(NeowEvent self) {
      SlayTheStreamer.log("SetDeckBlessing.Postfix called");
      if (SlayTheStreamer.config.getBool("VoteOnNeow")) {
        SlayTheStreamer.log("VoteOnNeow is enabled");
        StartGamePatch.mayVote = true;
        
        ArrayList<NeowReward> neowRewards = (ArrayList)ReflectionHacks.getPrivate(self, NeowEvent.class, "rewards");
        StartGamePatch.neowOptions = new String[neowRewards.size()];
        int i = 0;
        
        for (NeowReward nr : neowRewards) {
          String voteOption = nr.optionLabel;
          
          if (voteOption.contains("#r")) {
            voteOption = voteOption.replaceFirst(" #g", ", ").replace("#g", "").replace("#r", "").replace("[ ", "").replace(" ]", "").replace("]", "").replace("[", "").replace(".,", ",");
          }
          else {
            voteOption = voteOption.replace("#g", "").replace("#r", "").replace("[ ", "").replace(" ]", "");
          }
          
          StartGamePatch.neowOptions[i] = voteOption;
          i++;
        }
        
        SlayTheStreamer.log("Neow options: " + Arrays.toString(neowOptions));
        StartGamePatch.updateVote();
      }
    }
  }
  
  @SpirePatch(clz = NeowRoom.class, method = "renderAboveTopPanel")
  public static class RenderNeowVotes {
    public static SpireReturn<Void> Prefix(NeowRoom self, SpriteBatch sb) {
      StartGamePatch.renderTwitchVotes(sb);
      return SpireReturn.Return(null);
    }
  }
  
  @SpirePatch(clz = NeowRoom.class, method = "onPlayerEntry")
  public static class OnNeowRoomEntry {
    public static void Postfix(NeowRoom self) {
      SlayTheStreamer.log("NeowRoom.onPlayerEntry called");
      // 尝试手动启动投票
      if (SlayTheStreamer.config.getBool("VoteOnNeow") && !isVoting) {
        SlayTheStreamer.log("Manually starting Neow voting");
        mayVote = true;
        // 获取 Neow 事件中的奖励选项
        if (self.event instanceof NeowEvent) {
          NeowEvent neowEvent = (NeowEvent)self.event;
          ArrayList<NeowReward> neowRewards = (ArrayList)ReflectionHacks.getPrivate(neowEvent, NeowEvent.class, "rewards");
          if (neowRewards != null && !neowRewards.isEmpty()) {
            neowOptions = new String[neowRewards.size()];
            int i = 0;
            for (NeowReward nr : neowRewards) {
              String voteOption = nr.optionLabel;
              if (voteOption.contains("#r")) {
                voteOption = voteOption.replaceFirst(" #g", ", ").replace("#g", "").replace("#r", "").replace("[ ", "").replace(" ]", "").replace("]", "").replace("[", "").replace(".,", ",");
              }
              else {
                voteOption = voteOption.replace("#g", "").replace("#r", "").replace("[ ", "").replace(" ]", "");
              }
              neowOptions[i] = voteOption;
              i++;
            }
            SlayTheStreamer.log("Neow options from room entry: " + Arrays.toString(neowOptions));
            updateVote();
          }
        }
      }
    }
  }
  
  public static Optional<TwitchVoter> getVoter() {
    return TwitchPanel.getDefaultVoter();
  }
  
  public static void updateVote() {
    SlayTheStreamer.log("updateVote called - mayVote: " + mayVote + ", isVoting: " + isVoting);
    SlayTheStreamer.log("MockTwitchHelper.isMockMode(): " + MockTwitchHelper.isMockMode());
    SlayTheStreamer.log("getVoter().isPresent(): " + getVoter().isPresent());
    
    if (getVoter().isPresent()) {
      SlayTheStreamer.log("Using real Twitch voter");
      TwitchVoter twitchVoter = (TwitchVoter)getVoter().get();
      if (mayVote && twitchVoter.isVotingConnected() && !isVoting) {
        isVoting = twitchVoter.initiateSimpleNumberVote(neowOptions, StartGamePatch::completeVoting);
        SlayTheStreamer.log("Real voting started, isVoting: " + isVoting);
      }
      else if (isVoting && (!mayVote || !twitchVoter.isVotingConnected())) {
        twitchVoter.endVoting(true);
        isVoting = false;
        SlayTheStreamer.log("Real voting ended");
      }
    }
    else if (MockTwitchHelper.isMockMode()) {
      SlayTheStreamer.log("Using mock Twitch mode");
      SlayTheStreamer.log("MockTwitchHelper.isVotingConnected(): " + MockTwitchHelper.isVotingConnected());
      if (mayVote && MockTwitchHelper.isVotingConnected() && !isVoting) {
        SlayTheStreamer.log("Starting mock voting with options: " + Arrays.toString(neowOptions));
        isVoting = MockTwitchHelper.initiateSimpleNumberVote(neowOptions, StartGamePatch::completeVoting);
        SlayTheStreamer.log("Mock voting started, isVoting: " + isVoting);
      }
      else if (isVoting && (!mayVote || !MockTwitchHelper.isVotingConnected())) {
        MockTwitchHelper.endVoting(true);
        isVoting = false;
        SlayTheStreamer.log("Mock voting ended");
      }
    }
    else {
      SlayTheStreamer.log("No voter available and mock mode is disabled");
    }
  }
  
  public static void completeVoting(int option) {
    if (!isVoting) {
      return;
    }
    isVoting = false;
    if (getVoter().isPresent()) {
      TwitchVoter twitchVoter = (TwitchVoter)getVoter().get();
      AbstractDungeon.topPanel.twitch.ifPresent(twitchPanel -> twitchPanel.connection.sendMessage((CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[5] + (twitchVoter.getOptions()[option]).displayName));
    }
    else if (MockTwitchHelper.isMockMode()) {
      SlayTheStreamer.log("Mock voting completed - selected option #" + option);
    }
    
    SlayTheStreamer.log("Voting Ended");
    StartGamePatch.option = option;
    
    try {
      Method m = NeowEvent.class.getDeclaredMethod("dismissBubble", new Class[0]);
      m.setAccessible(true);
      m.invoke((AbstractDungeon.getCurrRoom()).event, new Object[0]);
    }
    catch (Throwable throwable) {}
    ((NeowEvent)(AbstractDungeon.getCurrRoom()).event).roomEventText.clearRemainingOptions();
    
    chooseCards((NeowEvent)(AbstractDungeon.getCurrRoom()).event);
  }
  
  @SpirePatch(clz = NeowEvent.class, method = "update")
  public static class WaitForDeckConstruction {
    public static void Prefix(NeowEvent self) {
      if ((AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMPLETE) {
        StartGamePatch.mayVote = false;
        StartGamePatch.updateVote();
      }
      
      if (SlayTheStreamer.config.getBool("VoteOnNeow")) {
        if (!StartGamePatch.isVoting && !AbstractDungeon.gridSelectScreen.confirmScreenUp && AbstractDungeon.gridSelectScreen.isJustForConfirming && ((Integer)ReflectionHacks.getPrivate(self, NeowEvent.class, "screenNum")).intValue() == 3) {
          SlayTheStreamer.log("ADVANCING OPTION - ACTIVATING NEW REWARDS ~~~~~~~~~~~~~~~~~~~~~~~~");
          RoomEventDialog.waitForInput = false;
          RoomEventDialog.selectedOption = StartGamePatch.option;
          ReflectionHacks.setPrivate(self, NeowEvent.class, "pickCard", Boolean.valueOf(false));
        }
      }
    }
  }
  
  public static void renderTwitchVotes(SpriteBatch sb) {
    SlayTheStreamer.log("renderTwitchVotes called - isVoting: " + isVoting);
    if (!isVoting) {
      return;
    }
    SlayTheStreamer.log("renderTwitchVotes - isVoting is true, checking voter");
    if (getVoter().isPresent()) {
      TwitchVoter twitchVoter = (TwitchVoter)getVoter().get();
      TwitchVoteOption[] options = twitchVoter.getOptions();
      int sum = ((Integer)Arrays.stream(options).map(c -> Integer.valueOf(c.voteCount)).reduce(Integer.valueOf(0), Integer::sum)).intValue();
      for (int i = 0; i < 4; i++) {
        String s = "#" + i + ": " + (options[i]).voteCount;
        if (sum > 0) {
          s = s + " (" + ((options[i]).voteCount * 100 / sum) + "%)";
        }
        
        float y = Settings.OPTION_Y - 500.0F * Settings.scale;
        y += i * -82.0F * Settings.scale;
        y -= -328.0F * Settings.scale;
        
        FontHelper.renderFontRightAligned(sb, FontHelper.panelEndTurnFont, s, 160.0F * Settings.scale, y, Color.WHITE.cpy());
      }
      FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[2] + twitchVoter.getSecondsRemaining() + (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[3], 340.0F * Settings.scale, 77.0F * Settings.scale + 328.0F * Settings.scale, Color.WHITE.cpy());
    }
    else if (MockTwitchHelper.isMockMode()) {
      SlayTheStreamer.log("Rendering mock Twitch votes");
      int[] voteCounts = MockTwitchHelper.getMockVoteCounts();
      SlayTheStreamer.log("voteCounts: " + (voteCounts == null ? "null" : Arrays.toString(voteCounts)));
      if (voteCounts != null) {
        int sum = 0;
        for (int count : voteCounts) {
          sum += count;
        }
        for (int i = 0; i < voteCounts.length; i++) {
          String s = "#" + i + ": " + voteCounts[i];
          if (sum > 0) {
            s = s + " (" + (voteCounts[i] * 100 / sum) + "%)";
          }
          
          float y = Settings.OPTION_Y - 500.0F * Settings.scale;
          y += i * -82.0F * Settings.scale;
          y -= -328.0F * Settings.scale;
          
          FontHelper.renderFontRightAligned(sb, FontHelper.panelEndTurnFont, s, 160.0F * Settings.scale, y, Color.WHITE.cpy());
        }
        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[2] + MockTwitchHelper.getSecondsRemaining() + (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[3], 340.0F * Settings.scale, 77.0F * Settings.scale + 328.0F * Settings.scale, Color.WHITE.cpy());
      }
    }
  }
  
  static void chooseCards(NeowEvent self) {
    ReflectionHacks.setPrivate(self, NeowEvent.class, "pickCard", Boolean.valueOf(true));
    
    AbstractDungeon.player.masterDeck.group.removeIf(c -> !c.cardID.equals("AscendersBane"));
    
    CardGroup sealedGroup = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
    
    for (int rares = 0; rares < SlayTheStreamer.config.getInt("GuaranteedRares"); rares++) {
      AbstractCard card = AbstractDungeon.getCard(AbstractCard.CardRarity.RARE);
      sealedGroup.addToBottom(card.makeCopy());
    }
    
    for (int uncommons = 0; uncommons < SlayTheStreamer.config.getInt("GuaranteedUncommons"); uncommons++) {
      AbstractCard card = AbstractDungeon.getCard(AbstractCard.CardRarity.UNCOMMON);
      sealedGroup.addToBottom(card.makeCopy());
    }
    
    for (int commons = 0; commons < SlayTheStreamer.config.getInt("GuaranteedCommons"); commons++) {
      AbstractCard card = AbstractDungeon.getCard(AbstractCard.CardRarity.COMMON);
      sealedGroup.addToBottom(card.makeCopy());
    }
    
    if (sealedGroup.size() < SlayTheStreamer.config.getInt("CardPickPool")) {
      int size = sealedGroup.size();
      for (int i = 0; i < SlayTheStreamer.config.getInt("CardPickPool") - size; i++) {
        AbstractCard card = AbstractDungeon.getCard(AbstractDungeon.rollRarity());
        if (!sealedGroup.contains(card)) {
          sealedGroup.addToBottom(card.makeCopy());
        }
        else {
          i--;
          SlayTheStreamer.log("WUTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
        }
      }
    }
    
    for (AbstractCard c : sealedGroup.group) {
      UnlockTracker.markCardAsSeen(c.cardID);
    }
    
    AbstractDungeon.gridSelectScreen.open(sealedGroup, SlayTheStreamer.config.getInt("CardPickChoices"), (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[9] + SlayTheStreamer.config.getInt("CardPickChoices") + (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[10], false);
  }
}

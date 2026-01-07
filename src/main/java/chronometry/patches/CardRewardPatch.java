 package chronometry.patches;
 
 import chronometry.MockTwitchHelper;
 import chronometry.SlayTheStreamer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
 import com.megacrit.cardcrawl.cards.AbstractCard;
 import com.megacrit.cardcrawl.core.CardCrawlGame;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.rewards.RewardItem;
 import com.megacrit.cardcrawl.screens.CardRewardScreen;
 import de.robojumper.ststwitch.TwitchPanel;
 import de.robojumper.ststwitch.TwitchVoteListener;
 import de.robojumper.ststwitch.TwitchVoteOption;
 import de.robojumper.ststwitch.TwitchVoter;
 import java.util.ArrayList;
import java.util.Arrays;
 
 public class CardRewardPatch
 {
   static boolean isVoting = false;
   static boolean mayVote = false;

   @SpirePatch(clz = CardRewardScreen.class, method = "renderTwitchVotes")
   public static class renderTwitchVotes {
     public static void Postfix(CardRewardScreen self, SpriteBatch sb) {
       sb.draw(SlayTheStreamer.startScreenImage, Settings.WIDTH / 2.0F, 0.0F, SlayTheStreamer.startScreenImage
           
           .getWidth() * Settings.scale, SlayTheStreamer.startScreenImage
           .getHeight() * Settings.scale);
       if (!isVoting) {
         SlayTheStreamer.log("CardRewardPatch: Twitch not active");
         return;
       }
       if (MockTwitchHelper.isMockMode()) {
         SlayTheStreamer.log("CardRewardPatch: Rendering mock Twitch votes");
         String[] options = MockTwitchHelper.getCurrentOptions();
         int[] voteCounts = MockTwitchHelper.getMockVoteCounts();
         if (options == null || voteCounts == null) {
           SlayTheStreamer.log("CardRewardPatch: Mock options or voteCounts is null");
           return;
         }
         int sum = 0;
         for (int count : voteCounts) {
           sum += count;
         }
         for (int i = 0; i < options.length; i++) {
           String s = "#" + i + ": " + voteCounts[i];
           if (sum > 0) {
             s = s + " (" + (voteCounts[i] * 100 / sum) + "%)";
           }
           float x = 400.0F * Settings.scale + i * 400.0F * Settings.scale;
           float y = 700.0F * Settings.scale;
           FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, x, y, Color.WHITE.cpy());
         }
         FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[2] + MockTwitchHelper.getSecondsRemaining() + (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[3], Settings.WIDTH / 2.0F, 192.0F * Settings.scale, Color.WHITE.cpy());
       } else if (TwitchPanel.getDefaultVoter().isPresent()) {
         SlayTheStreamer.log("CardRewardPatch: Rendering real Twitch votes");
         TwitchVoter twitchVoter = (TwitchVoter)TwitchPanel.getDefaultVoter().get();
         TwitchVoteOption[] options = twitchVoter.getOptions();
         int sum = ((Integer)Arrays.stream(options).map(c -> Integer.valueOf(c.voteCount)).reduce(Integer.valueOf(0), Integer::sum)).intValue();
         for (int i = 0; i < options.length; i++) {
           String s = "#" + i + ": " + (options[i]).voteCount;
           if (sum > 0) {
             s = s + " (" + ((options[i]).voteCount * 100 / sum) + "%)";
           }
           float x = 400.0F * Settings.scale + i * 400.0F * Settings.scale;
           float y = 700.0F * Settings.scale;
           FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, x, y, Color.WHITE.cpy());
         }
         FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[2] + twitchVoter.getSecondsRemaining() + (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[3], Settings.WIDTH / 2.0F, 192.0F * Settings.scale, Color.WHITE.cpy());
       } else {
         SlayTheStreamer.log("CardRewardPatch: No voter available and mock mode is disabled");
       }
     }
   }

   @SpirePatch(clz = CardRewardScreen.class, method = "open")
   public static class openHook
   {
     public static void Postfix(CardRewardScreen self, ArrayList<AbstractCard> cards, RewardItem rItem, String header) {
       AbstractDungeon.dynamicBanner.appear((CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[0]);
       mayVote = true;
       SlayTheStreamer.cardRewardPatch.updateVote();
     }
   }

   public CardRewardPatch() {
      TwitchVoter.registerListener(new TwitchVoteListener()
         {
           public void onTwitchAvailable() {
              updateVote();
           }

           
           public void onTwitchUnavailable() { updateVote(); }
         });
   }

   public void updateVote() {
      SlayTheStreamer.log("CardRewardPatch.updateVote called - mayVote: " + mayVote + ", isVoting: " + isVoting);
      if (MockTwitchHelper.isMockMode()) {
        SlayTheStreamer.log("CardRewardPatch: Mock mode enabled");
        if (mayVote && !isVoting) {
          String[] cardList = new String[AbstractDungeon.cardRewardScreen.rewardGroup.size()];
          for (int i = 0; i < AbstractDungeon.cardRewardScreen.rewardGroup.size(); i++) {
            cardList[i] = ((AbstractCard)AbstractDungeon.cardRewardScreen.rewardGroup.get(i)).name;
          }
          isVoting = MockTwitchHelper.initiateSimpleNumberVote(cardList, this::completeVoting);
          SlayTheStreamer.log("CardRewardPatch: Mock voting started with " + cardList.length + " options");
        } else if (isVoting && !mayVote) {
          isVoting = false;
          SlayTheStreamer.log("CardRewardPatch: Mock voting ended");
        }
      } else if (TwitchPanel.getDefaultVoter().isPresent()) {
        SlayTheStreamer.log("CardRewardPatch: Real Twitch mode");
        TwitchVoter twitchVoter = (TwitchVoter)TwitchPanel.getDefaultVoter().get();
        if (mayVote && twitchVoter.isVotingConnected()) { if (!isVoting) {
            String[] cardList = new String[AbstractDungeon.cardRewardScreen.rewardGroup.size()];
            for (int i = 0; i < AbstractDungeon.cardRewardScreen.rewardGroup.size(); i++) {
              cardList[i] = ((AbstractCard)AbstractDungeon.cardRewardScreen.rewardGroup.get(i)).name;
            }
            
            isVoting = twitchVoter.initiateSimpleNumberVote(cardList, this::completeVoting); return;
          }  }
         if (isVoting) { if (!mayVote || !twitchVoter.isVotingConnected()) {
            twitchVoter.endVoting(true);
            isVoting = false;
          }  }
      } else {
        SlayTheStreamer.log("CardRewardPatch: No voter available and mock mode is disabled");
      }
   }

   public void completeVoting(int option) {
      if (!isVoting) {
        return;
      }
      isVoting = false;
      if (TwitchPanel.getDefaultVoter().isPresent()) {
        TwitchVoter twitchVoter = (TwitchVoter)TwitchPanel.getDefaultVoter().get();
        AbstractDungeon.topPanel.twitch.ifPresent(twitchPanel -> twitchPanel.connection.sendMessage((CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[6] + (twitchVoter.getOptions()[option]).displayName));
      }
      
      mayVote = false;
      updateVote();
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\CardRewardPatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */
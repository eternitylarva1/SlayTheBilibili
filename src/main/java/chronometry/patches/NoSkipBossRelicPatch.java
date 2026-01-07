 package chronometry.patches;
 
 import chronometry.MockTwitchHelper;
 import chronometry.SlayTheStreamer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
 import com.megacrit.cardcrawl.core.CardCrawlGame;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.relics.AbstractRelic;
 import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
 import de.robojumper.ststwitch.TwitchPanel;
 import de.robojumper.ststwitch.TwitchVoteListener;
 import de.robojumper.ststwitch.TwitchVoteOption;
 import de.robojumper.ststwitch.TwitchVoter;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class NoSkipBossRelicPatch
 {
   static boolean isVoting = false;
   static boolean mayVote = false;
   
   @SpirePatch(clz = BossRelicSelectScreen.class, method = "renderTwitchVotes")
   public static class RenderHook
   {
     public static void Prefix(BossRelicSelectScreen self, SpriteBatch sb) {
       SlayTheStreamer.noSkip.RenderVote(sb);
       SpireReturn.Return(null);
     }
   }
   
   @SpirePatch(clz = BossRelicSelectScreen.class, method = "updateVote")
   public static class updateHook
   {
     public static void Replace(BossRelicSelectScreen self) { SlayTheStreamer.noSkip.updateVote(); }
   }
 
   
   @SpirePatch(clz = BossRelicSelectScreen.class, method = "open")
   public static class openHook
   {
     public static void Postfix(BossRelicSelectScreen self, ArrayList<AbstractRelic> chosenRelics) {
       AbstractDungeon.dynamicBanner.appear(800.0F * Settings.scale, (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[7]);
       NoSkipBossRelicPatch.mayVote = true;
       SlayTheStreamer.noSkip.updateVote();
     }
   }
   
   public NoSkipBossRelicPatch() {
     TwitchVoter.registerListener(new TwitchVoteListener()
         {
           public void onTwitchAvailable() {
             SlayTheStreamer.noSkip.updateVote();
           }
 
 
           
           public void onTwitchUnavailable() { SlayTheStreamer.noSkip.updateVote(); }
         });
   }
 
   
   public void RenderVote(SpriteBatch sb) {
     sb.draw(SlayTheStreamer.startScreenImage, Settings.WIDTH / 2.0F, 0.0F);
     if (!isVoting) {
       return;
     }
     if (TwitchPanel.getDefaultVoter().isPresent()) {
       TwitchVoter twitchVoter = (TwitchVoter)TwitchPanel.getDefaultVoter().get();
       TwitchVoteOption[] options = twitchVoter.getOptions();
       int sum = ((Integer)Arrays.stream(options).map(c -> Integer.valueOf(c.voteCount)).reduce(Integer.valueOf(0), Integer::sum)).intValue();
       for (int i = 0; i < AbstractDungeon.bossRelicScreen.relics.size(); i++) {
         String s = "#" + i + ": " + (options[i]).voteCount;
         if (sum > 0) {
           s = s + " (" + ((options[i]).voteCount * 100 / sum) + "%)";
         }
         switch (i) {
           case 0:
             FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, 964.0F * Settings.scale, 700.0F * Settings.scale - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;
           
           case 1:
             FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, 844.0F * Settings.scale, 560.0F * Settings.scale - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;
           
           case 2:
             FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, 1084.0F * Settings.scale, 560.0F * Settings.scale - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;
         } 
       
       } 
       FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, BossRelicSelectScreen.TEXT[4] + twitchVoter.getSecondsRemaining() + BossRelicSelectScreen.TEXT[5], Settings.WIDTH / 2.0F, 192.0F * Settings.scale, Color.WHITE.cpy());
     } 
   }
   
   public void updateVote() {
      SlayTheStreamer.log("NoSkipBossRelicPatch.updateVote called - mayVote: " + mayVote + ", isVoting: " + isVoting);
      if (MockTwitchHelper.isMockMode()) {
        SlayTheStreamer.log("NoSkipBossRelicPatch: Mock mode enabled");
        if (mayVote && !isVoting) {
          String[] relicList = new String[3];
          for (int i = 0; i < AbstractDungeon.bossRelicScreen.relics.size(); i++) {
            relicList[i] = ((AbstractRelic)AbstractDungeon.bossRelicScreen.relics.get(i)).toString();
          }
          isVoting = MockTwitchHelper.initiateSimpleNumberVote(relicList, this::completeVoting);
          SlayTheStreamer.log("NoSkipBossRelicPatch: Mock voting started with " + relicList.length + " options");
        } else if (isVoting && !mayVote) {
          isVoting = false;
          SlayTheStreamer.log("NoSkipBossRelicPatch: Mock voting ended");
        }
      } else if (TwitchPanel.getDefaultVoter().isPresent()) {
        SlayTheStreamer.log("NoSkipBossRelicPatch: Real Twitch mode");
        TwitchVoter twitchVoter = (TwitchVoter)TwitchPanel.getDefaultVoter().get();
        if (mayVote && twitchVoter.isVotingConnected()) { if (!isVoting) {
            String[] relicList = new String[3];
            for (int i = 0; i < AbstractDungeon.bossRelicScreen.relics.size(); i++) {
              relicList[i] = ((AbstractRelic)AbstractDungeon.bossRelicScreen.relics.get(i)).toString();
            }
            
            isVoting = twitchVoter.initiateSimpleNumberVote(relicList, this::completeVoting); return;
          }  }
         if (isVoting) { if (!mayVote || !twitchVoter.isVotingConnected()) {
            twitchVoter.endVoting(true);
            isVoting = false;
          }  }
      } else {
        SlayTheStreamer.log("NoSkipBossRelicPatch: No voter available and mock mode is disabled");
      }
    }
   public void completeVoting(int option) {
     if (!isVoting) {
       return;
     }
     isVoting = false;
     if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.BOSS_REWARD) {
       if (TwitchPanel.getDefaultVoter().isPresent()) {
         TwitchVoter twitchVoter = (TwitchVoter)TwitchPanel.getDefaultVoter().get();
         AbstractDungeon.topPanel.twitch.ifPresent(twitchPanel -> twitchPanel.connection.sendMessage((CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[8] + (twitchVoter.getOptions()[option]).displayName));
       } 
       while (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.BOSS_REWARD) {
         AbstractDungeon.closeCurrentScreen();
       }
       AbstractRelic r = (AbstractRelic)AbstractDungeon.bossRelicScreen.relics.get(option);
       if (!r.relicId.equals("Black Blood") && !r.relicId.equals("Ring of the Serpent")) {
         r.obtain();
       }
       r.isObtained = true;
     } 
     
     mayVote = false;
     updateVote();
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\NoSkipBossRelicPatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */

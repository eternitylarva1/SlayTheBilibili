package chronometry.patches;

import chronometry.BossSelectRoom;
import chronometry.SlayTheStreamer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
 
 public class BossChoicePatch {
   @SpireEnum
   public static AbstractDungeon.CurrentScreen BOSS_SELECT;
   
   @SpirePatch(clz = AbstractDungeon.class, method = "setBoss")
   public static class HideBoss {
     public static SpireReturn Prefix(AbstractDungeon self, String key) {
       if (SlayTheStreamer.config.getBool("VoteOnBosses") && 
         SlayTheStreamer.bossHidden) {
         AbstractDungeon.bossKey = key;
         DungeonMap.boss = ImageMaster.loadImage("versusImages/unknownBoss.png");
         DungeonMap.bossOutline = ImageMaster.loadImage("versusImages/unknownBossOutline.png");
         return SpireReturn.Return(null);
       } 
       
       return SpireReturn.Continue();
     }
   }
   
   @SpirePatch(clz = ProceedButton.class, method = "goToNextDungeon")
   public static class HideBossAtDungeonStart
   {
     public static void Postfix(ProceedButton self, AbstractRoom room) {
       if (SlayTheStreamer.config.getBool("VoteOnBosses") && (
         Settings.isEndless || !AbstractDungeon.id.equals("TheEnding"))) {
         SlayTheStreamer.bossHidden = true;
       }
     }
   }
 
   
   @SpirePatch(clz = AbstractCreature.class, method = "loadAnimation")
   public static class MakeBossesCute
   {
     public static void Prefix(AbstractCreature self, String atlasUrl, String skeletonUrl, @ByRef float[] scale) {
       if (AbstractDungeon.screen == BossChoicePatch.BOSS_SELECT) {
         scale[0] = scale[0] * 2.0F;
       }
     }
   }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
   
   @SpirePatch(clz = ProceedButton.class, method = "update")
   public static class ChangeBossRoom
   {
     @SpireInsertPatch(rloc = 22)
     public static SpireReturn Insert(ProceedButton self) {
       if (SlayTheStreamer.config.getBool("VoteOnBosses")) {
         SlayTheStreamer.log("~~~~~~~ Activating Code " + AbstractDungeon.currMapNode.getRoomSymbol(Boolean.valueOf(true)));
         if (AbstractDungeon.currMapNode.getRoomSymbol(Boolean.valueOf(true)) == "T" && AbstractDungeon.getCurrRoom() instanceof com.megacrit.cardcrawl.rooms.TreasureRoom) {
           
           SlayTheStreamer.log("~~~~~~~ We're a treasure room");
           CardCrawlGame.music.fadeOutTempBGM();
           AbstractDungeon.currMapNode.setRoom(new BossSelectRoom());
           AbstractDungeon.nextRoom = AbstractDungeon.currMapNode;
           AbstractDungeon.closeCurrentScreen();
           AbstractDungeon.nextRoomTransitionStart();
           self.hide();
           return SpireReturn.Return(null);
         } 
       } 
       return SpireReturn.Continue();
     }
   }
 
 
 
   
   @SpirePatch(clz = AbstractDungeon.class, method = "render")
   public static class RenderBossSelect
   {
     @SpireInsertPatch(rloc = 133)
     public static void Insert(AbstractDungeon self, SpriteBatch sb) {
       if (AbstractDungeon.screen == BossChoicePatch.BOSS_SELECT)
         SlayTheStreamer.bossSelectScreen.render(sb); 
     }
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\BossChoicePatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */

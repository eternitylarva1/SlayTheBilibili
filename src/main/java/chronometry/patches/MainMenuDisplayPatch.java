 package chronometry.patches;
 
 import basemod.ReflectionHacks;
 import chronometry.SlayTheStreamer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
 import com.megacrit.cardcrawl.core.CardCrawlGame;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
 import de.robojumper.ststwitch.TwitchConfig;
 import de.robojumper.ststwitch.TwitchPanel;
 
 public class MainMenuDisplayPatch
 {
   @SpirePatch(cls = "com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen", method = "render")
   public static class render
   {
     @SpireInsertPatch(rloc = 3)
     public static void Insert(MainMenuScreen __instance, SpriteBatch sb) {
       Color white = new Color(1.0F, 1.0F, 1.0F, 1.0F);
       
       if (AbstractDungeon.topPanel.twitch.isPresent()) {
         TwitchConfig t = ((TwitchPanel)AbstractDungeon.topPanel.twitch.get()).connection.getTwitchConfig();
         String username = (String)ReflectionHacks.getPrivate(t, TwitchConfig.class, "username");
         FontHelper.renderFontCentered(sb, FontHelper.bannerNameFont, (CardCrawlGame.languagePack.getUIString("versus:ForPlayer")).TEXT[6] + username, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F - 196.0F * Settings.scale, white);
 
 
         
         sb.draw(SlayTheStreamer.startScreenImage, Settings.WIDTH / 2.0F, 0.0F, SlayTheStreamer.startScreenImage
             
             .getWidth() * Settings.scale, SlayTheStreamer.startScreenImage
             .getHeight() * Settings.scale);
       } 
     }
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\MainMenuDisplayPatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */
 package chronometry.patches;
 
 import chronometry.SlayTheStreamer;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.shop.Merchant;
 
 public class ShopkeeperNamesPatch
 {
   static String MerchantName = "";
 
 
 
 
 
   
   @SpirePatch(clz = Merchant.class, method = "<ctor>", paramtypez = { float.class, float.class, int.class})
   public static class setupShopkeeperName
   {
     static String client_id = "ldy9d28m1ry8zvg1ucxow8akhuuiw0";
     static String URL = "https://api.twitch.tv/helix/streams?game_id=496902";
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
     
     public static void Postfix(Merchant m, float x, float y, int newShopScreen) {
       String[] names = SlayTheStreamer.config.getString("MerchantNames").split(",");
       ShopkeeperNamesPatch.MerchantName = names[(int)(Math.random() * names.length)];
     }
   }
   
   @SpirePatch(clz = Merchant.class, method = "render")
   public static class renderMerchantName {
     public static void Postfix(Merchant m, SpriteBatch sb) {
       sb.setColor(Settings.CREAM_COLOR);
       FontHelper.renderFontCentered(sb, FontHelper.tipHeaderFont, ShopkeeperNamesPatch.MerchantName, m.hb.cX + 8.0F, m.hb.y + m.hb.height, Settings.CREAM_COLOR);
     }
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\ShopkeeperNamesPatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */

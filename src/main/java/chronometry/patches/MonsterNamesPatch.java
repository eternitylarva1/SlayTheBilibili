 package chronometry.patches;
 import basemod.ReflectionHacks;
 import chronometry.MockTwitchHelper;
 import chronometry.SlayTheStreamer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
 import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
 import com.gikk.twirk.Twirk;
 import com.gikk.twirk.types.users.TwitchUser;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.monsters.AbstractMonster;
 import de.robojumper.ststwitch.TwitchPanel;
 import de.robojumper.ststwitch.TwitchVoter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Optional;
 import java.util.Random;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class MonsterNamesPatch {
   public static final Pattern QUOTE_PATTERN = Pattern.compile("(?i)\"(.*)\"(?:.*?)-?- ?joinrbs(?:[ \\.,].*)?");
 
 
 
 
 
 
 
 
 
 
 
 
 
 
   
   @SpirePatch(clz = AbstractMonster.class, method = "<ctor>", paramtypez = {String.class, String.class, int.class, float.class, float.class, float.class, float.class, String.class, float.class, float.class, boolean.class})
   public static class changeMonsterNames
   {
     public static void Postfix(AbstractMonster self, String name, String id, int maxHealth, float hb_x, float hb_y, float hb_w, float hb_h, String imgUrl, float offsetX, float offsetY, boolean ignoreBlights) {
       Set<String> votedUsernames = null;

       // 尝试获取真实的 TwitchVoter
       Optional<TwitchVoter> voterOpt = TwitchPanel.getDefaultVoter();
       if (voterOpt.isPresent()) {
         TwitchVoter twitchVoter = voterOpt.get();
         votedUsernames = (Set)ReflectionHacks.getPrivate(twitchVoter, TwitchVoter.class, "votedUsernames");
       }
       // 如果没有真实的 TwitchVoter，使用模拟模式
       else if (MockTwitchHelper.isMockMode()) {
         votedUsernames = MockTwitchHelper.getMockVotedUsernames();
       }

       // 如果没有可用的投票用户名，保持怪物原始名称
       if (votedUsernames == null || votedUsernames.size() == 0) {
         return;
       }
       
       if (votedUsernames.size() > 0) {
 
         
         List<String> screwYouList = new ArrayList<String>(votedUsernames);
         Map<String, Double> weightedMap = new HashMap<String, Double>();
         double totalWeight = 0.0D;
         
         for (String e : screwYouList) {
           int chosenTimes; String tarName = e;
           if (SlayTheStreamer.displayNames.containsKey(tarName)) {
             tarName = (String)SlayTheStreamer.displayNames.get(tarName);
           }
 
           
           if (SlayTheStreamer.usedNames.containsKey(tarName)) {
             chosenTimes = ((Integer)SlayTheStreamer.usedNames.get(tarName)).intValue();
           } else {
             chosenTimes = 0;
           } 
 
 
           
           if (SlayTheStreamer.votedTimes.containsKey(tarName)) {
             SlayTheStreamer.votedTimes.put(tarName, Integer.valueOf(((Integer)SlayTheStreamer.votedTimes.get(tarName)).intValue() + 1));
             weightedMap.put(e, Double.valueOf(Math.pow((((Integer)SlayTheStreamer.votedTimes.get(tarName)).intValue() + 15), 1.05D) / Math.pow((chosenTimes + 5), 2.5D)));
             totalWeight += ((Double)weightedMap.get(e)).doubleValue();
           } else {
             
             SlayTheStreamer.votedTimes.put(tarName, Integer.valueOf(1));
             weightedMap.put(e, Double.valueOf(Math.pow((((Integer)SlayTheStreamer.votedTimes.get(tarName)).intValue() + 15), 1.05D) / Math.pow((chosenTimes + 5), 2.5D)));
             totalWeight += ((Double)weightedMap.get(e)).doubleValue();
           } 
           SlayTheStreamer.log("Name " + tarName + ", Voted " + SlayTheStreamer.votedTimes.get(tarName) + " time(s), Chosed " + chosenTimes + " time(s), weight is " + weightedMap
               .get(e));
         } 
         
         String username = null;
         double randomVal = MathUtils.random.nextDouble() * totalWeight;
         
         for (String e : weightedMap.keySet()) {
           randomVal -= ((Double)weightedMap.get(e)).doubleValue();
           if (randomVal <= 0.0D) {
             username = e;
             
             break;
           } 
         } 
         String usernameOrigin = username;
 
 
         
         if (SlayTheStreamer.displayNames.containsKey(username)) {
           username = (String)SlayTheStreamer.displayNames.get(username);
         }
 
         
         if (SlayTheStreamer.usedNames.containsKey(username)) {
           SlayTheStreamer.usedNames.put(username, Integer.valueOf(((Integer)SlayTheStreamer.usedNames.get(username)).intValue() + 1));
           
           if (((Integer)SlayTheStreamer.usedNames.get(username)).intValue() == 2) {
             username = username + " Jr.";
           } else {
             username = username + " " + MonsterNamesPatch.IntegerToRomanNumeral(((Integer)SlayTheStreamer.usedNames.get(username)).intValue());
           } 
         } else {
           
           SlayTheStreamer.usedNames.put(username, Integer.valueOf(1));
         } 
 
         
         self.name = username;
         votedUsernames.remove(usernameOrigin);

         // 如果是模拟模式，从模拟列表中移除
         if (MockTwitchHelper.isMockMode()) {
           MockTwitchHelper.removeUsername(usernameOrigin);
         }
       }
     }
   }
 
   
   @SpirePatch(clz = Twirk.class, method = "incommingMessage")
   public static class storeTwitchNames
   {
     @SpireInsertPatch(rloc = 57, localvars = {"user"})
     public static void Insert(Twirk self, String line, TwitchUser user) { SlayTheStreamer.displayNames.put(user.getUserName(), user.getDisplayName()); }
   }
   
   @SpirePatch(clz = AbstractMonster.class, method = "renderName")
   public static class renderMonsterNames
   {
     public static void Replace(AbstractMonster m, SpriteBatch sb) {
       if (AbstractDungeon.screen == BossChoicePatch.BOSS_SELECT)
         return; 
       float y = m.intentHb.cY - 56.0F;
       float x = m.hb.cX - m.animX;
       Color c = Settings.CREAM_COLOR;
       
       if (m.isDying) {
         c = m.tint.color;
         
         return;
       } 
       sb.setColor(Settings.CREAM_COLOR);
       FontHelper.renderFontCentered(sb, FontHelper.tipHeaderFont, m.name, x, y, Settings.CREAM_COLOR);
       
       Random nameIndicer = new Random(m.name.hashCode());
       String[] titles = SlayTheStreamer.config.getString("MonsterTitles").split(",");
       String title = titles[nameIndicer.nextInt(titles.length)];
       
       FontHelper.renderFontCentered(sb, FontHelper.powerAmountFont, "the " + title, x, y - 20.0F * Settings.scale, Settings.CREAM_COLOR);
     }
   }
   
   @SpirePatch(clz = AbstractMonster.class, method = "refreshIntentHbLocation")
   public static class changeIntentHBPosition {
     public static void Postfix(AbstractMonster m) {
       m.intentHb.y += 42.0F;
       m.intentHb.cY += 42.0F;
     }
   }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
   
   public static String IntegerToRomanNumeral(int input) {
     if (input < 1 || input > 3999)
       return "Invalid Roman Number Value"; 
     String s = "";
     while (input >= 1000) {
       s = s + "M";
       input -= 1000;
     }  while (input >= 900) {
       s = s + "CM";
       input -= 900;
     } 
     while (input >= 500) {
       s = s + "D";
       input -= 500;
     } 
     while (input >= 400) {
       s = s + "CD";
       input -= 400;
     } 
     while (input >= 100) {
       s = s + "C";
       input -= 100;
     } 
     while (input >= 90) {
       s = s + "XC";
       input -= 90;
     } 
     while (input >= 50) {
       s = s + "L";
       input -= 50;
     } 
     while (input >= 40) {
       s = s + "XL";
       input -= 40;
     } 
     while (input >= 10) {
       s = s + "X";
       input -= 10;
     } 
     while (input >= 9) {
       s = s + "IX";
       input -= 9;
     } 
     while (input >= 5) {
       s = s + "V";
       input -= 5;
     } 
     while (input >= 4) {
       s = s + "IV";
       input -= 4;
     } 
     while (input >= 1) {
       s = s + "I";
       input--;
     } 
     return s;
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\MonsterNamesPatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */

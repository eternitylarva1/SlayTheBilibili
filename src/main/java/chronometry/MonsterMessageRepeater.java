 package chronometry;
 
 import com.megacrit.cardcrawl.core.CardCrawlGame;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.monsters.AbstractMonster;
 import com.megacrit.cardcrawl.vfx.SpeechBubble;
 
 public class MonsterMessageRepeater
 {
   static int MsgLength = 64;
   
   static void parseMessage(String msg, String user) {
     if (CardCrawlGame.isInARun() && 
       AbstractDungeon.getCurrRoom() != null && 
       (AbstractDungeon.getCurrRoom()).monsters != null)
       for (AbstractMonster m : (AbstractDungeon.getCurrRoom()).monsters.monsters) {
         if (m.isDying)
           return;  String username = user;
         if (SlayTheStreamer.displayNames.containsKey(username)) {
           username = (String)SlayTheStreamer.displayNames.get(username);
         }
         if (m.name.split(" ")[0].toLowerCase().equals(username.split(" ")[0].toLowerCase())) {
           msg = msg.substring(0, Math.min(msg.length(), MsgLength));
           if (msg.length() == MsgLength) {
             int lastSpacePos = msg.lastIndexOf(" ");
             if (lastSpacePos != -1) {
               msg = msg.substring(0, lastSpacePos);
             }
             msg = msg + "...";
           } 
           AbstractDungeon.effectList.add(new SpeechBubble(m.hb.cX + m.dialogX, m.hb.cY + m.dialogY, 5.0F, msg, false));
         } 
       }  
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\MonsterMessageRepeater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */
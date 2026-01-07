 package chronometry;
 
 import com.megacrit.cardcrawl.cards.AbstractCard;
 import com.megacrit.cardcrawl.core.CardCrawlGame;
 import com.megacrit.cardcrawl.rooms.AbstractRoom;
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 public class BossSelectRoom
   extends AbstractRoom
 {
   public boolean choseRelic = false;
   
   public void onPlayerEntry() {
     CardCrawlGame.music.silenceBGM();
     this.phase = AbstractRoom.RoomPhase.COMPLETE;
     SlayTheStreamer.bossSelectScreen.open();
     playBGM("SHRINE");
   }
 
   
   public void update() {
     super.update();
     SlayTheStreamer.bossSelectScreen.update();
   }
 
 
   
   public AbstractCard.CardRarity getCardRarity(int roll) { return null; }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\BossSelectRoom.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */
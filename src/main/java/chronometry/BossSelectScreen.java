 package chronometry;

 import basemod.BaseMod;
 import basemod.ReflectionHacks;
 import chronometry.MockTwitchHelper;
 import chronometry.patches.BossChoicePatch;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.esotericsoftware.spine.Skeleton;
 import com.megacrit.cardcrawl.core.CardCrawlGame;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.dungeons.TheCity;
 import com.megacrit.cardcrawl.helpers.FontHelper;
 import com.megacrit.cardcrawl.helpers.ImageMaster;
 import com.megacrit.cardcrawl.helpers.MonsterHelper;
 import com.megacrit.cardcrawl.localization.UIStrings;
 import com.megacrit.cardcrawl.monsters.AbstractMonster;
 import com.megacrit.cardcrawl.monsters.MonsterGroup;
 import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
 import com.megacrit.cardcrawl.monsters.beyond.Donu;
 import com.megacrit.cardcrawl.monsters.beyond.TimeEater;
 import com.megacrit.cardcrawl.monsters.city.BronzeAutomaton;
 import com.megacrit.cardcrawl.monsters.city.Champ;
 import com.megacrit.cardcrawl.monsters.city.TheCollector;
 import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;
 import com.megacrit.cardcrawl.monsters.exordium.TheGuardian;
 import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
 import com.megacrit.cardcrawl.vfx.BossChestShineEffect;
 import com.megacrit.cardcrawl.vfx.GlowyFireEyesEffect;
 import com.megacrit.cardcrawl.vfx.StaffFireEffect;
 import de.robojumper.ststwitch.TwitchPanel;
 import de.robojumper.ststwitch.TwitchVoteListener;
 import de.robojumper.ststwitch.TwitchVoteOption;
 import de.robojumper.ststwitch.TwitchVoter;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Optional;
 
 
 
 
 public class BossSelectScreen
 {
   public boolean isDone;
   public Texture smokeImg;
   public float shineTimer;
   public static final float SHINE_INTERAL = 0.1F;
   public static ArrayList<Float> monsterX = new ArrayList();
   public static ArrayList<Float> monsterY = new ArrayList();
   
   public float fireTimer;
   
   boolean isVoting;
   
   boolean mayVote;
   protected ArrayList<AbstractMonster> bosses;
   boolean reopened;
   public static UIStrings bossRoomStrings = CardCrawlGame.languagePack.getUIString("versus:ForPlayer");
   public static String[] TEXT = bossRoomStrings.TEXT; public BossSelectScreen() { this.shineTimer = 0.0F;
     this.fireTimer = 0.0F;
     this.bosses = new ArrayList();
     this.reopened = false;
     this.isDone = false;
     this.isVoting = false;
     this.mayVote = false;
     
     monsterX.add(Float.valueOf(964.0F * Settings.scale));
     monsterY.add(Float.valueOf(540.0F * Settings.scale));
     monsterX.add(Float.valueOf(804.0F * Settings.scale));
     monsterY.add(Float.valueOf(360.0F * Settings.scale));
     monsterX.add(Float.valueOf(1124.0F * Settings.scale));
     monsterY.add(Float.valueOf(360.0F * Settings.scale));
     
     this.smokeImg = ImageMaster.loadImage("versusImages/BossScreenOverlay.png");
     
     TwitchVoter.registerListener(new TwitchVoteListener()
         {
           public void onTwitchAvailable() {
             SlayTheStreamer.bossSelectScreen.updateVote();
           }
 
 
           
           public void onTwitchUnavailable() { SlayTheStreamer.bossSelectScreen.updateVote(); }
         }); }
 
 
 
   
   public void update() {
     this.shineTimer -= Gdx.graphics.getDeltaTime();
     if (this.shineTimer < 0.0F && !Settings.DISABLE_EFFECTS) {
       this.shineTimer = 0.1F;
       AbstractDungeon.topLevelEffects.add(new BossChestShineEffect());
       AbstractDungeon.topLevelEffects.add(new BossChestShineEffect(MathUtils.random(0.0F, Settings.WIDTH), MathUtils.random(0.0F, Settings.HEIGHT - 128.0F * Settings.scale)));
     } 
     
     if (this.reopened)
       return; 
     for (AbstractMonster m : this.bosses) {
       if (m.name == "Hexaghost") {
         m.update();
       }
       if (m.id == "TheCollector") {
         this.fireTimer -= Gdx.graphics.getDeltaTime();
         Skeleton skeleton = (Skeleton)ReflectionHacks.getPrivate(m, com.megacrit.cardcrawl.core.AbstractCreature.class, "skeleton");
         
         if (this.fireTimer < 0.0F) {
           
           this.fireTimer = 0.07F;
 
 
           
           GlowyFireEyesEffect left = new GlowyFireEyesEffect(skeleton.getX() + skeleton.findBone("lefteyefireslot").getX(), skeleton.getY() + skeleton.findBone("lefteyefireslot").getY() + 70.0F * Settings.scale);
 
 
 
           
           GlowyFireEyesEffect right = new GlowyFireEyesEffect(skeleton.getX() + skeleton.findBone("righteyefireslot").getX(), skeleton.getY() + skeleton.findBone("righteyefireslot").getY() + 70.0F * Settings.scale);
           
           float leftScale = ((Float)ReflectionHacks.getPrivate(left, AbstractGameEffect.class, "scale")).floatValue() / 2.0F;
           float rightScale = ((Float)ReflectionHacks.getPrivate(right, AbstractGameEffect.class, "scale")).floatValue() / 2.0F;
           
           ReflectionHacks.setPrivate(left, AbstractGameEffect.class, "scale", Float.valueOf(leftScale));
           ReflectionHacks.setPrivate(right, AbstractGameEffect.class, "scale", Float.valueOf(rightScale));
           
           AbstractDungeon.topLevelEffects.add(left);
           AbstractDungeon.topLevelEffects.add(right);
           
           AbstractDungeon.topLevelEffects.add(new StaffFireEffect(skeleton
                 
                 .getX() + skeleton.findBone("fireslot").getX() - 60.0F * Settings.scale, skeleton
                 .getY() + skeleton.findBone("fireslot").getY() + 195.0F * Settings.scale));
         } 
       } 
     } 
   }
 
   
   public void open() {
     refresh();
     this.bosses.clear();
     Settings.hideCombatElements = true;
 
     
     AbstractDungeon.dynamicBanner.appear(800.0F * Settings.scale, TEXT[1]);
     AbstractDungeon.isScreenUp = true;
     AbstractDungeon.screen = BossChoicePatch.BOSS_SELECT;
     AbstractDungeon.overlayMenu.proceedButton.hide();
     AbstractDungeon.overlayMenu.showBlackScreen();
     
     int bossCount = AbstractDungeon.bossList.size();
     SlayTheStreamer.log("BossSelectScreen: Creating " + bossCount + " bosses");
     
     for (int i = 0; i < bossCount; i++) {
       String bossID = (String)AbstractDungeon.bossList.get(i);
       AbstractMonster m = getBoss(bossID);
       if (m == null) {
         SlayTheStreamer.log("BossSelectScreen: Failed to create boss for ID: " + bossID);
         continue;
       }
       // Use available positions, but don't exceed the array bounds
       if (i < monsterX.size() && i < monsterY.size()) {
         m.drawX = ((Float)monsterX.get(i)).floatValue();
         m.drawY = ((Float)monsterY.get(i)).floatValue() - 42.0F * Settings.scale;
       } else {
         SlayTheStreamer.log("BossSelectScreen: Warning: Not enough positions for boss " + i);
       }
       this.bosses.add(m);
     }
 
     
     this.mayVote = true;
     updateVote();
   }
 
   
   public void reopen() { this.reopened = true; }
   
   public AbstractMonster getBoss(String bossID) {
     TheGuardian theGuardian;
     SlimeBoss slimeBoss;
     switch (bossID) {
       
       case "Hexaghost":
         return new HexaghostModel();
       case "Slime Boss":
         return new SlimeBoss();
 
       
       case "The Guardian":
         theGuardian = new TheGuardian();
         theGuardian.animY -= 50.0F;
         return theGuardian;
       
       case "Champ":
         return new Champ();
       case "Collector":
         return new TheCollector();
       case "Automaton":
         return new BronzeAutomaton();
       
       case "Awakened One":
         return new AwakenedOne(0.0F, 0.0F);
       case "Time Eater":
         return new TimeEater();
       case "Donu and Deca":
         return new Donu();
     } 
 
     
     BaseMod.BossInfo bossInfo = BaseMod.getBossInfo(bossID);
     if (bossInfo != null) {
       MonsterGroup bossGroup = MonsterHelper.getEncounter(bossID);
       if (bossGroup.monsters.size() == 1)
       {
         AbstractMonster m = (AbstractMonster)bossGroup.monsters.get(0);
         SlayTheStreamer.log("BossSelectScreen: Created boss " + bossID + " from MonsterHelper, hb is null: " + (m.hb == null));
         return m;
       }
       
       for (AbstractMonster mo : bossGroup.monsters) {
         
         if (mo.type == AbstractMonster.EnemyType.BOSS)
         {
           SlayTheStreamer.log("BossSelectScreen: Created boss " + bossID + " from MonsterHelper (BOSS type), hb is null: " + (mo.hb == null));
           return mo;
         }
       }
       
       AbstractMonster m = (AbstractMonster)bossGroup.monsters.get(0);
       SlayTheStreamer.log("BossSelectScreen: Created boss " + bossID + " from MonsterHelper (first monster), hb is null: " + (m.hb == null));
       return m;
     }
     
     return null;
   }
   
   public void refresh() {
     this.isDone = false;
     this.shineTimer = 0.0F;
   }
 
   
   public void hide() { AbstractDungeon.dynamicBanner.hide(); }
 
 
 
   
   public void render(SpriteBatch sb) {
     update();
     
     for (AbstractGameEffect e : AbstractDungeon.effectList) {
       e.render(sb);
     }
     
     sb.setColor(Color.WHITE);
     sb.draw(this.smokeImg, 470.0F * Settings.scale, AbstractDungeon.floorY - 258.0F * Settings.scale, this.smokeImg.getWidth() * Settings.scale, this.smokeImg.getHeight() * Settings.scale);
     
     for (AbstractMonster m : this.bosses) {
       if (m != null) {
         if (m.hb == null) {
           SlayTheStreamer.log("BossSelectScreen: Skipping monster " + m.name + " (ID: " + m.id + ") because hb is null");
           continue;
         }
         try {
           m.render(sb);
         } catch (Exception e) {
           // Only log once per monster to reduce console spam
           if (!m.name.startsWith("Error logged")) {
             SlayTheStreamer.log("BossSelectScreen: Error rendering monster " + m.name + " (ID: " + m.id + "): " + e.getMessage());
             m.name = "Error logged: " + m.name;
           }
         }
       }
     }
     
     if (AbstractDungeon.topPanel.twitch.isPresent() || MockTwitchHelper.isMockMode()) {
       renderTwitchVotes(sb);
     }
   }
 
 
 
 
 
   
   public void renderTwitchVotes(SpriteBatch sb) {
      if (!this.isVoting) {
        SlayTheStreamer.log("BossSelectScreen: Twitch not active");
       return;
     }
      if (MockTwitchHelper.isMockMode()) {
        SlayTheStreamer.log("BossSelectScreen: Rendering mock Twitch votes");
        String[] options = MockTwitchHelper.getCurrentOptions();
        int[] voteCounts = MockTwitchHelper.getMockVoteCounts();
        if (options == null || voteCounts == null) {
          SlayTheStreamer.log("BossSelectScreen: Mock options or voteCounts is null");
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
          switch (i) {
           case 0:
              FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, ((Float)monsterX.get(0)).floatValue(), ((Float)monsterY.get(0)).floatValue() - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;

           case 1:
              FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, ((Float)monsterX.get(1)).floatValue(), ((Float)monsterY.get(1)).floatValue() - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;

           case 2:
              FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, ((Float)monsterX.get(2)).floatValue(), ((Float)monsterY.get(2)).floatValue() - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;
         }
        }
        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, TEXT[2] + MockTwitchHelper.getSecondsRemaining() + TEXT[3], Settings.WIDTH / 2.0F, 192.0F * Settings.scale, Color.WHITE.cpy());
     } else if (getVoter().isPresent()) {
        SlayTheStreamer.log("BossSelectScreen: Rendering real Twitch votes");
        TwitchVoter twitchVoter = (TwitchVoter)getVoter().get();
        TwitchVoteOption[] options = twitchVoter.getOptions();
        int sum = ((Integer)Arrays.stream(options).map(c -> Integer.valueOf(c.voteCount)).reduce(Integer.valueOf(0), Integer::sum)).intValue();
        for (int i = 0; i < 3; i++) {
          String s = "#" + i + ": " + (options[i]).voteCount;
          if (sum > 0) {
            s = s + " (" + ((options[i]).voteCount * 100 / sum) + "%)";
          }
          switch (i) {
           case 0:
              FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, ((Float)monsterX.get(0)).floatValue(), ((Float)monsterY.get(0)).floatValue() - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;

           case 1:
              FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, ((Float)monsterX.get(1)).floatValue(), ((Float)monsterY.get(1)).floatValue() - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;

           case 2:
              FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, s, ((Float)monsterX.get(2)).floatValue(), ((Float)monsterY.get(2)).floatValue() - 75.0F * Settings.scale, Color.WHITE.cpy());
             break;
         }
        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, TEXT[2] + twitchVoter.getSecondsRemaining() + TEXT[3], Settings.WIDTH / 2.0F, 192.0F * Settings.scale, Color.WHITE.cpy());
     }
      } else {
        SlayTheStreamer.log("BossSelectScreen: No voter available and mock mode is disabled");
      }
     sb.draw(SlayTheStreamer.startScreenImage, Settings.WIDTH / 2.0F, 0.0F, SlayTheStreamer.startScreenImage
         
         .getWidth() * Settings.scale, SlayTheStreamer.startScreenImage
         .getHeight() * Settings.scale);
   }
 
 
 
   
   public Optional<TwitchVoter> getVoter() { return TwitchPanel.getDefaultVoter(); }
 
   
   public void updateVote() {
      SlayTheStreamer.log("BossSelectScreen.updateVote called - mayVote: " + this.mayVote + ", isVoting: " + this.isVoting);
      if (MockTwitchHelper.isMockMode()) {
        SlayTheStreamer.log("BossSelectScreen: Mock mode enabled");
        if (this.mayVote && !this.isVoting) {
          String[] array = new String[AbstractDungeon.bossList.size()];
          array = (String[])AbstractDungeon.bossList.toArray(array);
          SlayTheStreamer.log("BossSelectScreen: Boss list: " + Arrays.toString(array));
          this.isVoting = MockTwitchHelper.initiateSimpleNumberVote(array, this::completeVoting);
          SlayTheStreamer.log("BossSelectScreen: Mock voting started with " + array.length + " options");
        } else if (this.isVoting && !this.mayVote) {
          this.isVoting = false;
          SlayTheStreamer.log("BossSelectScreen: Mock voting ended");
        }
      } else if (getVoter().isPresent()) {
        SlayTheStreamer.log("BossSelectScreen: Real Twitch mode");
        TwitchVoter twitchVoter = (TwitchVoter)getVoter().get();
        if (this.mayVote && twitchVoter.isVotingConnected() && !this.isVoting) {
          String[] array = new String[AbstractDungeon.bossList.size()];
          array = (String[])AbstractDungeon.bossList.toArray(array);

          this.isVoting = twitchVoter.initiateSimpleNumberVote(array, this::completeVoting);
       }
        else if (this.isVoting && (!this.mayVote || !twitchVoter.isVotingConnected())) {
          twitchVoter.endVoting(true);
          this.isVoting = false;
       }
      } else {
        SlayTheStreamer.log("BossSelectScreen: No voter available and mock mode is disabled");
      }
   }
   
   public void completeVoting(int option) {
     if (!this.isVoting) {
       return;
     }
     this.isVoting = false;
     if (getVoter().isPresent()) {
       TwitchVoter twitchVoter = (TwitchVoter)getVoter().get();
       AbstractDungeon.topPanel.twitch.ifPresent(twitchPanel -> twitchPanel.connection.sendMessage(TEXT[4] + (twitchVoter.getOptions()[option]).displayName));
     }

     
     AbstractDungeon.bossKey = (String)AbstractDungeon.bossList.get(option);
     SlayTheStreamer.bossHidden = false;
     this.bosses.clear();
     Settings.hideCombatElements = false;

     // Call setBoss BEFORE opening the map to ensure boss icons are loaded correctly
     try {
       Method m = AbstractDungeon.class.getDeclaredMethod("setBoss", new Class[] { String.class });
       m.setAccessible(true);
       m.invoke(CardCrawlGame.dungeon, new Object[] { AbstractDungeon.bossKey });
       SlayTheStreamer.log("BossSelectScreen: setBoss called with key: " + AbstractDungeon.bossKey);
     }
     catch (Throwable throwable) {
       SlayTheStreamer.log("BossSelectScreen: Error calling setBoss: " + throwable.getMessage());
     }

     // Close screen and open map AFTER setBoss is called
     AbstractDungeon.closeCurrentScreen();
     AbstractDungeon.dungeonMapScreen.open(false);
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\BossSelectScreen.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */

 package chronometry;
 
 import basemod.BaseMod;
 import basemod.ReflectionHacks;
 import basemod.interfaces.EditStringsSubscriber;
 import basemod.interfaces.PostDungeonInitializeSubscriber;
 import basemod.interfaces.PostInitializeSubscriber;
 import basemod.interfaces.StartGameSubscriber;
 import chronometry.MockTwitchHelper;
 import chronometry.patches.CardRewardPatch;
 import chronometry.patches.NoSkipBossRelicPatch;
 import chronometry.patches.StartGamePatch;
 import com.badlogic.gdx.graphics.Texture;
 import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
 import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
 import com.megacrit.cardcrawl.core.Settings;
 import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
 import com.megacrit.cardcrawl.helpers.ImageMaster;
 import de.robojumper.ststwitch.TwitchMessageListener;
 import de.robojumper.ststwitch.TwitchPanel;
 import de.robojumper.ststwitch.TwitchVoteListener;
 import de.robojumper.ststwitch.TwitchVoter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;


@SpireInitializer
 public class SlayTheStreamer
   implements PostInitializeSubscriber, StartGameSubscriber, PostDungeonInitializeSubscriber, EditStringsSubscriber
 {
   public static final Logger logger = LogManager.getLogger(SlayTheStreamer.class.getName());
   
   private static final String MOD_NAME = "Slay the Streamer";
   
   private static final String AUTHOR = "Chronometrics";
   
   private static final String DESCRIPTION = "Chat vs Streamer in the ultimate showdown! The streamer begins with a winnable deck, and chat tries to find ways to ruin it by voting and influencing the run throughout the stream. Requires Twitch Integration to work.";
   
   public static SpireConfig config;
   public static boolean bossHidden = false;
   public static BossSelectScreen bossSelectScreen;
   public static NoSkipBossRelicPatch noSkip;
   public static CardRewardPatch cardRewardPatch;
   public static Texture startScreenImage;
   public static Map<String, Integer> usedNames = new HashMap();
   public static Map<String, String> displayNames = new HashMap();
   public static Map<String, Integer> votedTimes = new HashMap();


   public SlayTheStreamer() { BaseMod.subscribe(this); }


   public static void log(String s) { logger.info(s); }



   public static void initialize() {
   new SlayTheStreamer();
}
   
   public void receivePostInitialize() {
     bossSelectScreen = new BossSelectScreen();
     noSkip = new NoSkipBossRelicPatch();
     cardRewardPatch = new CardRewardPatch();
     startScreenImage = ImageMaster.loadImage("versusImages/FacesOfEvil.png");
     
     try {
       config = new SpireConfig("SlayTheStreamer", "config");
       setDefaultPrefs();
     } catch (Exception e) {
       logger.info("Could not save config");
       logger.error(e.toString());
     } 
     
     if (config.getBool("VoteOnBosses")) {
       bossHidden = true;
     }

     if (config.getBool("MockTwitchMode")) {
       MockTwitchHelper.enableMockMode();
       log("Mock Twitch mode enabled via config");
     }

     Settings.isTestingNeow = true;
     
     Texture badgeTexture = ImageMaster.loadImage("versusImages/Badge.png");
     BaseMod.registerModBadge(badgeTexture, "Slay the Streamer", "Chronometrics", "Chat vs Streamer in the ultimate showdown! The streamer begins with a winnable deck, and chat tries to find ways to ruin it by voting and influencing the run throughout the stream. Requires Twitch Integration to work.", new ConfigPanel());

     
     TwitchVoter.registerListener(new TwitchVoteListener()
         {
           public void onTwitchAvailable() {
             StartGamePatch.updateVote();
           }

           
           public void onTwitchUnavailable() {
             StartGamePatch.updateVote();
           }
         });
     
     if (AbstractDungeon.topPanel != null && AbstractDungeon.topPanel.twitch != null && AbstractDungeon.topPanel.twitch.isPresent()) {
       List<TwitchMessageListener> listeners = (List)ReflectionHacks.getPrivate(((TwitchPanel)AbstractDungeon.topPanel.twitch.get()).connection, de.robojumper.ststwitch.TwitchConnection.class, "listeners");
       
       TwitchMessageListener t = new TwitchMessageListener()
         {
           public void onMessage(String msg, String user) {
             MonsterMessageRepeater.parseMessage(msg, user);
           }
         };
       
       listeners.add(t);
     }
   }
   
   public void receivePostDungeonInitialize() {
     if (config.getBool("VoteOnBosses")) {
       bossHidden = true;
     } 
     
     AbstractDungeon.bossRelicPool.removeIf(s -> s.equals("Pandora's Box"));
   }
   
   public void setDefaultPrefs() {
     if (!config.has("CardPickPool")) config.setInt("CardPickPool", 30); 
     if (!config.has("CardPickChoices")) config.setInt("CardPickChoices", 10);
     
     if (!config.has("GuaranteedRares")) config.setInt("GuaranteedRares", 2); 
     if (!config.has("GuaranteedUncommons")) config.setInt("GuaranteedUncommons", 5); 
     if (!config.has("GuaranteedCommons")) config.setInt("GuaranteedCommons", 10);
     
     if (!config.has("VoteOnBosses")) config.setBool("VoteOnBosses", true);
     if (!config.has("VoteOnNeow")) config.setBool("VoteOnNeow", true);
     if (!config.has("VoteOnCards")) config.setBool("VoteOnCards", true);
     if (!config.has("MockTwitchMode")) config.setBool("MockTwitchMode", false);
     
     if (!config.has("MerchantNames")) config.setString("MerchantNames", "Casey,Anthony"); 
     if (!config.has("MonsterTitles")) config.setString("MonsterTitles", "Painbringer,Snecko's Eye,Lord of Reptiles,Whistleblower,Paragon of Chat,Baron of Slimes,Count of Encouragement,Duke of Wonder,Enslaver of Slaves,Mangler of Malaphors,Antiquated,Unsummoner,Titan,Thorny,Baffling,True Hero,Patron of Demons,Provoker of Perseveration,Bastion of Bureaucracy,Shaper of Towers,Spinner of Cloth,Duchess of the Exordium,Instiller of Dishonesty,Installer of Distilleries,Judge and Jury,Executable,Agreeable,Analytic,Attractive,Backward-Compatible,Bleeding-Edge,Boiling Mad,Brave,Bullheaded,Chic,Cold,Corrugated,Corrupt,Cost-Effective,Daydreamer,Dazzling,Delightful,Destructive,Devoted,Disheveled,Distinctive,Dreamless,Dynamically-Loading,Elastic,Ethical,Exceptional,Expansive,Fashionable,Feature-Driven,Focused,Frictionless,Frustrated,Future-Proof,Handsome,Hasty,Holier-than-thou,Holistic,Illustrious,Incorrigible,Industrious,Inept,Intermandated,Intuitive,Jealous,Levelheaded,Magnanimous,Magnetic,Misrepresented,Multidisciplinary,Muscular,Musical,Obsessive,Open-Sourced,Outlandish,Overzealous,Precognitive,Prehistoric,Princely,Professional,Quarrelsome,Rapturous,Regal,Responsible,Ridiculous,Robust,Rugged,Sleep-Deprived,Smiling,Stubborn,Synergistic,Timid,Underappreciated");
     
     try {
       config.save();
     } catch (Exception e) {
       e.printStackTrace();
     } 
   }


   
   public void receiveStartGame() {
     Settings.isTestingNeow = true;
     Settings.isFinalActAvailable = true;
     if (config.getBool("VoteOnBosses")) {
       bossHidden = true;
     } 
   }


   
   public void receiveEditStrings() {
     loadLocStrings("eng");
     
     try {
       loadLocStrings(Settings.language.toString().toLowerCase());
     }
     catch (Exception e) {
       
       System.out.println("Slay the Streamer | Language pack not found, default to eng.");
     } 
   }



   private void loadLocStrings(String languageKey) { BaseMod.loadCustomStringsFile(com.megacrit.cardcrawl.localization.UIStrings.class, "SlayTheStreamer/localizations/" + languageKey + "/uiStrings.json"); }
 }

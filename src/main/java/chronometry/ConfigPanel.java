 package chronometry;
 
 import basemod.ModLabel;
 import basemod.ModPanel;
 import com.badlogic.gdx.InputProcessor;
 
 
 
 public class ConfigPanel
   extends ModPanel
 {
   private InputProcessor oldInputProcessor;
   public static final float BUTTON_X = 350.0F;
   public static final float BUTTON_Y = 650.0F;
   public static final float BUTTON_LABEL_X = 475.0F;
   public static final float BUTTON_LABEL_Y = 700.0F;
   public static final float BUTTON_ENABLE_X = 350.0F;
   public static final float BUTTON_ENABLE_Y = 600.0F;
   public static final float AUTOCOMPLETE_BUTTON_ENABLE_X = 350.0F;
   public static final float AUTOCOMPLETE_BUTTON_ENABLE_Y = 550.0F;
   public static final float AUTOCOMPLETE_LABEL_X = 350.0F;
   public static final float AUTOCOMPLETE_LABEL_Y = 425.0F;
   public static final float WHATMOD_BUTTON_X = 350.0F;
   public static final float WHATMOD_BUTTON_Y = 350.0F;
   
   public ConfigPanel() {
     ModLabel buttonLabel = new ModLabel("Adjust config in your LOCALAPPDATA/ModTheSpire folder", 475.0F, 700.0F, this, me -> {
         
         });
 
 
 
     
     addUIElement(buttonLabel);
   }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
   
   public void setBool(String key, Boolean value) {
     SlayTheStreamer.config.setBool(key, value.booleanValue());
     try {
       SlayTheStreamer.config.save();
     } catch (Exception e) {
       e.printStackTrace();
     } 
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\ConfigPanel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */
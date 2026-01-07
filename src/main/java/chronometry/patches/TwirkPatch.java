package chronometry.patches;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.gikk.twirk.Twirk;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javassist.CannotCompileException;
import javassist.CtBehavior;
 
 @SpirePatch(clz = Twirk.class, method = "createResources")
 public class TwirkPatch {
   @SpireInsertPatch(locator = Locator.class, localvars = {"writer", "reader", "socket"})
   public static void Insert(Twirk __instance, @ByRef BufferedWriter[] writer, @ByRef BufferedReader[] reader, @ByRef Socket[] socket) throws IOException {
     if (!Loader.isModLoaded("bettertwitchmod")) {
       
       writer[0] = new BufferedWriter(new OutputStreamWriter(socket[0].getOutputStream(), StandardCharsets.UTF_8));
       reader[0] = new BufferedReader(new InputStreamReader(socket[0].getInputStream(), StandardCharsets.UTF_8));
     } 
   }
   
   private static class Locator
     extends SpireInsertLocator
   {
     public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
       Matcher.FieldAccessMatcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(Twirk.class, "outThread");
       return LineFinder.findInOrder(ctMethodToPatch, new ArrayList(), fieldAccessMatcher);
     }
   }
 }


/* Location:              C:\Users\gaoming\Desktop\游戏\Slay the Streamer.jar!\chronometry\patches\TwirkPatch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.0.7
 */
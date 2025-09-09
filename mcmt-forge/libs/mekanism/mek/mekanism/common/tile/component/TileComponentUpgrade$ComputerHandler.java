package mekanism.common.tile.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Map;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileComponentUpgrade.class
)
public class TileComponentUpgrade$ComputerHandler extends ComputerMethodFactory<TileComponentUpgrade> {
   private static MethodHandle fieldGetter$upgrades = getGetterHandle(TileComponentUpgrade.class, "upgrades");

   public TileComponentUpgrade$ComputerHandler() {
      this.register(
         MethodData.builder("getInstalledUpgrades", TileComponentUpgrade$ComputerHandler::getInstalledUpgrades_0)
            .returnType(Map.class)
            .returnExtra(Upgrade.class, Integer.class)
      );
      this.register(
         MethodData.builder("getSupportedUpgrades", TileComponentUpgrade$ComputerHandler::getSupportedUpgrades_0)
            .returnType(Set.class)
            .returnExtra(Upgrade.class)
      );
   }

   private static Map<Upgrade, Integer> getter$upgrades(TileComponentUpgrade subject) {
      try {
         return (Map)fieldGetter$upgrades.invokeExact((TileComponentUpgrade)subject);
      } catch (WrongMethodTypeException var2) {
         throw new RuntimeException("Getter not bound correctly", var2);
      } catch (Throwable var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public static Object getInstalledUpgrades_0(TileComponentUpgrade subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(getter$upgrades(subject), helper::convert, helper::convert);
   }

   public static Object getSupportedUpgrades_0(TileComponentUpgrade subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSupportedTypes(), helper::convert);
   }
}

package mekanism.common.tile.machine;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityDimensionalStabilizer.class
)
public class TileEntityDimensionalStabilizer$ComputerHandler extends ComputerMethodFactory<TileEntityDimensionalStabilizer> {
   private static MethodHandle fieldGetter$chunksLoaded = getGetterHandle(TileEntityDimensionalStabilizer.class, "chunksLoaded");
   private final String[] NAMES_x_z = new String[]{"x", "z"};
   private final String[] NAMES_x_z_load = new String[]{"x", "z", "load"};
   private final String[] NAMES_radius = new String[]{"radius"};
   private final Class[] TYPES_3301a1 = new Class[]{int.class, int.class};
   private final Class[] TYPES_1980e = new Class[]{int.class};
   private final Class[] TYPES_a089ea7 = new Class[]{int.class, int.class, boolean.class};

   public TileEntityDimensionalStabilizer$ComputerHandler() {
      this.register(
         MethodData.builder("getChunksLoaded", TileEntityDimensionalStabilizer$ComputerHandler::getChunksLoaded_0)
            .returnType(int.class)
            .methodDescription("Get the number of chunks being loaded.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityDimensionalStabilizer$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("isChunkLoadingAt", TileEntityDimensionalStabilizer$ComputerHandler::isChunkLoadingAt_2)
            .returnType(boolean.class)
            .methodDescription(
               "Check if the Dimensional Stabilizer is configured to load a the specified relative chunk position at x,y (Stabilizer is at 0,0). Range: [-2, 2]"
            )
            .arguments(this.NAMES_x_z, this.TYPES_3301a1)
      );
      this.register(
         MethodData.builder("toggleChunkLoadingAt", TileEntityDimensionalStabilizer$ComputerHandler::toggleChunkLoadingAt_2)
            .methodDescription(
               "Toggle loading the specified relative chunk at the relative x,y position (Stabilizer is at 0,0). Just like clicking the button in the GUI. Range: [-2, 2]"
            )
            .requiresPublicSecurity()
            .arguments(this.NAMES_x_z, this.TYPES_3301a1)
      );
      this.register(
         MethodData.builder("setChunkLoadingAt", TileEntityDimensionalStabilizer$ComputerHandler::setChunkLoadingAt_3)
            .methodDescription(
               "Set if the Dimensional Stabilizer is configured to load a the specified relative position (Stabilizer is at 0,0). True = load the chunk, false = don't load the chunk. Range: [-2, 2]"
            )
            .requiresPublicSecurity()
            .arguments(this.NAMES_x_z_load, this.TYPES_a089ea7)
      );
      this.register(
         MethodData.builder("enableChunkLoadingFor", TileEntityDimensionalStabilizer$ComputerHandler::enableChunkLoadingFor_1)
            .methodDescription("Sets the chunks in the specified radius to be loaded. The chunk the Stabilizer is in is always loaded. Range: [1, 2]")
            .requiresPublicSecurity()
            .arguments(this.NAMES_radius, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("disableChunkLoadingFor", TileEntityDimensionalStabilizer$ComputerHandler::disableChunkLoadingFor_1)
            .methodDescription("Sets the chunks in the specified radius to not be kept loaded. The chunk the Stabilizer is in is always loaded. Range: [1, 2]")
            .requiresPublicSecurity()
            .arguments(this.NAMES_radius, this.TYPES_1980e)
      );
   }

   private static int getter$chunksLoaded(TileEntityDimensionalStabilizer subject) {
      try {
         return (int)fieldGetter$chunksLoaded.invokeExact((TileEntityDimensionalStabilizer)subject);
      } catch (WrongMethodTypeException var2) {
         throw new RuntimeException("Getter not bound correctly", var2);
      } catch (Throwable var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public static Object getChunksLoaded_0(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(getter$chunksLoaded(subject));
   }

   public static Object energySlot$getEnergyItem(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object isChunkLoadingAt_2(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.computerIsChunkloadingAt(helper.getInt(0), helper.getInt(1)));
   }

   public static Object toggleChunkLoadingAt_2(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerToggleChunkLoadingAt(helper.getInt(0), helper.getInt(1));
      return helper.voidResult();
   }

   public static Object setChunkLoadingAt_3(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetChunkLoadingAt(helper.getInt(0), helper.getInt(1), helper.getBoolean(2));
      return helper.voidResult();
   }

   public static Object enableChunkLoadingFor_1(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      subject.enableChunkLoadingFor(helper.getInt(0));
      return helper.voidResult();
   }

   public static Object disableChunkLoadingFor_1(TileEntityDimensionalStabilizer subject, BaseComputerHelper helper) throws ComputerException {
      subject.disableChunkLoadingFor(helper.getInt(0));
      return helper.voidResult();
   }
}

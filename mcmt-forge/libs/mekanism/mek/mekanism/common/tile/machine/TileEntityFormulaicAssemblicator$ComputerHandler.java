package mekanism.common.tile.machine;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityFormulaicAssemblicator.class
)
public class TileEntityFormulaicAssemblicator$ComputerHandler extends ComputerMethodFactory<TileEntityFormulaicAssemblicator> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final String[] NAMES_slot = new String[]{"slot"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityFormulaicAssemblicator$ComputerHandler() {
      this.register(
         MethodData.builder("getExcessRemainingItems", TileEntityFormulaicAssemblicator$ComputerHandler::getExcessRemainingItems_0)
            .returnType(NonNullList.class)
            .returnExtra(ItemStack.class)
      );
      this.register(
         MethodData.builder("getFormulaItem", TileEntityFormulaicAssemblicator$ComputerHandler::formulaSlot$getFormulaItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the formula slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityFormulaicAssemblicator$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(MethodData.builder("hasRecipe", TileEntityFormulaicAssemblicator$ComputerHandler::hasRecipe_0).returnType(boolean.class));
      this.register(MethodData.builder("getRecipeProgress", TileEntityFormulaicAssemblicator$ComputerHandler::getRecipeProgress_0).returnType(int.class));
      this.register(MethodData.builder("getTicksRequired", TileEntityFormulaicAssemblicator$ComputerHandler::getTicksRequired_0).returnType(int.class));
      this.register(
         MethodData.builder("getCraftingInputSlot", TileEntityFormulaicAssemblicator$ComputerHandler::getCraftingInputSlot_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("getCraftingOutputSlots", TileEntityFormulaicAssemblicator$ComputerHandler::getCraftingOutputSlots_0).returnType(int.class)
      );
      this.register(
         MethodData.builder("getCraftingOutputSlot", TileEntityFormulaicAssemblicator$ComputerHandler::getCraftingOutputSlot_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
      this.register(MethodData.builder("hasValidFormula", TileEntityFormulaicAssemblicator$ComputerHandler::hasValidFormula_0).returnType(boolean.class));
      this.register(MethodData.builder("getSlots", TileEntityFormulaicAssemblicator$ComputerHandler::getSlots_0).returnType(int.class));
      this.register(
         MethodData.builder("getItemInSlot", TileEntityFormulaicAssemblicator$ComputerHandler::getItemInSlot_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("encodeFormula", TileEntityFormulaicAssemblicator$ComputerHandler::encodeFormula_0)
            .methodDescription("Requires an unencoded formula in the formula slot and a valid recipe")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("emptyGrid", TileEntityFormulaicAssemblicator$ComputerHandler::emptyGrid_0)
            .methodDescription("Requires auto mode to be disabled")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("fillGrid", TileEntityFormulaicAssemblicator$ComputerHandler::fillGrid_0)
            .methodDescription("Requires auto mode to be disabled")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("craftSingleItem", TileEntityFormulaicAssemblicator$ComputerHandler::craftSingleItem_0)
            .methodDescription("Requires recipe and auto mode to be disabled")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("craftAvailableItems", TileEntityFormulaicAssemblicator$ComputerHandler::craftAvailableItems_0)
            .methodDescription("Requires recipe and auto mode to be disabled")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("getStockControl", TileEntityFormulaicAssemblicator$ComputerHandler::getStockControl_0)
            .returnType(boolean.class)
            .methodDescription("Requires valid encoded formula")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("setStockControl", TileEntityFormulaicAssemblicator$ComputerHandler::setStockControl_1)
            .methodDescription("Requires valid encoded formula")
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("getAutoMode", TileEntityFormulaicAssemblicator$ComputerHandler::getAutoMode_0)
            .returnType(boolean.class)
            .methodDescription("Requires valid encoded formula")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("setAutoMode", TileEntityFormulaicAssemblicator$ComputerHandler::setAutoMode_1)
            .methodDescription("Requires valid encoded formula")
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_3db6c47)
      );
   }

   public static Object getExcessRemainingItems_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.lastRemainingItems, helper::convert);
   }

   public static Object formulaSlot$getFormulaItem(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.formulaSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object hasRecipe_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.hasRecipe());
   }

   public static Object getRecipeProgress_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOperatingTicks());
   }

   public static Object getTicksRequired_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTicksRequired());
   }

   public static Object getCraftingInputSlot_1(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCraftingInputSlot(helper.getInt(0)));
   }

   public static Object getCraftingOutputSlots_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCraftingOutputSlots());
   }

   public static Object getCraftingOutputSlot_1(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCraftingOutputSlot(helper.getInt(0)));
   }

   public static Object hasValidFormula_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.hasValidFormula());
   }

   public static Object getSlots_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.computerGetSlots());
   }

   public static Object getItemInSlot_1(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getItemInSlot(helper.getInt(0)));
   }

   public static Object encodeFormula_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerEncodeFormula();
      return helper.voidResult();
   }

   public static Object emptyGrid_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerEmptyGrid();
      return helper.voidResult();
   }

   public static Object fillGrid_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerFillGrid();
      return helper.voidResult();
   }

   public static Object craftSingleItem_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.craftSingleItem();
      return helper.voidResult();
   }

   public static Object craftAvailableItems_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.craftAvailableItems();
      return helper.voidResult();
   }

   public static Object getStockControl_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.computerGetStockControl());
   }

   public static Object setStockControl_1(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.setStockControl(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object getAutoMode_0(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.computerGetAutoMode());
   }

   public static Object setAutoMode_1(TileEntityFormulaicAssemblicator subject, BaseComputerHelper helper) throws ComputerException {
      subject.setAutoMode(helper.getBoolean(0));
      return helper.voidResult();
   }
}

package mekanism.common.tile.qio;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityQIORedstoneAdapter.class
)
public class TileEntityQIORedstoneAdapter$ComputerHandler extends ComputerMethodFactory<TileEntityQIORedstoneAdapter> {
   private final String[] NAMES_itemName = new String[]{"itemName"};
   private final String[] NAMES_fuzzy = new String[]{"fuzzy"};
   private final String[] NAMES_amount = new String[]{"amount"};
   private final Class[] TYPES_dbbe1d5d = new Class[]{ResourceLocation.class};
   private final Class[] TYPES_32c69b = new Class[]{long.class};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public TileEntityQIORedstoneAdapter$ComputerHandler() {
      this.register(MethodData.builder("getTargetItem", TileEntityQIORedstoneAdapter$ComputerHandler::getTargetItem_0).returnType(ItemStack.class));
      this.register(MethodData.builder("getTriggerAmount", TileEntityQIORedstoneAdapter$ComputerHandler::getTriggerAmount_0).returnType(long.class));
      this.register(MethodData.builder("getFuzzyMode", TileEntityQIORedstoneAdapter$ComputerHandler::getFuzzyMode_0).returnType(boolean.class));
      this.register(MethodData.builder("clearTargetItem", TileEntityQIORedstoneAdapter$ComputerHandler::clearTargetItem_0).requiresPublicSecurity());
      this.register(
         MethodData.builder("setTargetItem", TileEntityQIORedstoneAdapter$ComputerHandler::setTargetItem_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_itemName, this.TYPES_dbbe1d5d)
      );
      this.register(
         MethodData.builder("setTriggerAmount", TileEntityQIORedstoneAdapter$ComputerHandler::setTriggerAmount_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_amount, this.TYPES_32c69b)
      );
      this.register(MethodData.builder("toggleFuzzyMode", TileEntityQIORedstoneAdapter$ComputerHandler::toggleFuzzyMode_0).requiresPublicSecurity());
      this.register(
         MethodData.builder("setFuzzyMode", TileEntityQIORedstoneAdapter$ComputerHandler::setFuzzyMode_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_fuzzy, this.TYPES_3db6c47)
      );
   }

   public static Object getTargetItem_0(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getItemType());
   }

   public static Object getTriggerAmount_0(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCount());
   }

   public static Object getFuzzyMode_0(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFuzzyMode());
   }

   public static Object clearTargetItem_0(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      subject.clearTargetItem();
      return helper.voidResult();
   }

   public static Object setTargetItem_1(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setTargetItem(helper.getResourceLocation(0));
      return helper.voidResult();
   }

   public static Object setTriggerAmount_1(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setTriggerAmount(helper.getLong(0));
      return helper.voidResult();
   }

   public static Object toggleFuzzyMode_0(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerToggleFuzzyMode();
      return helper.voidResult();
   }

   public static Object setFuzzyMode_1(TileEntityQIORedstoneAdapter subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetFuzzyMode(helper.getBoolean(0));
      return helper.voidResult();
   }
}

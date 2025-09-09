package mekanism.common.content.filter;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = IItemStackFilter.class
)
public class IItemStackFilter$ComputerHandler extends ComputerMethodFactory<IItemStackFilter> {
   private final String[] NAMES_item = new String[]{"item"};
   private final String[] NAMES_stack = new String[]{"stack"};
   private final Class[] TYPES_3ad32407 = new Class[]{ItemStack.class};
   private final Class[] TYPES_b987be9f = new Class[]{Item.class};

   public IItemStackFilter$ComputerHandler() {
      this.register(MethodData.builder("getItemStack", IItemStackFilter$ComputerHandler::getItemStack_0).threadSafe().returnType(ItemStack.class));
      this.register(
         MethodData.builder("setItemStack", IItemStackFilter$ComputerHandler::setItemStack_1).threadSafe().arguments(this.NAMES_stack, this.TYPES_3ad32407)
      );
      this.register(MethodData.builder("setItem", IItemStackFilter$ComputerHandler::setItem_1).arguments(this.NAMES_item, this.TYPES_b987be9f));
   }

   public static Object getItemStack_0(IItemStackFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getItemStack());
   }

   public static Object setItemStack_1(IItemStackFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setItemStack(helper.getItemStack(0));
      return helper.voidResult();
   }

   public static Object setItem_1(IItemStackFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setItem(helper.getItem(0));
      return helper.voidResult();
   }
}

package mekanism.common.content.miner;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.Item;

@MethodFactory(
   target = MinerFilter.class
)
public class MinerFilter$ComputerHandler extends ComputerMethodFactory<MinerFilter> {
   private final String[] NAMES_value = new String[]{"value"};
   private final Class[] TYPES_b987be9f = new Class[]{Item.class};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public MinerFilter$ComputerHandler() {
      this.register(MethodData.builder("getReplaceTarget", MinerFilter$ComputerHandler::getReplaceTarget_0).threadSafe().returnType(Item.class));
      this.register(
         MethodData.builder("setReplaceTarget", MinerFilter$ComputerHandler::setReplaceTarget_1).threadSafe().arguments(this.NAMES_value, this.TYPES_b987be9f)
      );
      this.register(MethodData.builder("getRequiresReplacement", MinerFilter$ComputerHandler::getRequiresReplacement_0).threadSafe().returnType(boolean.class));
      this.register(
         MethodData.builder("setRequiresReplacement", MinerFilter$ComputerHandler::setRequiresReplacement_1)
            .threadSafe()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
      this.register(MethodData.builder("hasBlacklistedElement", MinerFilter$ComputerHandler::hasBlacklistedElement_0).returnType(boolean.class));
      this.register(MethodData.builder("clone", MinerFilter$ComputerHandler::clone_0).threadSafe().returnType(MinerFilter.class));
   }

   public static Object getReplaceTarget_0(MinerFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.replaceTarget);
   }

   public static Object setReplaceTarget_1(MinerFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.replaceTarget = helper.getItem(0);
      return helper.voidResult();
   }

   public static Object getRequiresReplacement_0(MinerFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.requiresReplacement);
   }

   public static Object setRequiresReplacement_1(MinerFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.requiresReplacement = helper.getBoolean(0);
      return helper.voidResult();
   }

   public static Object hasBlacklistedElement_0(MinerFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.hasBlacklistedElement());
   }

   public static Object clone_0(MinerFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.clone());
   }
}

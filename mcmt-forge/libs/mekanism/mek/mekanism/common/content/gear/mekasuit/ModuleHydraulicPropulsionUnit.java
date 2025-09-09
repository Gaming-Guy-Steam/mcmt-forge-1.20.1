package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;

@ParametersAreNotNullByDefault
public class ModuleHydraulicPropulsionUnit implements ICustomModule<ModuleHydraulicPropulsionUnit> {
   private IModuleConfigItem<ModuleHydraulicPropulsionUnit.JumpBoost> jumpBoost;
   private IModuleConfigItem<ModuleHydraulicPropulsionUnit.StepAssist> stepAssist;

   @Override
   public void init(IModule<ModuleHydraulicPropulsionUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.jumpBoost = configItemCreator.createConfigItem(
         "jump_boost", MekanismLang.MODULE_JUMP_BOOST, new ModuleEnumData<>(ModuleHydraulicPropulsionUnit.JumpBoost.LOW, module.getInstalledCount() + 1)
      );
      this.stepAssist = configItemCreator.createConfigItem(
         "step_assist", MekanismLang.MODULE_STEP_ASSIST, new ModuleEnumData<>(ModuleHydraulicPropulsionUnit.StepAssist.LOW, module.getInstalledCount() + 1)
      );
   }

   public float getBoost() {
      return this.jumpBoost.get().getBoost();
   }

   public float getStepHeight() {
      return this.stepAssist.get().getHeight();
   }

   @NothingNullByDefault
   public static enum JumpBoost implements IHasTextComponent {
      OFF(0.0F),
      LOW(0.5F),
      MED(1.0F),
      HIGH(3.0F),
      ULTRA(5.0F);

      private final float boost;
      private final Component label;

      private JumpBoost(float boost) {
         this.boost = boost;
         this.label = TextComponentUtil.getString(Float.toString(boost));
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public float getBoost() {
         return this.boost;
      }
   }

   @NothingNullByDefault
   public static enum StepAssist implements IHasTextComponent {
      OFF(0.0F),
      LOW(0.5F),
      MED(1.0F),
      HIGH(1.5F),
      ULTRA(2.0F);

      private final float height;
      private final Component label;

      private StepAssist(float height) {
         this.height = height;
         this.label = TextComponentUtil.getString(Float.toString(height));
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public float getHeight() {
         return this.height;
      }
   }
}

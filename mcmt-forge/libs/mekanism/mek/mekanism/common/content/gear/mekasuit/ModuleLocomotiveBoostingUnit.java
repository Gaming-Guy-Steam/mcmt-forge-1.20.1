package mekanism.common.content.gear.mekasuit;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@ParametersAreNotNullByDefault
public class ModuleLocomotiveBoostingUnit implements ICustomModule<ModuleLocomotiveBoostingUnit> {
   private IModuleConfigItem<ModuleLocomotiveBoostingUnit.SprintBoost> sprintBoost;

   @Override
   public void init(IModule<ModuleLocomotiveBoostingUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.sprintBoost = configItemCreator.createConfigItem(
         "sprint_boost", MekanismLang.MODULE_SPRINT_BOOST, new ModuleEnumData<>(ModuleLocomotiveBoostingUnit.SprintBoost.LOW, module.getInstalledCount() + 1)
      );
   }

   @Override
   public void changeMode(IModule<ModuleLocomotiveBoostingUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
      ModuleLocomotiveBoostingUnit.SprintBoost currentMode = this.sprintBoost.get();
      ModuleLocomotiveBoostingUnit.SprintBoost newMode = currentMode.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 1);
      if (currentMode != newMode) {
         this.sprintBoost.set(newMode);
         if (displayChangeMessage) {
            module.displayModeChange(player, MekanismLang.MODULE_SPRINT_BOOST.translate(new Object[0]), newMode);
         }
      }
   }

   @Override
   public void tickServer(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
      if (this.tick(module, player)) {
         module.useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply((double)(this.getBoost() / 0.1F)));
      }
   }

   @Override
   public void tickClient(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
      this.tick(module, player);
   }

   private boolean tick(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
      if (this.canFunction(module, player)) {
         float boost = this.getBoost();
         if (!player.m_20096_()) {
            boost /= 5.0F;
         }

         if (player.m_20069_()) {
            boost /= 5.0F;
         }

         player.m_19920_(boost, new Vec3(0.0, 0.0, 1.0));
         return true;
      } else {
         return false;
      }
   }

   public boolean canFunction(IModule<ModuleLocomotiveBoostingUnit> module, Player player) {
      return !player.m_21255_()
         && player.m_20142_()
         && module.canUseEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get().multiply((double)(this.getBoost() / 0.1F)));
   }

   public float getBoost() {
      return this.sprintBoost.get().getBoost();
   }

   @NothingNullByDefault
   public static enum SprintBoost implements IHasTextComponent, IIncrementalEnum<ModuleLocomotiveBoostingUnit.SprintBoost> {
      OFF(0.0F),
      LOW(0.05F),
      MED(0.1F),
      HIGH(0.25F),
      ULTRA(0.5F);

      private static final ModuleLocomotiveBoostingUnit.SprintBoost[] MODES = values();
      private final float boost;
      private final Component label;

      private SprintBoost(float boost) {
         this.boost = boost;
         this.label = TextComponentUtil.getString(Float.toString(boost));
      }

      public ModuleLocomotiveBoostingUnit.SprintBoost byIndex(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public float getBoost() {
         return this.boost;
      }
   }
}

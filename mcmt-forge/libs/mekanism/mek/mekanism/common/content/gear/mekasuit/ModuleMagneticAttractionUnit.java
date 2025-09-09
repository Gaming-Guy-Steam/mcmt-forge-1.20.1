package mekanism.common.content.gear.mekasuit;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@ParametersAreNotNullByDefault
public class ModuleMagneticAttractionUnit implements ICustomModule<ModuleMagneticAttractionUnit> {
   private IModuleConfigItem<ModuleMagneticAttractionUnit.Range> range;

   @Override
   public void init(IModule<ModuleMagneticAttractionUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.range = configItemCreator.createConfigItem(
         "range", MekanismLang.MODULE_RANGE, new ModuleEnumData<>(ModuleMagneticAttractionUnit.Range.LOW, module.getInstalledCount() + 1)
      );
   }

   @Override
   public void tickServer(IModule<ModuleMagneticAttractionUnit> module, Player player) {
      if (this.range.get() != ModuleMagneticAttractionUnit.Range.OFF) {
         float size = 4.0F + this.range.get().getRange();
         FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageItemAttraction.get().multiply((double)this.range.get().getRange());
         boolean free = usage.isZero() || player.m_7500_();
         IEnergyContainer energyContainer = free ? null : module.getEnergyContainer();
         if (free || energyContainer != null && energyContainer.getEnergy().greaterOrEqual(usage)) {
            for (ItemEntity item : player.m_9236_().m_6443_(ItemEntity.class, player.m_20191_().m_82377_(size, size, size), itemx -> !itemx.m_32063_())) {
               if (item.m_20270_(player) > 0.001) {
                  if (free) {
                     this.pullItem(player, item);
                  } else {
                     if (module.useEnergy(player, energyContainer, usage, true).isZero()) {
                        break;
                     }

                     this.pullItem(player, item);
                     if (energyContainer.getEnergy().smallerThan(usage)) {
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   private void pullItem(Player player, ItemEntity item) {
      Vec3 diff = player.m_20182_().m_82546_(item.m_20182_());
      Vec3 motionNeeded = new Vec3(Math.min(diff.f_82479_, 1.0), Math.min(diff.f_82480_, 1.0), Math.min(diff.f_82481_, 1.0));
      Vec3 motionDiff = motionNeeded.m_82546_(player.m_20184_());
      item.m_20256_(motionDiff.m_82490_(0.2));
      Mekanism.packetHandler()
         .sendToAllTrackingAndSelf(
            new PacketLightningRender(
               PacketLightningRender.LightningPreset.MAGNETIC_ATTRACTION,
               Objects.hash(player.m_20148_(), item),
               player.m_20182_().m_82520_(0.0, 0.2, 0.0),
               item.m_20182_(),
               (int)(diff.m_82553_() * 4.0)
            ),
            player
         );
   }

   @Override
   public boolean canChangeModeWhenDisabled(IModule<ModuleMagneticAttractionUnit> module) {
      return true;
   }

   @Override
   public void changeMode(IModule<ModuleMagneticAttractionUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
      module.toggleEnabled(player, MekanismLang.MODULE_MAGNETIC_ATTRACTION.translate(new Object[0]));
   }

   @NothingNullByDefault
   public static enum Range implements IHasTextComponent {
      OFF(0.0F),
      LOW(1.0F),
      MED(3.0F),
      HIGH(5.0F),
      ULTRA(10.0F);

      private final float range;
      private final Component label;

      private Range(float boost) {
         this.range = boost;
         this.label = TextComponentUtil.getString(Float.toString(boost));
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public float getRange() {
         return this.range;
      }
   }
}

package mekanism.common.integration.lookingat.theoneprobe;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.IProbeConfig.ConfigMode;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class TOPProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {
   private BooleanSupplier displayFluidTanks;
   private Supplier<ConfigMode> tankMode = () -> ConfigMode.EXTENDED;

   public Void apply(ITheOneProbe probe) {
      probe.registerProvider(this);
      probe.registerEntityProvider(TOPEntityProvider.INSTANCE);
      probe.registerProbeConfigProvider(ProbeConfigProvider.INSTANCE);
      probe.registerElementFactory(new TOPEnergyElement.Factory());
      probe.registerElementFactory(new TOPFluidElement.Factory());
      probe.registerElementFactory(new TOPChemicalElement.GasElementFactory());
      probe.registerElementFactory(new TOPChemicalElement.InfuseTypeElementFactory());
      probe.registerElementFactory(new TOPChemicalElement.PigmentElementFactory());
      probe.registerElementFactory(new TOPChemicalElement.SlurryElementFactory());
      IProbeConfig probeConfig = probe.createProbeConfig();
      this.displayFluidTanks = () -> probeConfig.getTankMode() > 0;
      this.tankMode = probeConfig::getShowTankSetting;
      return null;
   }

   public ResourceLocation getID() {
      return Mekanism.rl("data");
   }

   public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, BlockState blockState, IProbeHitData data) {
      BlockPos pos = data.getPos();
      if (blockState.m_60734_() instanceof BlockBounding) {
         BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
         if (mainPos != null) {
            pos = mainPos;
         }
      }

      BlockEntity tile = WorldUtils.getTileEntity(world, pos);
      if (tile != null) {
         LookingAtUtils.addInfo(new TOPProvider.TOPLookingAtHelper(info), tile, this.displayTanks(mode), this.displayFluidTanks.getAsBoolean());
      }
   }

   private boolean displayTanks(ProbeMode mode) {
      return switch ((ConfigMode)this.tankMode.get()) {
         case NOT -> false;
         case NORMAL -> mode == ProbeMode.NORMAL;
         case EXTENDED -> mode == ProbeMode.EXTENDED;
         default -> throw new IncompatibleClassChangeError();
      };
   }

   static class TOPLookingAtHelper implements LookingAtHelper {
      private final IProbeInfo info;

      public TOPLookingAtHelper(IProbeInfo info) {
         this.info = info;
      }

      @Override
      public void addText(Component text) {
         this.info.text(CompoundText.create().name(text).get());
      }

      @Override
      public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
         this.info.element(new TOPEnergyElement(energy, maxEnergy));
      }

      @Override
      public void addFluidElement(FluidStack stored, int capacity) {
         this.info.element(new TOPFluidElement(stored, capacity));
      }

      @Override
      public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
         TOPChemicalElement element = TOPChemicalElement.create(stored, capacity);
         if (element != null) {
            this.info.element(element);
         }
      }
   }
}

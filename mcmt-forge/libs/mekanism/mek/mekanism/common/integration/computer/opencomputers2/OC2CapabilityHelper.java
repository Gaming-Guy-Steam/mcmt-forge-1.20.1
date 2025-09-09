package mekanism.common.integration.computer.opencomputers2;

import li.cil.oc2.api.bus.device.Device;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class OC2CapabilityHelper {
   private static final Capability<Device> CAPABILITY = CapabilityManager.get(new CapabilityToken<Device>() {});

   public static <TILE extends BlockEntity & IComputerTile> ICapabilityResolver getOpenComputers2Capability(TILE tile) {
      return tile.isComputerCapabilityPersistent()
         ? BasicCapabilityResolver.persistent(CAPABILITY, () -> MekanismDevice.create(tile))
         : BasicCapabilityResolver.create(CAPABILITY, () -> MekanismDevice.create(tile));
   }
}

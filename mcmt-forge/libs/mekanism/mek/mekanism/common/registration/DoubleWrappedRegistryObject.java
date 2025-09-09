package mekanism.common.registration;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraftforge.registries.RegistryObject;

@NothingNullByDefault
public class DoubleWrappedRegistryObject<PRIMARY, SECONDARY> implements INamedEntry {
   protected final RegistryObject<PRIMARY> primaryRO;
   protected final RegistryObject<SECONDARY> secondaryRO;

   public DoubleWrappedRegistryObject(RegistryObject<PRIMARY> primaryRO, RegistryObject<SECONDARY> secondaryRO) {
      this.primaryRO = primaryRO;
      this.secondaryRO = secondaryRO;
   }

   public PRIMARY getPrimary() {
      return (PRIMARY)this.primaryRO.get();
   }

   public SECONDARY getSecondary() {
      return (SECONDARY)this.secondaryRO.get();
   }

   @Override
   public String getInternalRegistryName() {
      return this.primaryRO.getId().m_135815_();
   }
}

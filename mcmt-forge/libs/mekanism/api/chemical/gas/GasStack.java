package mekanism.api.chemical.gas;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IGasProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class GasStack extends ChemicalStack<Gas> {
   public static final GasStack EMPTY = new GasStack(MekanismAPI.EMPTY_GAS, 0L);

   public GasStack(IGasProvider gasProvider, long amount) {
      super(gasProvider.getChemical(), amount);
   }

   public GasStack(GasStack stack, long amount) {
      this(stack.getType(), amount);
   }

   @Override
   protected IForgeRegistry<Gas> getRegistry() {
      return MekanismAPI.gasRegistry();
   }

   protected Gas getEmptyChemical() {
      return MekanismAPI.EMPTY_GAS;
   }

   public static GasStack readFromNBT(@Nullable CompoundTag nbtTags) {
      if (nbtTags != null && !nbtTags.m_128456_()) {
         Gas type = Gas.readFromNBT(nbtTags);
         if (type.isEmptyType()) {
            return EMPTY;
         } else {
            long amount = nbtTags.m_128454_("amount");
            return amount <= 0L ? EMPTY : new GasStack(type, amount);
         }
      } else {
         return EMPTY;
      }
   }

   public static GasStack readFromPacket(FriendlyByteBuf buf) {
      Gas gas = (Gas)buf.readRegistryIdSafe(Gas.class);
      return gas.isEmptyType() ? EMPTY : new GasStack(gas, buf.m_130258_());
   }

   public GasStack copy() {
      return this.isEmpty() ? EMPTY : new GasStack(this, this.getAmount());
   }
}

package mekanism.api.chemical.pigment;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class PigmentStack extends ChemicalStack<Pigment> {
   public static final PigmentStack EMPTY = new PigmentStack(MekanismAPI.EMPTY_PIGMENT, 0L);

   public PigmentStack(IPigmentProvider pigmentProvider, long amount) {
      super(pigmentProvider.getChemical(), amount);
   }

   public PigmentStack(PigmentStack stack, long amount) {
      this(stack.getType(), amount);
   }

   @Override
   protected IForgeRegistry<Pigment> getRegistry() {
      return MekanismAPI.pigmentRegistry();
   }

   protected Pigment getEmptyChemical() {
      return MekanismAPI.EMPTY_PIGMENT;
   }

   public static PigmentStack readFromNBT(@Nullable CompoundTag nbtTags) {
      if (nbtTags != null && !nbtTags.m_128456_()) {
         Pigment type = Pigment.readFromNBT(nbtTags);
         if (type.isEmptyType()) {
            return EMPTY;
         } else {
            long amount = nbtTags.m_128454_("amount");
            return amount <= 0L ? EMPTY : new PigmentStack(type, amount);
         }
      } else {
         return EMPTY;
      }
   }

   public static PigmentStack readFromPacket(FriendlyByteBuf buf) {
      Pigment pigment = (Pigment)buf.readRegistryIdSafe(Pigment.class);
      return pigment.isEmptyType() ? EMPTY : new PigmentStack(pigment, buf.m_130258_());
   }

   public PigmentStack copy() {
      return this.isEmpty() ? EMPTY : new PigmentStack(this, this.getAmount());
   }
}

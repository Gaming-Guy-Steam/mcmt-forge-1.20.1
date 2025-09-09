package mekanism.api.chemical.slurry;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class SlurryStack extends ChemicalStack<Slurry> {
   public static final SlurryStack EMPTY = new SlurryStack(MekanismAPI.EMPTY_SLURRY, 0L);

   public SlurryStack(ISlurryProvider slurryProvider, long amount) {
      super(slurryProvider.getChemical(), amount);
   }

   public SlurryStack(SlurryStack stack, long amount) {
      this(stack.getType(), amount);
   }

   @Override
   protected IForgeRegistry<Slurry> getRegistry() {
      return MekanismAPI.slurryRegistry();
   }

   protected Slurry getEmptyChemical() {
      return MekanismAPI.EMPTY_SLURRY;
   }

   public static SlurryStack readFromNBT(@Nullable CompoundTag nbtTags) {
      if (nbtTags != null && !nbtTags.m_128456_()) {
         Slurry type = Slurry.readFromNBT(nbtTags);
         if (type.isEmptyType()) {
            return EMPTY;
         } else {
            long amount = nbtTags.m_128454_("amount");
            return amount <= 0L ? EMPTY : new SlurryStack(type, amount);
         }
      } else {
         return EMPTY;
      }
   }

   public static SlurryStack readFromPacket(FriendlyByteBuf buf) {
      Slurry slurry = (Slurry)buf.readRegistryIdSafe(Slurry.class);
      return slurry.isEmptyType() ? EMPTY : new SlurryStack(slurry, buf.m_130258_());
   }

   public SlurryStack copy() {
      return this.isEmpty() ? EMPTY : new SlurryStack(this, this.getAmount());
   }
}

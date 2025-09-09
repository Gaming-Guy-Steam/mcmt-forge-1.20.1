package mekanism.api.chemical.infuse;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class InfusionStack extends ChemicalStack<InfuseType> {
   public static final InfusionStack EMPTY = new InfusionStack(MekanismAPI.EMPTY_INFUSE_TYPE, 0L);

   public InfusionStack(IInfuseTypeProvider infuseTypeProvider, long amount) {
      super(infuseTypeProvider.getChemical(), amount);
   }

   public InfusionStack(InfusionStack stack, long amount) {
      this(stack.getType(), amount);
   }

   @Override
   protected IForgeRegistry<InfuseType> getRegistry() {
      return MekanismAPI.infuseTypeRegistry();
   }

   protected InfuseType getEmptyChemical() {
      return MekanismAPI.EMPTY_INFUSE_TYPE;
   }

   public static InfusionStack readFromNBT(@Nullable CompoundTag nbtTags) {
      if (nbtTags != null && !nbtTags.m_128456_()) {
         InfuseType type = InfuseType.readFromNBT(nbtTags);
         if (type.isEmptyType()) {
            return EMPTY;
         } else {
            long amount = nbtTags.m_128454_("amount");
            return amount <= 0L ? EMPTY : new InfusionStack(type, amount);
         }
      } else {
         return EMPTY;
      }
   }

   public static InfusionStack readFromPacket(FriendlyByteBuf buf) {
      InfuseType infuseType = (InfuseType)buf.readRegistryIdSafe(InfuseType.class);
      return infuseType.isEmptyType() ? EMPTY : new InfusionStack(infuseType, buf.m_130258_());
   }

   public InfusionStack copy() {
      return this.isEmpty() ? EMPTY : new InfusionStack(this, this.getAmount());
   }
}

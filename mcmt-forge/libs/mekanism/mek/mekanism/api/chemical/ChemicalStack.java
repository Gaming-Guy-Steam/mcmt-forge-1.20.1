package mekanism.api.chemical;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.attribute.IChemicalAttributeContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Holder.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalStack<CHEMICAL extends Chemical<CHEMICAL>>
   implements IHasTextComponent,
   IHasTranslationKey,
   IChemicalAttributeContainer<ChemicalStack<CHEMICAL>> {
   private boolean isEmpty;
   private long amount;
   private final Reference<CHEMICAL> chemicalDelegate;

   protected ChemicalStack(CHEMICAL chemical, long amount) {
      IForgeRegistry<CHEMICAL> registry = this.getRegistry();
      if (registry.getKey(chemical) == null) {
         MekanismAPI.logger
            .error(
               LogUtils.FATAL_MARKER,
               "Failed attempt to create a ChemicalStack for an unregistered Chemical {} (type {})",
               chemical.getRegistryName(),
               chemical.getClass().getName()
            );
         throw new IllegalArgumentException("Cannot create a ChemicalStack from an unregistered Chemical");
      } else {
         this.chemicalDelegate = registry.getDelegateOrThrow(chemical);
         this.amount = amount;
         this.updateEmpty();
      }
   }

   protected abstract IForgeRegistry<CHEMICAL> getRegistry();

   protected abstract CHEMICAL getEmptyChemical();

   public abstract ChemicalStack<CHEMICAL> copy();

   public final CHEMICAL getType() {
      return this.isEmpty ? this.getEmptyChemical() : this.getRaw();
   }

   public boolean isTypeEqual(ChemicalStack<CHEMICAL> stack) {
      return this.isTypeEqual(stack.getType());
   }

   public boolean isTypeEqual(CHEMICAL chemical) {
      return this.getType() == chemical;
   }

   public ResourceLocation getTypeRegistryName() {
      return this.getType().getRegistryName();
   }

   public int getChemicalTint() {
      return this.getType().getTint();
   }

   public int getChemicalColorRepresentation() {
      return this.getType().getColorRepresentation();
   }

   public final CHEMICAL getRaw() {
      return (CHEMICAL)this.chemicalDelegate.get();
   }

   public boolean isEmpty() {
      return this.isEmpty;
   }

   protected void updateEmpty() {
      this.isEmpty = this.getRaw().isEmptyType() || this.amount <= 0L;
   }

   public long getAmount() {
      return this.isEmpty ? 0L : this.amount;
   }

   public void setAmount(long amount) {
      if (this.getRaw().isEmptyType()) {
         throw new IllegalStateException("Can't modify the empty stack.");
      } else {
         this.amount = amount;
         this.updateEmpty();
      }
   }

   public void grow(long amount) {
      this.setAmount(this.amount + amount);
   }

   public void shrink(long amount) {
      this.setAmount(this.amount - amount);
   }

   @Override
   public boolean has(Class<? extends ChemicalAttribute> type) {
      return this.getType().has(type);
   }

   @Nullable
   @Override
   public <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
      return this.getType().get(type);
   }

   @Override
   public Collection<ChemicalAttribute> getAttributes() {
      return this.getType().getAttributes();
   }

   @Override
   public Collection<Class<? extends ChemicalAttribute>> getAttributeTypes() {
      return this.getType().getAttributeTypes();
   }

   @Override
   public int hashCode() {
      int code = 1;
      code = 31 * code + this.getType().hashCode();
      return 31 * code + Long.hashCode(this.getAmount());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ChemicalStack<?> other = (ChemicalStack<?>)o;
         return this.getType() == other.getType() && this.getAmount() == other.getAmount();
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return "[" + this.getType() + ", " + this.amount + "]";
   }

   @Override
   public Component getTextComponent() {
      return this.getType().getTextComponent();
   }

   @Override
   public String getTranslationKey() {
      return this.getType().getTranslationKey();
   }

   public boolean contains(ChemicalStack<CHEMICAL> other) {
      return this.isTypeEqual(other) && this.amount >= other.amount;
   }

   public boolean isStackIdentical(ChemicalStack<CHEMICAL> other) {
      return this.isTypeEqual(other) && this.amount == other.amount;
   }

   public CompoundTag write(CompoundTag nbtTags) {
      this.getType().write(nbtTags);
      nbtTags.m_128356_("amount", this.getAmount());
      return nbtTags;
   }

   public void writeToPacket(FriendlyByteBuf buffer) {
      buffer.writeRegistryId(this.getRegistry(), this.getType());
      if (!this.isEmpty()) {
         buffer.m_130103_(this.getAmount());
      }
   }
}

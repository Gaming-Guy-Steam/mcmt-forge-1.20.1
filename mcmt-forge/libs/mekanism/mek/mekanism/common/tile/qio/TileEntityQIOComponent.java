package mekanism.common.tile.qio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIOComponent extends TileEntityMekanism implements IQIOFrequencyHolder, ISustainedData {
   @Nullable
   private EnumColor lastColor;

   public TileEntityQIOComponent(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.frequencyComponent.track(FrequencyType.QIO, true, true, true);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @Nullable
   public EnumColor getColor() {
      return this.lastColor;
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      EnumColor prev = this.lastColor;
      QIOFrequency frequency = this.getQIOFrequency();
      this.lastColor = frequency == null ? null : frequency.getColor();
      if (prev != this.lastColor) {
         this.sendUpdatePacket();
      }

      if (this.f_58857_.m_46467_() % 10L == 0L) {
         this.setActive(frequency != null);
      }
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      if (this.lastColor != null) {
         NBTUtils.writeEnum(dataMap, "color", this.lastColor);
      }
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      EnumColor color = dataMap.m_128425_("color", 3) ? EnumColor.byIndexStatic(dataMap.m_128451_("color")) : null;
      if (this.lastColor != color) {
         this.lastColor = color;
      }
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("color", "color");
      return remap;
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      if (this.lastColor != null) {
         NBTUtils.writeEnum(updateTag, "color", this.lastColor);
      }

      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      EnumColor color = tag.m_128425_("color", 3) ? EnumColor.byIndexStatic(tag.m_128451_("color")) : null;
      if (this.lastColor != color) {
         this.lastColor = color;
         WorldUtils.updateBlock(this.m_58904_(), this.m_58899_(), this.m_58900_());
      }
   }

   @ComputerMethod(
      methodDescription = "Lists public frequencies"
   )
   Collection<QIOFrequency> getFrequencies() {
      return FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequencies();
   }

   @ComputerMethod
   boolean hasFrequency() {
      QIOFrequency frequency = this.getQIOFrequency();
      return frequency != null && frequency.isValid() && !frequency.isRemoved();
   }

   @ComputerMethod(
      nameOverride = "getFrequency",
      methodDescription = "Requires a frequency to be selected"
   )
   QIOFrequency computerGetFrequency() throws ComputerException {
      QIOFrequency frequency = this.getQIOFrequency();
      if (frequency != null && frequency.isValid() && !frequency.isRemoved()) {
         return frequency;
      } else {
         throw new ComputerException("No frequency is currently selected.");
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a public frequency to exist"
   )
   void setFrequency(String name) throws ComputerException {
      this.validateSecurityIsPublic();
      QIOFrequency frequency = FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequency(name);
      if (frequency == null) {
         throw new ComputerException("No public QIO frequency with name '%s' found.", name);
      } else {
         this.setFrequency(FrequencyType.QIO, frequency.getIdentity(), this.getOwnerUUID());
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation"
   )
   void createFrequency(String name) throws ComputerException {
      this.validateSecurityIsPublic();
      QIOFrequency frequency = FrequencyType.QIO.getManagerWrapper().getPublicManager().getFrequency(name);
      if (frequency != null) {
         throw new ComputerException("Unable to create public QIO frequency with name '%s' as one already exists.", name);
      } else {
         this.setFrequency(FrequencyType.QIO, new Frequency.FrequencyIdentity(name, true), this.getOwnerUUID());
      }
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   EnumColor getFrequencyColor() throws ComputerException {
      return this.computerGetFrequency().getColor();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a frequency to be selected"
   )
   void setFrequencyColor(EnumColor color) throws ComputerException {
      this.validateSecurityIsPublic();
      this.computerGetFrequency().setColor(color);
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a frequency to be selected"
   )
   void incrementFrequencyColor() throws ComputerException {
      this.validateSecurityIsPublic();
      QIOFrequency frequency = this.computerGetFrequency();
      frequency.setColor(frequency.getColor().getNext());
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a frequency to be selected"
   )
   void decrementFrequencyColor() throws ComputerException {
      this.validateSecurityIsPublic();
      QIOFrequency frequency = this.computerGetFrequency();
      frequency.setColor(frequency.getColor().getPrevious());
   }
}

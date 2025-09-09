package mekanism.common.content.network.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.LogisticalTransporterUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LogisticalTransporter extends LogisticalTransporterBase implements IUpgradeableTransmitter<LogisticalTransporterUpgradeData> {
   private EnumColor color;

   public LogisticalTransporter(IBlockProvider blockProvider, TileEntityTransmitter tile) {
      super(tile, Attribute.getTier(blockProvider, TransporterTier.class));
   }

   public TransporterTier getTier() {
      return this.tier;
   }

   @Override
   public EnumColor getColor() {
      return this.color;
   }

   public void setColor(EnumColor c) {
      this.color = c;
   }

   @Override
   public InteractionResult onConfigure(Player player, Direction side) {
      TransporterUtils.incrementColor(this);
      PathfinderCache.onChanged(this.getTransmitterNetwork());
      this.getTransmitterTile().sendUpdatePacket();
      EnumColor color = this.getColor();
      player.m_5661_(
         MekanismLang.TOGGLE_COLOR
            .translateColored(
               EnumColor.GRAY, new Object[]{color == null ? MekanismLang.NONE.translateColored(EnumColor.WHITE, new Object[0]) : color.getColoredName()}
            ),
         true
      );
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult onRightClick(Player player, Direction side) {
      EnumColor color = this.getColor();
      player.m_5661_(
         MekanismLang.CURRENT_COLOR
            .translateColored(
               EnumColor.GRAY, new Object[]{color == null ? MekanismLang.NONE.translateColored(EnumColor.WHITE, new Object[0]) : color.getColoredName()}
            ),
         true
      );
      return super.onRightClick(player, side);
   }

   @Nullable
   public LogisticalTransporterUpgradeData getUpgradeData() {
      return new LogisticalTransporterUpgradeData(
         this.redstoneReactive, this.getConnectionTypesRaw(), this.getColor(), this.transit, this.needsSync, this.nextId, this.delay, this.delayCount
      );
   }

   @Override
   public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
      return data instanceof LogisticalTransporterUpgradeData;
   }

   public void parseUpgradeData(@NotNull LogisticalTransporterUpgradeData data) {
      this.redstoneReactive = data.redstoneReactive;
      this.setConnectionTypesRaw(data.connectionTypes);
      this.setColor(data.color);
      this.transit.putAll(data.transit);
      this.needsSync.putAll(data.needsSync);
      this.nextId = data.nextId;
      this.delay = data.delay;
      this.delayCount = data.delayCount;
   }

   @Override
   protected void readFromNBT(CompoundTag nbtTags) {
      super.readFromNBT(nbtTags);
      NBTUtils.setEnumIfPresent(nbtTags, "color", TransporterUtils::readColor, this::setColor);
   }

   @Override
   public void writeToNBT(CompoundTag nbtTags) {
      super.writeToNBT(nbtTags);
      nbtTags.m_128405_("color", TransporterUtils.getColorIndex(this.getColor()));
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
      updateTag = super.getReducedUpdateTag(updateTag);
      updateTag.m_128405_("color", TransporterUtils.getColorIndex(this.getColor()));
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setEnumIfPresent(tag, "color", TransporterUtils::readColor, this::setColor);
   }
}

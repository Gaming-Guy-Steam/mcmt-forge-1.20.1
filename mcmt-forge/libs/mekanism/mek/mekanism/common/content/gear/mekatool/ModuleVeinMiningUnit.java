package mekanism.common.content.gear.mekatool;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.BasicRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleVeinMiningUnit implements ICustomModule<ModuleVeinMiningUnit> {
   private static final IRadialDataHelper.BooleanRadialModes RADIAL_MODES = new IRadialDataHelper.BooleanRadialModes(
      new BasicRadialMode(MekanismLang.RADIAL_VEIN_NORMAL, ItemAtomicDisassembler.DisassemblerMode.VEIN.icon(), EnumColor.AQUA),
      new BasicRadialMode(
         MekanismLang.RADIAL_VEIN_EXTENDED, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "vein_extended.png"), EnumColor.PINK
      )
   );
   private static final RadialData<IRadialMode> RADIAL_DATA = IRadialDataHelper.INSTANCE.booleanBasedData(Mekanism.rl("vein_mining_mode"), RADIAL_MODES);
   private static final NestedRadialMode NESTED_RADIAL_MODE = new NestedRadialMode(
      RADIAL_DATA, MekanismLang.RADIAL_VEIN, ItemAtomicDisassembler.DisassemblerMode.VEIN.icon(), EnumColor.AQUA
   );
   private IModuleConfigItem<Boolean> extendedMode;
   private IModuleConfigItem<ModuleVeinMiningUnit.ExcavationRange> excavationRange;

   @Override
   public void init(IModule<ModuleVeinMiningUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.extendedMode = configItemCreator.createDisableableConfigItem(
         "extended_mode", MekanismLang.MODULE_EXTENDED_MODE, false, MekanismConfig.gear.mekaToolExtendedMining
      );
      this.excavationRange = configItemCreator.createConfigItem(
         "excavation_range",
         MekanismLang.MODULE_EXCAVATION_RANGE,
         new ModuleEnumData<>(ModuleVeinMiningUnit.ExcavationRange.LOW, module.getInstalledCount() + 1)
      );
   }

   @Override
   public void addRadialModes(IModule<ModuleVeinMiningUnit> module, @NotNull ItemStack stack, Consumer<NestedRadialMode> adder) {
      if (MekanismConfig.gear.mekaToolExtendedMining.get()) {
         adder.accept(NESTED_RADIAL_MODE);
      }
   }

   @Nullable
   @Override
   public <MODE extends IRadialMode> MODE getMode(IModule<ModuleVeinMiningUnit> module, ItemStack stack, RadialData<MODE> radialData) {
      return (MODE)(radialData == RADIAL_DATA && MekanismConfig.gear.mekaToolExtendedMining.get() ? RADIAL_MODES.get(this.isExtended()) : null);
   }

   @Override
   public <MODE extends IRadialMode> boolean setMode(
      IModule<ModuleVeinMiningUnit> module, Player player, ItemStack stack, RadialData<MODE> radialData, MODE mode
   ) {
      if (radialData == RADIAL_DATA && MekanismConfig.gear.mekaToolExtendedMining.get()) {
         boolean extended = mode == RADIAL_MODES.trueMode();
         if (this.isExtended() != extended) {
            this.extendedMode.set(extended);
         }
      }

      return false;
   }

   @Nullable
   @Override
   public Component getModeScrollComponent(IModule<ModuleVeinMiningUnit> module, ItemStack stack) {
      return this.isExtended()
         ? MekanismLang.RADIAL_VEIN_EXTENDED.translateColored(EnumColor.PINK, new Object[0])
         : MekanismLang.RADIAL_VEIN_NORMAL.translateColored(EnumColor.AQUA, new Object[0]);
   }

   @Override
   public void changeMode(IModule<ModuleVeinMiningUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
      if (Math.abs(shift) % 2 == 1) {
         boolean newState = !this.isExtended();
         this.extendedMode.set(newState);
         if (displayChangeMessage) {
            player.m_213846_(
               MekanismUtils.logFormat(MekanismLang.MODULE_MODE_CHANGE.translate(new Object[]{MekanismLang.MODULE_EXTENDED_MODE, EnumColor.INDIGO, newState}))
            );
         }
      }
   }

   public boolean isExtended() {
      return this.extendedMode.get();
   }

   public int getExcavationRange() {
      return this.excavationRange.get().getRange();
   }

   public static boolean canVeinBlock(BlockState state) {
      return !(state.m_60734_() instanceof BlockBounding);
   }

   public static Object2IntMap<BlockPos> findPositions(
      Level world, Map<BlockPos, BlockState> initial, int extendedRange, Reference2BooleanMap<Block> oreTracker
   ) {
      Object2IntMap<BlockPos> found = new Object2IntLinkedOpenHashMap();
      int maxVein = MekanismConfig.gear.disassemblerMiningCount.get();
      int maxCount = initial.size() + maxVein * oreTracker.size();
      Map<BlockPos, BlockState> frontier = new LinkedHashMap<>(initial);

      for (ModuleVeinMiningUnit.TraversalDistance dist = new ModuleVeinMiningUnit.TraversalDistance(frontier.size());
         !frontier.isEmpty();
         dist.updateDistance(found.size(), frontier.size())
      ) {
         Iterator<Entry<BlockPos, BlockState>> iterator = frontier.entrySet().iterator();
         Entry<BlockPos, BlockState> blockEntry = iterator.next();
         iterator.remove();
         BlockPos blockPos = blockEntry.getKey();
         found.put(blockPos, dist.getDistance());
         if (found.size() >= maxCount) {
            break;
         }

         Block block = blockEntry.getValue().m_60734_();
         boolean isOre = oreTracker.getBoolean(block);
         if (isOre || extendedRange > dist.getDistance()) {
            for (BlockPos nextPos : BlockPos.m_121940_(blockPos.m_7918_(-1, -1, -1), blockPos.m_7918_(1, 1, 1))) {
               if (!found.containsKey(nextPos) && !frontier.containsKey(nextPos)) {
                  Optional<BlockState> nextState = WorldUtils.getBlockState(world, nextPos);
                  if (nextState.isPresent() && nextState.get().m_60713_(block)) {
                     frontier.put(nextPos.m_7949_(), nextState.get());
                     Mekanism.packetHandler()
                        .sendToAllTracking(
                           new PacketLightningRender(
                              PacketLightningRender.LightningPreset.TOOL_AOE,
                              Objects.hash(blockPos, nextPos),
                              Vec3.m_82512_(blockPos),
                              Vec3.m_82512_(nextPos),
                              10
                           ),
                           world,
                           blockPos
                        );
                  }
               }
            }
         }
      }

      return found;
   }

   @Override
   public void addHUDStrings(IModule<ModuleVeinMiningUnit> module, Player player, Consumer<Component> hudStringAdder) {
      if (module.isEnabled() && MekanismConfig.gear.mekaToolExtendedMining.get()) {
         hudStringAdder.accept(
            MekanismLang.MODULE_EXTENDED_ENABLED
               .translateColored(
                  EnumColor.DARK_GRAY,
                  new Object[]{
                     this.isExtended() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED,
                     this.isExtended() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER
                  }
               )
         );
      }
   }

   @NothingNullByDefault
   public static enum ExcavationRange implements IHasTextComponent {
      OFF(0),
      LOW(2),
      MED(4),
      HIGH(6),
      EXTREME(8);

      private final int range;
      private final Component label;

      private ExcavationRange(int range) {
         this.range = range;
         this.label = TextComponentUtil.getString(Integer.toString(range));
      }

      @Override
      public Component getTextComponent() {
         return this.label;
      }

      public int getRange() {
         return this.range;
      }
   }

   private static class TraversalDistance {
      private int distance = 0;
      private int next;

      public TraversalDistance(int next) {
         this.next = next;
      }

      public void updateDistance(int found, int frontierSize) {
         if (found == this.next) {
            this.distance++;
            this.next += frontierSize;
         }
      }

      public int getDistance() {
         return this.distance;
      }
   }
}

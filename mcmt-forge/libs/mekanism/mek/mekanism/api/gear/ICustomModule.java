package mekanism.api.gear;

import java.util.Objects;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import net.minecraft.core.BlockSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ICustomModule<MODULE extends ICustomModule<MODULE>> {
   default void init(IModule<MODULE> module, ModuleConfigItemCreator configItemCreator) {
   }

   default void tickServer(IModule<MODULE> module, Player player) {
   }

   default void tickClient(IModule<MODULE> module, Player player) {
   }

   default void addHUDStrings(IModule<MODULE> module, Player player, Consumer<Component> hudStringAdder) {
   }

   default void addHUDElements(IModule<MODULE> module, Player player, Consumer<IHUDElement> hudElementAdder) {
   }

   default boolean canChangeModeWhenDisabled(IModule<MODULE> module) {
      return false;
   }

   default boolean canChangeRadialModeWhenDisabled(IModule<MODULE> module) {
      return false;
   }

   @Nullable
   default Component getModeScrollComponent(IModule<MODULE> module, ItemStack stack) {
      return null;
   }

   default void changeMode(IModule<MODULE> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
   }

   default void addRadialModes(IModule<MODULE> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
   }

   @Nullable
   default <MODE extends IRadialMode> MODE getMode(IModule<MODULE> module, ItemStack stack, RadialData<MODE> radialData) {
      return null;
   }

   default <MODE extends IRadialMode> boolean setMode(IModule<MODULE> module, Player player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
      return false;
   }

   default void onAdded(IModule<MODULE> module, boolean first) {
   }

   default void onRemoved(IModule<MODULE> module, boolean last) {
   }

   default void onEnabledStateChange(IModule<MODULE> module) {
   }

   @Nullable
   default ICustomModule.ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
      return null;
   }

   default InteractionResult onItemUse(IModule<MODULE> module, UseOnContext context) {
      return InteractionResult.PASS;
   }

   default boolean canPerformAction(IModule<MODULE> module, ToolAction action) {
      return false;
   }

   default InteractionResult onInteract(IModule<MODULE> module, Player player, LivingEntity entity, InteractionHand hand) {
      return InteractionResult.PASS;
   }

   default ICustomModule.ModuleDispenseResult onDispense(IModule<MODULE> module, BlockSource source) {
      return ICustomModule.ModuleDispenseResult.DEFAULT;
   }

   public record ModuleDamageAbsorbInfo(@NotNull FloatSupplier absorptionRatio, @NotNull FloatingLongSupplier energyCost) {
      public ModuleDamageAbsorbInfo(@NotNull FloatSupplier absorptionRatio, @NotNull FloatingLongSupplier energyCost) {
         Objects.requireNonNull(absorptionRatio, "Absorption ratio supplier cannot be null");
         Objects.requireNonNull(energyCost, "Energy cost supplier cannot be null");
         this.absorptionRatio = absorptionRatio;
         this.energyCost = energyCost;
      }
   }

   public static enum ModuleDispenseResult {
      HANDLED,
      DEFAULT,
      FAIL_PREVENT_DROP;
   }
}

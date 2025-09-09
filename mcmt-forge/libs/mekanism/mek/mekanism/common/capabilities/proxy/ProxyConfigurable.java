package mekanism.common.capabilities.proxy;

import mekanism.api.IConfigurable;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyConfigurable extends ProxyHandler implements IConfigurable {
   private final ProxyConfigurable.ISidedConfigurable configurable;

   public ProxyConfigurable(ProxyConfigurable.ISidedConfigurable configurable, @Nullable Direction side) {
      super(side, null);
      this.configurable = configurable;
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      return !this.readOnly && this.side != null ? this.configurable.onSneakRightClick(player, this.side) : InteractionResult.PASS;
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      return !this.readOnly && this.side != null ? this.configurable.onRightClick(player, this.side) : InteractionResult.PASS;
   }

   public interface ISidedConfigurable extends IConfigurable {
      InteractionResult onSneakRightClick(Player player, Direction side);

      @Override
      default InteractionResult onSneakRightClick(Player player) {
         return InteractionResult.PASS;
      }

      InteractionResult onRightClick(Player player, Direction side);

      @Override
      default InteractionResult onRightClick(Player player) {
         return InteractionResult.PASS;
      }
   }
}

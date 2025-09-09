package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.lang.ref.WeakReference;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class MekanismPeripheral<TILE extends BlockEntity & IComputerTile> extends CCMethodCaller implements IDynamicPeripheral {
   private final String name;
   private final WeakReference<TILE> tile;

   public static <TILE extends BlockEntity & IComputerTile> MekanismPeripheral<TILE> create(TILE tile) {
      MekanismPeripheral<TILE> mekanismPeripheral = new MekanismPeripheral<>(tile);
      tile.getComputerMethods(mekanismPeripheral);
      return mekanismPeripheral;
   }

   private MekanismPeripheral(TILE tile) {
      this.tile = new WeakReference<>(tile);
      this.name = tile.getComputerName();
   }

   public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments) throws LuaException {
      return this.callMethod(context, method, arguments);
   }

   public String getType() {
      return this.name;
   }

   @Nullable
   public Object getTarget() {
      return this.tile.get();
   }

   public boolean equals(@Nullable IPeripheral other) {
      Object target = this.getTarget();
      return other instanceof MekanismPeripheral<?> otherP && target != null && target == other.getTarget() && this.methods.equals(otherP.methods);
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MekanismPeripheral<?> other && this.equals((IPeripheral)other);
   }

   @Override
   public int hashCode() {
      int result = this.name.hashCode();
      TILE tileRef = this.tile.get();
      result = 31 * result + (tileRef != null ? tileRef.hashCode() : 0);
      return 31 & result + this.methods.hashCode();
   }
}

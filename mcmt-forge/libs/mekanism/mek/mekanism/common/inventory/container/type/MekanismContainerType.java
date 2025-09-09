package mekanism.common.inventory.container.type;

import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.IContainerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismContainerType<T, CONTAINER extends AbstractContainerMenu>
   extends BaseMekanismContainerType<T, CONTAINER, MekanismContainerType.IMekanismContainerFactory<T, CONTAINER>> {
   public static <TILE extends TileEntityMekanism, CONTAINER extends AbstractContainerMenu> MekanismContainerType<TILE, CONTAINER> tile(
      Class<TILE> type, MekanismContainerType.IMekanismContainerFactory<TILE, CONTAINER> constructor
   ) {
      return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getTileFromBuf(buf, type)));
   }

   public static <TILE extends TileEntityMekanism, CONTAINER extends AbstractContainerMenu> MekanismContainerType<TILE, CONTAINER> tile(
      Class<TILE> type, MekanismContainerType.IMekanismSidedContainerFactory<TILE, CONTAINER> constructor
   ) {
      return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getTileFromBuf(buf, type), true));
   }

   public static <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> MekanismContainerType<ENTITY, CONTAINER> entity(
      Class<ENTITY> type, MekanismContainerType.IMekanismContainerFactory<ENTITY, CONTAINER> constructor
   ) {
      return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getEntityFromBuf(buf, type)));
   }

   public static <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> MekanismContainerType<ENTITY, CONTAINER> entity(
      Class<ENTITY> type, MekanismContainerType.IMekanismSidedContainerFactory<ENTITY, CONTAINER> constructor
   ) {
      return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getEntityFromBuf(buf, type), true));
   }

   protected MekanismContainerType(
      Class<T> type, MekanismContainerType.IMekanismContainerFactory<T, CONTAINER> mekanismConstructor, IContainerFactory<CONTAINER> constructor
   ) {
      super(type, mekanismConstructor, constructor);
   }

   @Nullable
   public CONTAINER create(int id, Inventory inv, Object data) {
      return this.type.isInstance(data) ? this.mekanismConstructor.create(id, inv, this.type.cast(data)) : null;
   }

   @Nullable
   public MenuConstructor create(Object data) {
      if (this.type.isInstance(data)) {
         T d = this.type.cast(data);
         return (id, inv, player) -> this.mekanismConstructor.create(id, inv, d);
      } else {
         return null;
      }
   }

   @NotNull
   private static <TILE extends BlockEntity> TILE getTileFromBuf(FriendlyByteBuf buf, Class<TILE> type) {
      if (buf == null) {
         throw new IllegalArgumentException("Null packet buffer");
      } else if (!FMLEnvironment.dist.isClient()) {
         throw new UnsupportedOperationException("This method is only supported on the client.");
      } else {
         BlockPos pos = buf.m_130135_();
         return WorldUtils.getTileEntity(type, Minecraft.m_91087_().f_91073_, pos);
      }
   }

   @NotNull
   private static <ENTITY extends Entity> ENTITY getEntityFromBuf(FriendlyByteBuf buf, Class<ENTITY> type) {
      if (buf == null) {
         throw new IllegalArgumentException("Null packet buffer");
      } else if (!FMLEnvironment.dist.isClient()) {
         throw new UnsupportedOperationException("This method is only supported on the client.");
      } else if (Minecraft.m_91087_().f_91073_ == null) {
         throw new IllegalStateException("Client world is null.");
      } else {
         int entityId = buf.m_130242_();
         Entity e = Minecraft.m_91087_().f_91073_.m_6815_(entityId);
         if (type.isInstance(e)) {
            return (ENTITY)e;
         } else {
            throw new IllegalStateException(
               "Client could not locate entity (id: "
                  + entityId
                  + ")  for entity container or the entity was of an invalid type. This is likely caused by a mod breaking client side entity lookup."
            );
         }
      }
   }

   @FunctionalInterface
   public interface IMekanismContainerFactory<T, CONTAINER extends AbstractContainerMenu> {
      CONTAINER create(int id, Inventory inv, T data);
   }

   @FunctionalInterface
   public interface IMekanismSidedContainerFactory<T, CONTAINER extends AbstractContainerMenu>
      extends MekanismContainerType.IMekanismContainerFactory<T, CONTAINER> {
      CONTAINER create(int id, Inventory inv, T data, boolean remote);

      @Override
      default CONTAINER create(int id, Inventory inv, T data) {
         return this.create(id, inv, data, false);
      }
   }
}

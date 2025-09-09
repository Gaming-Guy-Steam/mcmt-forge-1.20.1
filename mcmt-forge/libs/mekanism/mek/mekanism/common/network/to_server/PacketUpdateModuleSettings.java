package mekanism.common.network.to_server;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.gear.config.ModuleIntegerData;
import mekanism.api.math.MathUtils;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketUpdateModuleSettings implements IMekanismPacket {
   private final ModuleData<?> moduleType;
   private final int slotId;
   private final int dataIndex;
   private final PacketUpdateModuleSettings.ModuleDataType dataType;
   private final Object value;

   public static PacketUpdateModuleSettings create(int slotId, ModuleData<?> moduleType, int dataIndex, ModuleConfigData<?> configData) {
      if (configData instanceof ModuleEnumData<?> enumData) {
         return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, PacketUpdateModuleSettings.ModuleDataType.ENUM, enumData.get().ordinal());
      } else {
         for (PacketUpdateModuleSettings.ModuleDataType type : PacketUpdateModuleSettings.ModuleDataType.VALUES) {
            if (type.typeMatches(configData)) {
               return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, type, configData.get());
            }
         }

         throw new IllegalArgumentException("Unknown config data type.");
      }
   }

   private PacketUpdateModuleSettings(int slotId, ModuleData<?> moduleType, int dataIndex, PacketUpdateModuleSettings.ModuleDataType dataType, Object value) {
      this.slotId = slotId;
      this.moduleType = moduleType;
      this.dataIndex = dataIndex;
      this.dataType = dataType;
      this.value = value;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null && this.dataIndex >= 0 && this.value != null) {
         ItemStack stack = player.m_150109_().m_8020_(this.slotId);
         if (!stack.m_41619_() && stack.m_41720_() instanceof IModuleContainerItem) {
            Module<?> module = ModuleHelper.get().load(stack, this.moduleType);
            if (module != null) {
               List<ModuleConfigItem<?>> configItems = module.getConfigItems();
               if (this.dataIndex < configItems.size()) {
                  this.setValue(configItems.get(this.dataIndex));
               }
            }
         }
      }
   }

   private <TYPE> void setValue(ModuleConfigItem<TYPE> moduleConfigItem) {
      ModuleConfigData<TYPE> configData = moduleConfigItem.getData();
      if (configData instanceof ModuleEnumData && this.dataType == PacketUpdateModuleSettings.ModuleDataType.ENUM) {
         moduleConfigItem.set(MathUtils.getByIndexMod(((ModuleEnumData)configData).getEnums(), (Integer)this.value));
      } else if (this.dataType.typeMatches(configData)) {
         moduleConfigItem.set((TYPE)this.value);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130130_(this.slotId);
      buffer.writeRegistryId(MekanismAPI.moduleRegistry(), this.moduleType);
      buffer.m_130130_(this.dataIndex);
      buffer.m_130068_(this.dataType);
      switch (this.dataType) {
         case BOOLEAN:
            buffer.writeBoolean((Boolean)this.value);
            break;
         case COLOR:
            buffer.writeInt((Integer)this.value);
            break;
         case INTEGER:
         case ENUM:
            buffer.m_130130_((Integer)this.value);
      }
   }

   public static PacketUpdateModuleSettings decode(FriendlyByteBuf buffer) {
      int slotId = buffer.m_130242_();
      ModuleData<?> moduleType = (ModuleData<?>)buffer.readRegistryIdSafe(ModuleData.class);
      int dataIndex = buffer.m_130242_();
      PacketUpdateModuleSettings.ModuleDataType dataType = (PacketUpdateModuleSettings.ModuleDataType)buffer.m_130066_(
         PacketUpdateModuleSettings.ModuleDataType.class
      );

      Object data = switch (dataType) {
         case BOOLEAN -> buffer.readBoolean();
         case COLOR -> buffer.readInt();
         case INTEGER, ENUM -> buffer.m_130242_();
      };
      return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, dataType, data);
   }

   private static enum ModuleDataType {
      BOOLEAN(data -> data instanceof ModuleBooleanData),
      COLOR(data -> data instanceof ModuleColorData),
      INTEGER(data -> data instanceof ModuleIntegerData),
      ENUM(data -> data instanceof ModuleEnumData);

      private static final PacketUpdateModuleSettings.ModuleDataType[] VALUES = values();
      private final Predicate<ModuleConfigData<?>> configDataPredicate;

      private ModuleDataType(Predicate<ModuleConfigData<?>> configDataPredicate) {
         this.configDataPredicate = configDataPredicate;
      }

      public boolean typeMatches(ModuleConfigData<?> data) {
         return this.configDataPredicate.test(data);
      }
   }
}

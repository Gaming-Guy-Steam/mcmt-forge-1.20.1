package mekanism.common.recipe.upgrade;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.recipe.upgrade.chemical.ChemicalRecipeData;
import mekanism.common.recipe.upgrade.chemical.GasRecipeData;
import mekanism.common.recipe.upgrade.chemical.InfusionRecipeData;
import mekanism.common.recipe.upgrade.chemical.PigmentRecipeData;
import mekanism.common.recipe.upgrade.chemical.SlurryRecipeData;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public interface RecipeUpgradeData<TYPE extends RecipeUpgradeData<TYPE>> {
   @Nullable
   TYPE merge(TYPE other);

   boolean applyToStack(ItemStack stack);

   @NotNull
   static Set<RecipeUpgradeType> getSupportedTypes(ItemStack stack) {
      if (stack.m_41619_()) {
         return Collections.emptySet();
      } else {
         Set<RecipeUpgradeType> supportedTypes = EnumSet.noneOf(RecipeUpgradeType.class);
         Item item = stack.m_41720_();
         TileEntityMekanism tile = null;
         if (item instanceof BlockItem blockItem) {
            Block block = blockItem.m_40614_();
            if (block instanceof IHasTileEntity<?> hasTileEntity) {
               BlockEntity tileEntity = hasTileEntity.createDummyBlockEntity();
               if (tileEntity instanceof TileEntityMekanism) {
                  tile = (TileEntityMekanism)tileEntity;
               }
            }

            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
               supportedTypes.add(RecipeUpgradeType.UPGRADE);
            }
         }

         if (stack.getCapability(Capabilities.STRICT_ENERGY).isPresent() || tile != null && tile.handles(SubstanceType.ENERGY)) {
            supportedTypes.add(RecipeUpgradeType.ENERGY);
         }

         if (FluidUtil.getFluidHandler(stack).isPresent() || tile != null && tile.handles(SubstanceType.FLUID)) {
            supportedTypes.add(RecipeUpgradeType.FLUID);
         }

         if (stack.getCapability(Capabilities.GAS_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.GAS)) {
            supportedTypes.add(RecipeUpgradeType.GAS);
         }

         if (stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.INFUSION)) {
            supportedTypes.add(RecipeUpgradeType.INFUSION);
         }

         if (stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.PIGMENT)) {
            supportedTypes.add(RecipeUpgradeType.PIGMENT);
         }

         if (stack.getCapability(Capabilities.SLURRY_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.SLURRY)) {
            supportedTypes.add(RecipeUpgradeType.SLURRY);
         }

         if (item instanceof IItemSustainedInventory || tile != null && tile.persistInventory()) {
            supportedTypes.add(RecipeUpgradeType.ITEM);
         }

         if (stack.getCapability(Capabilities.OWNER_OBJECT).isPresent() || tile != null && tile.hasSecurity()) {
            supportedTypes.add(RecipeUpgradeType.SECURITY);
         }

         if (item instanceof ItemBlockBin bin && bin.getTier() != BinTier.CREATIVE) {
            supportedTypes.add(RecipeUpgradeType.LOCK);
         }

         if (tile instanceof TileEntityFactory) {
            supportedTypes.add(RecipeUpgradeType.SORTING);
         }

         if (item instanceof IQIODriveItem) {
            supportedTypes.add(RecipeUpgradeType.QIO_DRIVE);
         }

         return supportedTypes;
      }
   }

   @Nullable
   private static <TYPE extends RecipeUpgradeData<TYPE>> TYPE getContainerUpgradeData(@NotNull ItemStack stack, String key, Function<ListTag, TYPE> creator) {
      ListTag containers = ItemDataUtils.getList(stack, key);
      return containers.isEmpty() ? null : creator.apply(containers);
   }

   @Nullable
   static RecipeUpgradeData<?> getUpgradeData(@NotNull RecipeUpgradeType type, @NotNull ItemStack stack) {
      Item item = stack.m_41720_();

      return (RecipeUpgradeData<?>)(switch (type) {
         case ENERGY -> (EnergyRecipeData)getContainerUpgradeData(stack, "EnergyContainers", EnergyRecipeData::new);
         case FLUID -> (FluidRecipeData)getContainerUpgradeData(stack, "FluidTanks", FluidRecipeData::new);
         case GAS -> (ChemicalRecipeData)getContainerUpgradeData(stack, "GasTanks", GasRecipeData::new);
         case INFUSION -> (ChemicalRecipeData)getContainerUpgradeData(stack, "InfusionTanks", InfusionRecipeData::new);
         case PIGMENT -> (ChemicalRecipeData)getContainerUpgradeData(stack, "PigmentTanks", PigmentRecipeData::new);
         case SLURRY -> (ChemicalRecipeData)getContainerUpgradeData(stack, "SlurryTanks", SlurryRecipeData::new);
         case ITEM -> {
            if (item instanceof IItemSustainedInventory sustainedInventory) {
               ListTag inventory = sustainedInventory.getSustainedInventory(stack);
               yield inventory != null && !inventory.isEmpty() ? new ItemRecipeData(inventory) : null;
            } else if (item instanceof ItemBlockPersonalStorage) {
               yield PersonalStorageManager.getInventoryIfPresent(stack).map(inv -> new ItemRecipeData(inv.getInventorySlots(null))).orElse(null);
            } else {
               if (MekanismAPI.debug) {
                  throw new IllegalStateException("Requested ITEM upgrade data, but unable to handle");
               }

               yield null;
            }
         }
         case LOCK -> {
            BinMekanismInventory inventory = BinMekanismInventory.create(stack);
            yield inventory != null && inventory.getBinSlot().isLocked() ? new LockRecipeData(inventory) : null;
         }
         case SECURITY -> {
            UUID ownerUUID = ISecurityUtils.INSTANCE.getOwnerUUID(stack);
            if (ownerUUID == null) {
               yield null;
            } else {
               SecurityMode securityMode = stack.getCapability(Capabilities.SECURITY_OBJECT).map(ISecurityObject::getSecurityMode).orElse(SecurityMode.PUBLIC);
               yield new SecurityRecipeData(ownerUUID, securityMode);
            }
         }
         case SORTING -> {
            boolean sorting = ItemDataUtils.getBoolean(stack, "sorting");
            yield sorting ? SortingRecipeData.SORTING : null;
         }
         case UPGRADE -> UpgradesRecipeData.tryCreate(ItemDataUtils.getCompound(stack, "componentUpgrade"));
         case QIO_DRIVE -> {
            IQIODriveItem.DriveMetadata data = IQIODriveItem.DriveMetadata.load(stack);
            if (data.count() > 0L && ((IQIODriveItem)item).hasStoredItemMap(stack)) {
               long[] storedItems = ItemDataUtils.getLongArray(stack, "qioItemMap");
               if (storedItems.length % 3 == 0) {
                  yield new QIORecipeData(data, storedItems);
               }
            }

            yield null;
         }
      });
   }

   @Nullable
   static <TYPE extends RecipeUpgradeData<TYPE>> TYPE mergeUpgradeData(List<RecipeUpgradeData<?>> upgradeData) {
      if (upgradeData.isEmpty()) {
         return null;
      } else {
         TYPE data = (TYPE)upgradeData.get(0);

         for (int i = 1; i < upgradeData.size(); i++) {
            data = data.merge((TYPE)upgradeData.get(i));
            if (data == null) {
               return null;
            }
         }

         return data;
      }
   }

   @Nullable
   default TileEntityMekanism getTileFromBlock(Block block) {
      if (block instanceof IHasTileEntity<?> hasTileEntity) {
         BlockEntity tileEntity = hasTileEntity.createDummyBlockEntity();
         if (tileEntity instanceof TileEntityMekanism) {
            return (TileEntityMekanism)tileEntity;
         }
      }

      return null;
   }
}

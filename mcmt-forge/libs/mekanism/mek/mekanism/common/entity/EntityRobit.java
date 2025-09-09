package mekanism.common.entity;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.DataHandlerUtils;
import mekanism.api.MekanismAPI;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.robit.IRobit;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.ai.RobitAIFollow;
import mekanism.common.entity.ai.RobitAIPickup;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemRobit;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityRobit
   extends PathfinderMob
   implements IRobit,
   IMekanismInventory,
   ISustainedInventory,
   IMekanismStrictEnergyHandler,
   ISingleRecipeLookupHandler.ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {
   public static final ModelProperty<ResourceLocation> SKIN_TEXTURE_PROPERTY = new ModelProperty();
   private static final TicketType<Integer> ROBIT_CHUNK_UNLOAD = TicketType.m_9465_("robit_chunk_unload", Integer::compareTo, 20);
   private static final EntityDataAccessor<UUID> OWNER_UUID = define(MekanismDataSerializers.UUID.get());
   private static final EntityDataAccessor<String> OWNER_NAME = define(EntityDataSerializers.f_135030_);
   private static final EntityDataAccessor<SecurityMode> SECURITY = define(MekanismDataSerializers.SECURITY.get());
   private static final EntityDataAccessor<Boolean> FOLLOW = define(EntityDataSerializers.f_135035_);
   private static final EntityDataAccessor<Boolean> DROP_PICKUP = define(EntityDataSerializers.f_135035_);
   private static final EntityDataAccessor<ResourceKey<RobitSkin>> SKIN = define(MekanismDataSerializers.ROBIT_SKIN.get());
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final FloatingLong MAX_ENERGY = FloatingLong.createConst(100000L);
   private static final FloatingLong DISTANCE_MULTIPLIER = FloatingLong.createConst(1.5);
   private static final int ticksRequired = 100;
   private final CapabilityCache capabilityCache = new CapabilityCache();
   public Coord4D homeLocation;
   private int lastTextureUpdate;
   private int textureIndex;
   private int progress;
   private final Set<Player> playersUsing = new ObjectOpenHashSet();
   private final RecipeCacheLookupMonitor<ItemStackToItemStackRecipe> recipeCacheLookupMonitor;
   private final BooleanSupplier recheckAllRecipeErrors;
   private final boolean[] trackedErrors = new boolean[TRACKED_ERROR_TYPES.size()];
   private final IInputHandler<ItemStack> inputHandler;
   private final IOutputHandler<ItemStack> outputHandler;
   @NotNull
   private final List<IInventorySlot> inventorySlots;
   @NotNull
   private final List<IInventorySlot> mainContainerSlots;
   @NotNull
   private final List<IInventorySlot> smeltingContainerSlots;
   @NotNull
   private final List<IInventorySlot> inventoryContainerSlots;
   private final EnergyInventorySlot energySlot;
   private final InputInventorySlot smeltingInputSlot;
   private final OutputInventorySlot smeltingOutputSlot;
   private final List<IEnergyContainer> energyContainers;
   private final BasicEnergyContainer energyContainer;

   public static Builder getDefaultAttributes() {
      return Mob.m_21552_().m_22268_(Attributes.f_22276_, 1.0).m_22268_(Attributes.f_22279_, 0.3F);
   }

   private static <T> EntityDataAccessor<T> define(EntityDataSerializer<T> dataSerializer) {
      return SynchedEntityData.m_135353_(EntityRobit.class, dataSerializer);
   }

   public EntityRobit(EntityType<EntityRobit> type, Level world) {
      super(type, world);
      this.m_21573_().m_7008_(false);
      this.m_20340_(true);
      this.addCapabilityResolver(BasicCapabilityResolver.security(this));
      this.recipeCacheLookupMonitor = new RecipeCacheLookupMonitor<>(this);
      int checkOffset = this.m_9236_().f_46441_.m_188503_(100);
      this.recheckAllRecipeErrors = () -> !this.playersUsing.isEmpty() && this.m_9236_().m_46467_() % 100L == checkOffset;
      this.energyContainers = Collections.singletonList(this.energyContainer = BasicEnergyContainer.input(MAX_ENERGY, this));
      this.inventorySlots = new ArrayList<>();
      this.inventoryContainerSlots = new ArrayList<>();

      for (int slotY = 0; slotY < 3; slotY++) {
         for (int slotX = 0; slotX < 9; slotX++) {
            IInventorySlot slot = BasicInventorySlot.at(this, 8 + slotX * 18, 18 + slotY * 18);
            this.inventorySlots.add(slot);
            this.inventoryContainerSlots.add(slot);
         }
      }

      this.inventorySlots.add(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_9236_, this, 153, 17));
      this.inventorySlots.add(this.smeltingInputSlot = InputInventorySlot.at(this::containsRecipe, this.recipeCacheLookupMonitor, 51, 35));
      this.inventorySlots.add(this.smeltingOutputSlot = OutputInventorySlot.at(this, 116, 35));
      this.smeltingInputSlot
         .tracksWarnings(
            slotx -> slotx.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      this.smeltingOutputSlot
         .tracksWarnings(
            slotx -> slotx.warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE)
            )
         );
      this.mainContainerSlots = Collections.singletonList(this.energySlot);
      this.smeltingContainerSlots = List.of(this.smeltingInputSlot, this.smeltingOutputSlot);
      this.inputHandler = InputHelper.getInputHandler(this.smeltingInputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.smeltingOutputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @Nullable
   public static EntityRobit create(Level world, double x, double y, double z) {
      EntityRobit robit = (EntityRobit)((EntityType)MekanismEntityTypes.ROBIT.get()).m_20615_(world);
      if (robit == null) {
         return null;
      } else {
         robit.m_6034_(x, y, z);
         robit.f_19854_ = x;
         robit.f_19855_ = y;
         robit.f_19856_ = z;
         return robit;
      }
   }

   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(1, new RobitAIPickup(this, 1.0F));
      this.f_21345_.m_25352_(2, new RobitAIFollow(this, 1.0F, 4.0F, 2.0F));
      this.f_21345_.m_25352_(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.f_21345_.m_25352_(3, new RandomLookAroundGoal(this));
      this.f_21345_.m_25352_(4, new FloatGoal(this));
   }

   public boolean m_6785_(double distanceToClosestPlayer) {
      return false;
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(OWNER_UUID, Mekanism.gameProfile.getId());
      this.f_19804_.m_135372_(OWNER_NAME, "");
      this.f_19804_.m_135372_(SECURITY, SecurityMode.PUBLIC);
      this.f_19804_.m_135372_(FOLLOW, false);
      this.f_19804_.m_135372_(DROP_PICKUP, false);
      this.f_19804_.m_135372_(SKIN, MekanismRobitSkins.BASE);
   }

   private FloatingLong getRoundedTravelEnergy() {
      return DISTANCE_MULTIPLIER.multiply(Math.sqrt(this.m_20275_(this.f_19854_, this.f_19855_, this.f_19856_)));
   }

   public void onRemovedFromWorld() {
      if (this.m_9236_() != null && !this.m_9236_().f_46443_ && this.getFollowing() && this.getOwner() != null) {
         ((ServerLevel)this.m_9236_()).m_7726_().m_8387_(ROBIT_CHUNK_UNLOAD, new ChunkPos(this.m_20183_()), 2, this.m_19879_());
      }

      super.onRemovedFromWorld();
   }

   public void m_6075_() {
      if (!this.m_9236_().f_46443_ && this.getFollowing()) {
         Player owner = this.getOwner();
         if (owner != null && this.m_20280_(owner) > 4.0 && !this.m_21573_().m_26571_() && !this.energyContainer.isEmpty()) {
            this.energyContainer.extract(this.getRoundedTravelEnergy(), Action.EXECUTE, AutomationType.INTERNAL);
         }
      }

      super.m_6075_();
      if (!this.m_9236_().f_46443_) {
         if (this.getDropPickup()) {
            this.collectItems();
         }

         if (this.homeLocation == null) {
            this.m_146870_();
            return;
         }

         if (this.f_19797_ % 20 == 0) {
            Level serverWorld = ServerLifecycleHooks.getCurrentServer().m_129880_(this.homeLocation.dimension);
            BlockPos homePos = this.homeLocation.getPos();
            if (WorldUtils.isBlockLoaded(serverWorld, homePos) && WorldUtils.getTileEntity(TileEntityChargepad.class, serverWorld, homePos) == null) {
               this.drop();
               this.m_146870_();
            }
         }

         if (this.energyContainer.isEmpty() && !this.isOnChargepad()) {
            this.goHome();
         }

         this.energySlot.fillContainerOrConvert();
         this.recipeCacheLookupMonitor.updateAndProcess();
      }
   }

   public boolean isItemValid(ItemEntity item) {
      return item.m_6084_() && !item.m_32063_() && !(item.m_32055_().m_41720_() instanceof ItemRobit);
   }

   private void collectItems() {
      List<ItemEntity> items = this.m_9236_().m_45976_(ItemEntity.class, this.m_20191_().m_82377_(1.5, 1.5, 1.5));
      if (!items.isEmpty()) {
         for (ItemEntity item : items) {
            if (this.isItemValid(item)) {
               for (IInventorySlot slot : this.inventoryContainerSlots) {
                  if (slot.isEmpty()) {
                     slot.setStack(item.m_32055_());
                     this.m_7938_(item, item.m_32055_().m_41613_());
                     item.m_146870_();
                     this.m_5496_(SoundEvents.f_12019_, 1.0F, ((this.f_19796_.m_188501_() - this.f_19796_.m_188501_()) * 0.7F + 1.0F) * 2.0F);
                     break;
                  }

                  ItemStack itemStack = slot.getStack();
                  int maxSize = slot.getLimit(itemStack);
                  if (ItemHandlerHelper.canItemStacksStack(itemStack, item.m_32055_()) && itemStack.m_41613_() < maxSize) {
                     int needed = maxSize - itemStack.m_41613_();
                     int toAdd = Math.min(needed, item.m_32055_().m_41613_());
                     MekanismUtils.logMismatchedStackSize(slot.growStack(toAdd, Action.EXECUTE), toAdd);
                     item.m_32055_().m_41774_(toAdd);
                     this.m_7938_(item, toAdd);
                     if (item.m_32055_().m_41619_()) {
                        item.m_146870_();
                     }

                     this.m_5496_(SoundEvents.f_12019_, 1.0F, ((this.f_19796_.m_188501_() - this.f_19796_.m_188501_()) * 0.7F + 1.0F) * 2.0F);
                     break;
                  }
               }
            }
         }
      }
   }

   public void goHome() {
      if (!this.m_9236_().m_5776_()) {
         this.setFollowing(false);
         if (this.m_9236_().m_46472_() == this.homeLocation.dimension) {
            this.m_20334_(0.0, 0.0, 0.0);
            this.m_6021_(this.homeLocation.getX() + 0.5, this.homeLocation.getY() + 0.3, this.homeLocation.getZ() + 0.5);
         } else {
            ServerLevel newWorld = ((ServerLevel)this.m_9236_()).m_7654_().m_129880_(this.homeLocation.dimension);
            if (newWorld != null) {
               final Vec3 destination = new Vec3(this.homeLocation.getX() + 0.5, this.homeLocation.getY() + 0.3, this.homeLocation.getZ() + 0.5);
               this.changeDimension(
                  newWorld,
                  new ITeleporter() {
                     public Entity placeEntity(
                        Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity
                     ) {
                        return repositionEntity.apply(false);
                     }

                     public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                        return new PortalInfo(destination, Vec3.f_82478_, entity.m_146908_(), entity.m_146909_());
                     }

                     public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                        return false;
                     }
                  }
               );
            }
         }
      }
   }

   private boolean isOnChargepad() {
      return WorldUtils.getTileEntity(TileEntityChargepad.class, this.m_9236_(), this.m_20183_()) != null;
   }

   @NotNull
   public InteractionResult m_7111_(@NotNull Player player, @NotNull Vec3 vec, @NotNull InteractionHand hand) {
      if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, this)) {
         return InteractionResult.FAIL;
      } else if (player.m_6144_()) {
         ItemStack stack = player.m_21120_(hand);
         if (!stack.m_41619_() && stack.m_41720_() instanceof ItemConfigurator) {
            if (!this.m_9236_().f_46443_) {
               this.drop();
            }

            this.m_146870_();
            player.m_6674_(hand);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      } else {
         if (!this.m_9236_().f_46443_) {
            MenuProvider provider = MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, this);
            if (provider != null) {
               this.m_146852_(GameEvent.f_223708_, player);
               NetworkHooks.openScreen((ServerPlayer)player, provider, buf -> buf.m_130130_(this.m_19879_()));
            }
         }

         return InteractionResult.m_19078_(this.m_9236_().f_46443_);
      }
   }

   private ItemStack getItemVariant() {
      ItemStack stack = MekanismItems.ROBIT.getItemStack();
      Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY).resolve();
      if (capability.isPresent()) {
         IStrictEnergyHandler energyHandlerItem = capability.get();
         if (energyHandlerItem.getEnergyContainerCount() > 0) {
            energyHandlerItem.setEnergy(0, this.energyContainer.getEnergy());
         }
      }

      ItemRobit item = (ItemRobit)stack.m_41720_();
      item.setSustainedInventory(this.getSustainedInventory(), stack);
      item.setName(stack, this.m_7755_());
      stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
         security.setOwnerUUID(this.getOwnerUUID());
         security.setSecurityMode(this.getSecurityMode());
      });
      item.setSkin(stack, this.getSkin());
      return stack;
   }

   public void drop() {
      ItemEntity entityItem = new ItemEntity(this.m_9236_(), this.m_20185_(), this.m_20186_() + 0.3, this.m_20189_(), this.getItemVariant());
      entityItem.m_20334_(0.0, this.f_19796_.m_188583_() * 0.05F + 0.2F, 0.0);
      this.m_9236_().m_7967_(entityItem);
   }

   public double getScaledProgress() {
      return this.getOperatingTicks() / 100.0;
   }

   public int getOperatingTicks() {
      return this.progress;
   }

   @Override
   public int getSavedOperatingTicks(int cacheIndex) {
      return this.getOperatingTicks();
   }

   public void m_7380_(@NotNull CompoundTag nbtTags) {
      super.m_7380_(nbtTags);
      nbtTags.m_128362_("owner", this.getOwnerUUID());
      NBTUtils.writeEnum(nbtTags, "securityMode", this.getSecurityMode());
      nbtTags.m_128379_("follow", this.getFollowing());
      nbtTags.m_128379_("dropPickup", this.getDropPickup());
      if (this.homeLocation != null) {
         this.homeLocation.write(nbtTags);
      }

      nbtTags.m_128365_("Items", DataHandlerUtils.writeContainers(this.getInventorySlots(null)));
      nbtTags.m_128365_("EnergyContainers", DataHandlerUtils.writeContainers(this.getEnergyContainers(null)));
      nbtTags.m_128405_("progress", this.getOperatingTicks());
      NBTUtils.writeResourceKey(nbtTags, "skin", this.getSkin());
   }

   public void m_7378_(@NotNull CompoundTag nbtTags) {
      super.m_7378_(nbtTags);
      NBTUtils.setUUIDIfPresent(nbtTags, "owner", this::setOwnerUUID);
      NBTUtils.setEnumIfPresent(nbtTags, "securityMode", SecurityMode::byIndexStatic, this::setSecurityMode);
      this.setFollowing(nbtTags.m_128471_("follow"));
      this.setDropPickup(nbtTags.m_128471_("dropPickup"));
      this.homeLocation = Coord4D.read(nbtTags);
      DataHandlerUtils.readContainers(this.getInventorySlots(null), nbtTags.m_128437_("Items", 10));
      DataHandlerUtils.readContainers(this.getEnergyContainers(null), nbtTags.m_128437_("EnergyContainers", 10));
      this.progress = nbtTags.m_128451_("progress");
      NBTUtils.setResourceKeyIfPresentElse(
         nbtTags, "skin", MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, skin -> this.setSkin(skin, null), () -> this.setSkin(MekanismRobitSkins.BASE, null)
      );
   }

   public boolean m_6673_(@NotNull DamageSource source) {
      return source.m_276093_(MekanismDamageTypes.RADIATION.key()) || super.m_6673_(source);
   }

   protected void m_6475_(@NotNull DamageSource damageSource, float amount) {
      amount = ForgeHooks.onLivingHurt(this, damageSource, amount);
      if (!(amount <= 0.0F)) {
         amount = this.m_21161_(damageSource, amount);
         amount = this.m_6515_(damageSource, amount);
         if (damageSource.m_269533_(DamageTypeTags.f_268549_)) {
            amount /= 2.0F;
         }

         this.energyContainer.extract(FloatingLong.create((double)(1000.0F * amount)), Action.EXECUTE, AutomationType.INTERNAL);
         this.m_21231_().m_289194_(damageSource, amount);
      }
   }

   protected void m_6153_() {
   }

   public void setHome(Coord4D home) {
      this.homeLocation = home;
   }

   public boolean m_6094_() {
      return !this.energyContainer.isEmpty();
   }

   public Player getOwner() {
      return this.m_9236_().m_46003_(this.getOwnerUUID());
   }

   @NotNull
   @Override
   public String getOwnerName() {
      return (String)this.f_19804_.m_135370_(OWNER_NAME);
   }

   @NotNull
   @Override
   public UUID getOwnerUUID() {
      return (UUID)this.f_19804_.m_135370_(OWNER_UUID);
   }

   @NotNull
   @Override
   public SecurityMode getSecurityMode() {
      return (SecurityMode)this.f_19804_.m_135370_(SECURITY);
   }

   @Override
   public void setSecurityMode(@NotNull SecurityMode mode) {
      SecurityMode current = this.getSecurityMode();
      if (current != mode) {
         this.f_19804_.m_135381_(SECURITY, mode);
         this.onSecurityChanged(current, mode);
      }
   }

   @Override
   public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
      if (!this.m_9236_().f_46443_) {
         SecurityUtils.get().securityChanged(this.playersUsing, this, old, mode);
      }
   }

   public void open(Player player) {
      this.playersUsing.add(player);
   }

   public void close(Player player) {
      this.playersUsing.remove(player);
   }

   @Override
   public void setOwnerUUID(UUID uuid) {
      this.f_19804_.m_135381_(OWNER_UUID, uuid);
      this.f_19804_.m_135381_(OWNER_NAME, MekanismUtils.getLastKnownUsername(uuid));
   }

   public boolean getFollowing() {
      return (Boolean)this.f_19804_.m_135370_(FOLLOW);
   }

   public void setFollowing(boolean follow) {
      this.f_19804_.m_135381_(FOLLOW, follow);
   }

   public boolean getDropPickup() {
      return (Boolean)this.f_19804_.m_135370_(DROP_PICKUP);
   }

   public void setDropPickup(boolean pickup) {
      this.f_19804_.m_135381_(DROP_PICKUP, pickup);
   }

   @Override
   public void setSustainedInventory(ListTag nbtTags) {
      if (nbtTags != null && !nbtTags.isEmpty()) {
         DataHandlerUtils.readContainers(this.getInventorySlots(null), nbtTags);
      }
   }

   @Override
   public ListTag getSustainedInventory() {
      return DataHandlerUtils.writeContainers(this.getInventorySlots(null));
   }

   @NotNull
   @Override
   public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
      return this.hasInventory() ? this.inventorySlots : Collections.emptyList();
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.canHandleEnergy() ? this.energyContainers : Collections.emptyList();
   }

   @Override
   public void onContentsChanged() {
   }

   @NotNull
   public List<IInventorySlot> getContainerInventorySlots(@NotNull MenuType<?> containerType) {
      if (!this.hasInventory()) {
         return Collections.emptyList();
      } else if (containerType == MekanismContainerTypes.INVENTORY_ROBIT.get()) {
         return this.inventoryContainerSlots;
      } else if (containerType == MekanismContainerTypes.MAIN_ROBIT.get()) {
         return this.mainContainerSlots;
      } else {
         return containerType == MekanismContainerTypes.SMELTING_ROBIT.get() ? this.smeltingContainerSlots : Collections.emptyList();
      }
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
      return MekanismRecipeType.SMELTING;
   }

   @Nullable
   public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   public IEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }

   public ItemStack getPickedResult(HitResult target) {
      return this.getItemVariant();
   }

   @Override
   public void clearRecipeErrors(int cacheIndex) {
      Arrays.fill(this.trackedErrors, false);
   }

   @NotNull
   public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackToItemStackRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.itemToItem(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(errors -> {
            for (int i = 0; i < this.trackedErrors.length; i++) {
               this.trackedErrors[i] = errors.contains(TRACKED_ERROR_TYPES.get(i));
            }
         })
         .setEnergyRequirements(MekanismConfig.usage.energizedSmelter, this.energyContainer)
         .setRequiredTicks(() -> 100)
         .setOnFinish(this::onContentsChanged)
         .setOperatingTicksChanged(operatingTicks -> this.progress = operatingTicks);
   }

   public BooleanSupplier getWarningCheck(CachedRecipe.OperationTracker.RecipeError error) {
      int errorIndex = TRACKED_ERROR_TYPES.indexOf(error);
      return errorIndex == -1 ? () -> false : () -> this.trackedErrors[errorIndex];
   }

   public void addContainerTrackers(MekanismContainer container) {
      MenuType<?> containerType = container.m_6772_();
      if (containerType == MekanismContainerTypes.MAIN_ROBIT.get()) {
         container.track(SyncableFloatingLong.create(this.energyContainer::getEnergy, this.energyContainer::setEnergy));
      } else if (containerType == MekanismContainerTypes.SMELTING_ROBIT.get()) {
         container.track(SyncableInt.create(() -> this.progress, value -> this.progress = value));
         container.trackArray(this.trackedErrors);
      }
   }

   public ContainerLevelAccess getWorldPosCallable() {
      return new ContainerLevelAccess() {
         @NotNull
         public <T> Optional<T> m_6721_(@NotNull BiFunction<Level, BlockPos, T> worldBlockPosTBiFunction) {
            return Optional.ofNullable(worldBlockPosTBiFunction.apply(EntityRobit.this.m_9236_(), EntityRobit.this.m_20183_()));
         }
      };
   }

   @NotNull
   @Override
   public ResourceKey<RobitSkin> getSkin() {
      return (ResourceKey<RobitSkin>)this.f_19804_.m_135370_(SKIN);
   }

   @Override
   public boolean setSkin(@NotNull ResourceKey<RobitSkin> skinKey, @Nullable Player player) {
      Objects.requireNonNull(skinKey, "Robit skin cannot be null.");
      if (this.getSkin() == skinKey) {
         return true;
      } else {
         if (player != null) {
            if (!ISecurityUtils.INSTANCE.canAccess(player, this)) {
               return false;
            }

            MekanismRobitSkins.SkinLookup skinLookup = MekanismRobitSkins.lookup(this.m_9236_().m_9598_(), skinKey);
            skinKey = skinLookup.name();
            if (this.getSkin() == skinKey) {
               return true;
            }

            if (!skinLookup.skin().isUnlocked(player)) {
               return false;
            }

            if (player instanceof ServerPlayer serverPlayer) {
               MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.trigger(serverPlayer, skinKey);
            }
         }

         this.f_19804_.m_135381_(SKIN, skinKey);
         return true;
      }
   }

   public ModelData getModelData() {
      return ModelData.builder().with(SKIN_TEXTURE_PROPERTY, this.getModelTexture()).build();
   }

   private ResourceLocation getModelTexture() {
      Registry<RobitSkin> robitSkins = this.m_9236_().m_9598_().m_175515_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
      ResourceKey<RobitSkin> skinKey = this.getSkin();
      RobitSkin skin = (RobitSkin)robitSkins.m_6246_(skinKey);
      if (skin == null) {
         Mekanism.logger.error("Unknown Robit Skin: {}; resetting skin to base.", skinKey.m_135782_());
         skinKey = MekanismRobitSkins.BASE;
         this.setSkin(MekanismRobitSkins.BASE, null);
         skin = (RobitSkin)robitSkins.m_123013_(skinKey);
      }

      List<ResourceLocation> textures = skin.textures();
      if (textures.isEmpty()) {
         this.textureIndex = 0;
         if (skinKey != MekanismRobitSkins.BASE) {
            Mekanism.logger.error("Robit Skin: {}, has no textures; resetting skin to base.", skinKey.m_135782_());
            skinKey = MekanismRobitSkins.BASE;
            this.setSkin(MekanismRobitSkins.BASE, null);
            skin = (RobitSkin)robitSkins.m_123013_(skinKey);
         }

         if (skin.textures().isEmpty()) {
            throw new IllegalStateException("Base robit skin has no textures defined.");
         } else {
            return this.getModelTexture();
         }
      } else {
         int textureCount = textures.size();
         if (textureCount == 1) {
            this.textureIndex = 0;
         } else {
            if (this.lastTextureUpdate < this.f_19797_) {
               this.lastTextureUpdate = this.f_19797_;
               if (Math.abs(this.m_20185_() - this.f_19854_) + Math.abs(this.m_20189_() - this.f_19856_) > 0.001 && this.f_19797_ % 3 == 0) {
                  this.textureIndex++;
               }
            }

            if (this.textureIndex >= textureCount) {
               this.textureIndex %= textureCount;
            }
         }

         return textures.get(this.textureIndex);
      }
   }

   protected final void addCapabilityResolver(ICapabilityResolver resolver) {
      this.capabilityCache.addCapabilityResolver(resolver);
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
      if (this.capabilityCache != null) {
         if (this.capabilityCache.isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
         }

         if (this.capabilityCache.canResolve(capability)) {
            return this.capabilityCache.getCapabilityUnchecked(capability, side);
         }
      }

      return super.getCapability(capability, side);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.capabilityCache.invalidateAll();
   }
}

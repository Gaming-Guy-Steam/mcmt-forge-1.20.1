package mekanism.common.lib.radiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.collection.HashList;
import mekanism.common.network.to_client.PacketRadiationData;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RadiationManager implements IRadiationManager {
   private static final String DATA_HANDLER_NAME = "radiation_manager";
   private static final IntSupplier MAX_RANGE = () -> MekanismConfig.general.radiationChunkCheckRadius.get() * 16;
   private static final Random RAND = new Random();
   public static final double BASELINE = 1.0E-7;
   public static final double MIN_MAGNITUDE = 1.0E-5;
   private boolean loaded;
   private final Table<Chunk3D, Coord4D, RadiationSource> radiationTable = HashBasedTable.create();
   private final Table<Chunk3D, Coord4D, IRadiationSource> radiationView = Tables.unmodifiableTable(this.radiationTable);
   private final Map<ResourceLocation, List<Meltdown>> meltdowns = new Object2ObjectOpenHashMap();
   private final Map<UUID, RadiationManager.PreviousRadiationData> playerEnvironmentalExposureMap = new Object2ObjectOpenHashMap();
   private final Map<UUID, RadiationManager.PreviousRadiationData> playerExposureMap = new Object2ObjectOpenHashMap();
   private RadiationManager.RadiationScale clientRadiationScale = RadiationManager.RadiationScale.NONE;
   private double clientEnvironmentalRadiation = 1.0E-7;
   private double clientMaxMagnitude = 1.0E-7;
   @Nullable
   private RadiationManager.RadiationDataHandler dataHandler;

   public static RadiationManager get() {
      return (RadiationManager)INSTANCE;
   }

   @Override
   public boolean isRadiationEnabled() {
      return MekanismConfig.general.radiationEnabled.getOrDefault();
   }

   private void markDirty() {
      if (this.dataHandler != null) {
         this.dataHandler.m_77762_();
      }
   }

   @Override
   public DamageSource getRadiationDamageSource(RegistryAccess registryAccess) {
      return MekanismDamageTypes.RADIATION.source(registryAccess);
   }

   @Override
   public ResourceKey<DamageType> getRadiationDamageTypeKey() {
      return MekanismDamageTypes.RADIATION.key();
   }

   @Override
   public double getRadiationLevel(Entity entity) {
      return this.getRadiationLevel(new Coord4D(entity));
   }

   public int getDecayTime(double magnitude, boolean source) {
      double decayRate = source ? MekanismConfig.general.radiationSourceDecayRate.get() : MekanismConfig.general.radiationTargetDecayRate.get();
      int seconds = 0;

      for (double localMagnitude = magnitude; localMagnitude > 1.0E-5; seconds++) {
         localMagnitude *= decayRate;
      }

      return seconds;
   }

   @Override
   public Table<Chunk3D, Coord4D, IRadiationSource> getRadiationSources() {
      return this.radiationView;
   }

   @Override
   public void removeRadiationSources(Chunk3D chunk) {
      Map<Coord4D, RadiationSource> chunkSources = this.radiationTable.row(chunk);
      if (!chunkSources.isEmpty()) {
         chunkSources.clear();
         this.markDirty();
         this.updateClientRadiationForAll(chunk.dimension);
      }
   }

   @Override
   public void removeRadiationSource(Coord4D coord) {
      Chunk3D chunk = new Chunk3D(coord);
      if (this.radiationTable.contains(chunk, coord)) {
         this.radiationTable.remove(chunk, coord);
         this.markDirty();
         this.updateClientRadiationForAll(coord.dimension);
      }
   }

   @Override
   public double getRadiationLevel(Coord4D coord) {
      return this.getRadiationLevelAndMaxMagnitude(coord).level();
   }

   public RadiationManager.LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(Entity player) {
      return this.getRadiationLevelAndMaxMagnitude(new Coord4D(player));
   }

   public RadiationManager.LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(Coord4D coord) {
      double level = 1.0E-7;
      double maxMagnitude = 1.0E-7;

      for (Chunk3D chunk : new Chunk3D(coord).expand(MekanismConfig.general.radiationChunkCheckRadius.get())) {
         for (Entry<Coord4D, RadiationSource> entry : this.radiationTable.row(chunk).entrySet()) {
            if (entry.getKey().distanceTo(coord) <= MAX_RANGE.getAsInt()) {
               RadiationSource source = entry.getValue();
               level += this.computeExposure(coord, source);
               maxMagnitude = Math.max(maxMagnitude, source.getMagnitude());
            }
         }
      }

      return new RadiationManager.LevelAndMaxMagnitude(level, maxMagnitude);
   }

   @Override
   public void radiate(Coord4D coord, double magnitude) {
      if (this.isRadiationEnabled()) {
         Map<Coord4D, RadiationSource> radiationSourceMap = this.radiationTable.row(new Chunk3D(coord));
         RadiationSource src = radiationSourceMap.get(coord);
         if (src == null) {
            radiationSourceMap.put(coord, new RadiationSource(coord, magnitude));
         } else {
            src.radiate(magnitude);
         }

         this.markDirty();
         this.updateClientRadiationForAll(coord.dimension);
      }
   }

   @Override
   public void radiate(LivingEntity entity, double magnitude) {
      if (this.isRadiationEnabled()) {
         if (!(entity instanceof Player player && !MekanismUtils.isPlayingMode(player))) {
            entity.getCapability(Capabilities.RADIATION_ENTITY)
               .ifPresent(c -> c.radiate(magnitude * (1.0 - Math.min(1.0, this.getRadiationResistance(entity)))));
         }
      }
   }

   @Override
   public void dumpRadiation(Coord4D coord, IGasHandler gasHandler, boolean clearRadioactive) {
      int tank = 0;

      for (int gasTanks = gasHandler.getTanks(); tank < gasTanks; tank++) {
         if (this.dumpRadiation(coord, gasHandler.getChemicalInTank(tank)) && clearRadioactive) {
            gasHandler.setChemicalInTank(tank, GasStack.EMPTY);
         }
      }
   }

   @Override
   public void dumpRadiation(Coord4D coord, List<IGasTank> gasTanks, boolean clearRadioactive) {
      for (IGasTank gasTank : gasTanks) {
         if (this.dumpRadiation(coord, gasTank.getStack()) && clearRadioactive) {
            gasTank.setEmpty();
         }
      }
   }

   @Override
   public boolean dumpRadiation(Coord4D coord, GasStack stack) {
      if (this.isRadiationEnabled() && !stack.isEmpty()) {
         double radioactivity = stack.mapAttributeToDouble(
            GasAttributes.Radiation.class, (stored, attribute) -> stored.getAmount() * attribute.getRadioactivity()
         );
         if (radioactivity > 0.0) {
            this.radiate(coord, radioactivity);
            return true;
         }
      }

      return false;
   }

   public void createMeltdown(Level world, BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID) {
      this.meltdowns
         .computeIfAbsent(world.m_46472_().m_135782_(), id -> new ArrayList<>())
         .add(new Meltdown(minPos, maxPos, magnitude, chance, radius, multiblockID));
      this.markDirty();
   }

   public void clearSources() {
      if (!this.radiationTable.isEmpty()) {
         this.radiationTable.clear();
         this.markDirty();
         this.updateClientRadiationForAll(ConstantPredicates.alwaysTrue());
      }
   }

   private double computeExposure(Coord4D coord, RadiationSource source) {
      return source.getMagnitude() / Math.max(1.0, coord.distanceToSquared(source.getPos()));
   }

   private double getRadiationResistance(LivingEntity entity) {
      double resistance = 0.0;

      for (EquipmentSlot type : EnumUtils.ARMOR_SLOTS) {
         ItemStack stack = entity.m_6844_(type);
         Optional<IRadiationShielding> shielding = CapabilityUtils.getCapability(stack, Capabilities.RADIATION_SHIELDING, null).resolve();
         if (shielding.isPresent()) {
            resistance += shielding.get().getRadiationShielding();
         }
      }

      if (resistance < 1.0 && Mekanism.hooks.CuriosLoaded) {
         Optional<? extends IItemHandler> handlerOptional = CuriosIntegration.getCuriosInventory(entity);
         if (handlerOptional.isPresent()) {
            IItemHandler handler = handlerOptional.get();
            int slots = handler.getSlots();

            for (int i = 0; i < slots; i++) {
               ItemStack item = handler.getStackInSlot(i);
               Optional<IRadiationShielding> shielding = CapabilityUtils.getCapability(item, Capabilities.RADIATION_SHIELDING, null).resolve();
               if (shielding.isPresent()) {
                  resistance += shielding.get().getRadiationShielding();
                  if (resistance >= 1.0) {
                     return 1.0;
                  }
               }
            }
         }
      }

      return resistance;
   }

   private void updateClientRadiationForAll(ResourceKey<Level> dimension) {
      this.updateClientRadiationForAll((Predicate<ServerPlayer>)(player -> player.m_9236_().m_46472_() == dimension));
   }

   private void updateClientRadiationForAll(Predicate<ServerPlayer> clearForPlayer) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         for (ServerPlayer player : server.m_6846_().m_11314_()) {
            if (clearForPlayer.test(player)) {
               this.updateClientRadiation(player);
            }
         }
      }
   }

   public void updateClientRadiation(ServerPlayer player) {
      RadiationManager.LevelAndMaxMagnitude levelAndMaxMagnitude = this.getRadiationLevelAndMaxMagnitude(player);
      RadiationManager.PreviousRadiationData previousRadiationData = this.playerEnvironmentalExposureMap.get(player.m_20148_());
      RadiationManager.PreviousRadiationData relevantData = RadiationManager.PreviousRadiationData.compareTo(
         previousRadiationData, levelAndMaxMagnitude.level()
      );
      if (relevantData != null) {
         this.playerEnvironmentalExposureMap.put(player.m_20148_(), relevantData);
         Mekanism.packetHandler().sendTo(PacketRadiationData.createEnvironmental(levelAndMaxMagnitude), player);
      }
   }

   public void setClientEnvironmentalRadiation(double radiation, double maxMagnitude) {
      this.clientEnvironmentalRadiation = radiation;
      this.clientMaxMagnitude = maxMagnitude;
      this.clientRadiationScale = RadiationManager.RadiationScale.get(this.clientEnvironmentalRadiation);
   }

   public double getClientEnvironmentalRadiation() {
      return this.isRadiationEnabled() ? this.clientEnvironmentalRadiation : 1.0E-7;
   }

   public double getClientMaxMagnitude() {
      return this.isRadiationEnabled() ? this.clientMaxMagnitude : 1.0E-7;
   }

   public RadiationManager.RadiationScale getClientScale() {
      return this.isRadiationEnabled() ? this.clientRadiationScale : RadiationManager.RadiationScale.NONE;
   }

   public void tickClient(Player player) {
      if (this.isRadiationEnabled()) {
         RandomSource randomSource = player.m_9236_().m_213780_();
         if (this.clientRadiationScale != RadiationManager.RadiationScale.NONE
            && MekanismConfig.client.radiationParticleCount.get() != 0
            && randomSource.m_188503_(2) == 0) {
            int count = randomSource.m_188503_(this.clientRadiationScale.ordinal() * MekanismConfig.client.radiationParticleCount.get());
            int radius = MekanismConfig.client.radiationParticleRadius.get();

            for (int i = 0; i < count; i++) {
               double x = player.m_20185_() + randomSource.m_188500_() * radius * 2.0 - radius;
               double y = player.m_20186_() + randomSource.m_188500_() * radius * 2.0 - radius;
               double z = player.m_20189_() + randomSource.m_188500_() * radius * 2.0 - radius;
               player.m_9236_().m_7106_((ParticleOptions)MekanismParticleTypes.RADIATION.get(), x, y, z, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   public void tickServer(ServerPlayer player) {
      this.updateEntityRadiation(player);
   }

   private void updateEntityRadiation(LivingEntity entity) {
      if (this.isRadiationEnabled()) {
         LazyOptional<IRadiationEntity> radiationCap = entity.getCapability(Capabilities.RADIATION_ENTITY);
         if (entity.m_9236_().m_213780_().m_188503_(20) == 0) {
            double magnitude = this.getRadiationLevel(entity);
            if (magnitude > 1.0E-7 && !(entity instanceof Player player && !MekanismUtils.isPlayingMode(player))) {
               this.radiate(entity, magnitude / 3600.0);
            }

            radiationCap.ifPresent(IRadiationEntity::decay);
         }

         radiationCap.ifPresent(c -> {
            c.update(entity);
            if (entity instanceof ServerPlayer playerx) {
               double radiation = c.getRadiation();
               RadiationManager.PreviousRadiationData previousRadiationData = this.playerExposureMap.get(playerx.m_20148_());
               RadiationManager.PreviousRadiationData relevantData = RadiationManager.PreviousRadiationData.compareTo(previousRadiationData, radiation);
               if (relevantData != null) {
                  this.playerExposureMap.put(playerx.m_20148_(), relevantData);
                  Mekanism.packetHandler().sendTo(PacketRadiationData.createPlayer(radiation), playerx);
               }
            }
         });
      }
   }

   public void tickServerWorld(Level world) {
      if (this.isRadiationEnabled()) {
         if (!this.loaded) {
            this.createOrLoad();
         }

         List<Meltdown> dimensionMeltdowns = this.meltdowns.getOrDefault(world.m_46472_().m_135782_(), Collections.emptyList());
         if (!dimensionMeltdowns.isEmpty()) {
            dimensionMeltdowns.removeIf(meltdown -> meltdown.update(world));
            this.markDirty();
         }
      }
   }

   public void tickServer() {
      if (this.isRadiationEnabled()) {
         if (RAND.nextInt(20) == 0) {
            Collection<RadiationSource> sources = this.radiationTable.values();
            if (!sources.isEmpty()) {
               sources.removeIf(RadiationSource::decay);
               this.markDirty();
               this.updateClientRadiationForAll(ConstantPredicates.alwaysTrue());
            }
         }
      }
   }

   public void createOrLoad() {
      if (this.dataHandler == null) {
         this.dataHandler = MekanismSavedData.createSavedData(RadiationManager.RadiationDataHandler::new, "radiation_manager");
         this.dataHandler.setManagerAndSync(this);
         this.dataHandler.clearCached();
      }

      this.loaded = true;
   }

   public void reset() {
      this.radiationTable.clear();
      this.playerEnvironmentalExposureMap.clear();
      this.playerExposureMap.clear();
      this.meltdowns.clear();
      this.dataHandler = null;
      this.loaded = false;
   }

   public void resetClient() {
      this.setClientEnvironmentalRadiation(1.0E-7, 1.0E-7);
   }

   public void resetPlayer(UUID uuid) {
      this.playerEnvironmentalExposureMap.remove(uuid);
      this.playerExposureMap.remove(uuid);
   }

   @SubscribeEvent
   public void onLivingTick(LivingTickEvent event) {
      Level world = event.getEntity().m_9236_();
      if (!world.m_5776_() && !(event.getEntity() instanceof Player)) {
         this.updateEntityRadiation(event.getEntity());
      }
   }

   public record LevelAndMaxMagnitude(double level, double maxMagnitude) {
   }

   private record PreviousRadiationData(double magnitude, int power, double base) {
      private static int getPower(double magnitude) {
         return MathUtils.clampToInt(Math.floor(Math.log10(magnitude)));
      }

      @Nullable
      private static RadiationManager.PreviousRadiationData compareTo(@Nullable RadiationManager.PreviousRadiationData previousRadiationData, double magnitude) {
         if (previousRadiationData != null && !(Math.abs(magnitude - previousRadiationData.magnitude) >= previousRadiationData.base)) {
            if (magnitude < previousRadiationData.magnitude) {
               int power = getPower(magnitude);
               if (power < previousRadiationData.power) {
                  return getData(magnitude, power);
               }
            }

            return null;
         } else {
            return getData(magnitude, getPower(magnitude));
         }
      }

      private static RadiationManager.PreviousRadiationData getData(double magnitude, int power) {
         int siPower = Math.floorDiv(power, 3) * 3;
         double base = Math.pow(10.0, siPower - 2);
         return new RadiationManager.PreviousRadiationData(magnitude, power, base);
      }
   }

   public static class RadiationDataHandler extends MekanismSavedData {
      private Map<ResourceLocation, List<Meltdown>> savedMeltdowns = Collections.emptyMap();
      public List<RadiationSource> loadedSources = Collections.emptyList();
      @Nullable
      public RadiationManager manager;

      public void setManagerAndSync(RadiationManager m) {
         this.manager = m;
         if (IRadiationManager.INSTANCE.isRadiationEnabled()) {
            for (RadiationSource source : this.loadedSources) {
               this.manager.radiationTable.put(new Chunk3D(source.getPos()), source.getPos(), source);
            }

            for (Entry<ResourceLocation, List<Meltdown>> entry : this.savedMeltdowns.entrySet()) {
               List<Meltdown> meltdowns = entry.getValue();
               this.manager.meltdowns.computeIfAbsent(entry.getKey(), id -> new ArrayList<>(meltdowns.size())).addAll(meltdowns);
            }
         }
      }

      public void clearCached() {
         this.loadedSources = Collections.emptyList();
         this.savedMeltdowns = Collections.emptyMap();
      }

      @Override
      public void load(@NotNull CompoundTag nbtTags) {
         if (nbtTags.m_128425_("radList", 9)) {
            ListTag list = nbtTags.m_128437_("radList", 10);
            this.loadedSources = new HashList<>(list.size());

            for (Tag nbt : list) {
               this.loadedSources.add(RadiationSource.load((CompoundTag)nbt));
            }
         } else {
            this.loadedSources = Collections.emptyList();
         }

         if (nbtTags.m_128425_("meltdowns", 10)) {
            CompoundTag meltdownNBT = nbtTags.m_128469_("meltdowns");
            this.savedMeltdowns = new HashMap<>(meltdownNBT.m_128440_());

            for (String dim : meltdownNBT.m_128431_()) {
               ResourceLocation dimension = ResourceLocation.m_135820_(dim);
               if (dimension != null) {
                  ListTag meltdowns = meltdownNBT.m_128437_(dim, 10);
                  this.savedMeltdowns.put(dimension, meltdowns.stream().map(nbt -> Meltdown.load((CompoundTag)nbt)).collect(Collectors.toList()));
               }
            }
         } else {
            this.savedMeltdowns = Collections.emptyMap();
         }
      }

      @NotNull
      public CompoundTag m_7176_(@NotNull CompoundTag nbtTags) {
         if (this.manager != null && !this.manager.radiationTable.isEmpty()) {
            ListTag list = new ListTag();

            for (RadiationSource source : this.manager.radiationTable.values()) {
               CompoundTag compound = new CompoundTag();
               source.write(compound);
               list.add(compound);
            }

            nbtTags.m_128365_("radList", list);
         }

         if (this.manager != null && !this.manager.meltdowns.isEmpty()) {
            CompoundTag meltdownNBT = new CompoundTag();

            for (Entry<ResourceLocation, List<Meltdown>> entry : this.manager.meltdowns.entrySet()) {
               List<Meltdown> meltdowns = entry.getValue();
               if (!meltdowns.isEmpty()) {
                  ListTag list = new ListTag();

                  for (Meltdown meltdown : meltdowns) {
                     CompoundTag compound = new CompoundTag();
                     meltdown.write(compound);
                     list.add(compound);
                  }

                  meltdownNBT.m_128365_(entry.getKey().toString(), list);
               }
            }

            if (!meltdownNBT.m_128456_()) {
               nbtTags.m_128365_("meltdowns", meltdownNBT);
            }
         }

         return nbtTags;
      }
   }

   public static enum RadiationScale {
      NONE,
      LOW,
      MEDIUM,
      ELEVATED,
      HIGH,
      EXTREME;

      private static final double LOG_BASELINE = Math.log10(1.0E-5);
      private static final double LOG_MAX = Math.log10(100.0);
      private static final double SCALE = LOG_MAX - LOG_BASELINE;

      public static RadiationManager.RadiationScale get(double magnitude) {
         if (magnitude < 1.0E-5) {
            return NONE;
         } else if (magnitude < 0.001) {
            return LOW;
         } else if (magnitude < 0.1) {
            return MEDIUM;
         } else if (magnitude < 10.0) {
            return ELEVATED;
         } else {
            return magnitude < 100.0 ? HIGH : EXTREME;
         }
      }

      public static EnumColor getSeverityColor(double magnitude) {
         if (magnitude <= 1.0E-7) {
            return EnumColor.BRIGHT_GREEN;
         } else if (magnitude < 1.0E-5) {
            return EnumColor.GRAY;
         } else if (magnitude < 0.001) {
            return EnumColor.YELLOW;
         } else if (magnitude < 0.1) {
            return EnumColor.ORANGE;
         } else {
            return magnitude < 10.0 ? EnumColor.RED : EnumColor.DARK_RED;
         }
      }

      public static double getScaledDoseSeverity(double magnitude) {
         return magnitude < 1.0E-5 ? 0.0 : Math.min(1.0, Math.max(0.0, (-LOG_BASELINE + Math.log10(magnitude)) / SCALE));
      }

      public SoundEvent getSoundEvent() {
         return switch (this) {
            case LOW -> (SoundEvent)MekanismSounds.GEIGER_SLOW.get();
            case MEDIUM -> (SoundEvent)MekanismSounds.GEIGER_MEDIUM.get();
            case ELEVATED, HIGH -> (SoundEvent)MekanismSounds.GEIGER_ELEVATED.get();
            case EXTREME -> (SoundEvent)MekanismSounds.GEIGER_FAST.get();
            default -> null;
         };
      }
   }
}

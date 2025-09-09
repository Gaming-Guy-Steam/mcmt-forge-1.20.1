package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class FormationProtocol<T extends MultiblockData> {
   public static final int MAX_SIZE = 18;
   private final IMultiblock<T> pointer;
   private final Structure structure;
   private final MultiblockManager<T> manager;
   public final Set<BlockPos> locations = new ObjectOpenHashSet();
   public final Set<BlockPos> internalLocations = new ObjectOpenHashSet();
   public final Set<IValveHandler.ValveData> valves = new ObjectOpenHashSet();
   public final Map<UUID, MultiblockCache<T>> idsFound = new HashMap<>();

   public FormationProtocol(IMultiblock<T> tile, Structure structure) {
      this.pointer = tile;
      this.structure = structure;
      this.manager = tile.getManager();
   }

   private FormationProtocol.StructureResult<T> buildStructure(IStructureValidator<T> validator) {
      T structure = this.pointer.createMultiblock();
      if (!structure.setShape(validator.getShape())) {
         return this.fail(FormationProtocol.FormationResult.FAIL);
      } else {
         Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap();
         FormationProtocol.FormationResult result = validator.validate(this, chunkMap);
         if (!result.isFormed()) {
            return this.fail(result);
         } else {
            structure.locations = this.locations;
            structure.internalLocations = this.internalLocations;
            structure.valves = this.valves;
            result = validator.postcheck(structure, chunkMap);
            return result.isFormed() ? this.form(structure, this.idsFound) : this.fail(result);
         }
      }
   }

   public FormationProtocol.FormationResult doUpdate() {
      IStructureValidator<T> validator = this.manager.createValidator();
      Level world = this.pointer.getTileWorld();
      validator.init(world, this.manager, this.structure);
      if (!validator.precheck()) {
         return FormationProtocol.FormationResult.FAIL;
      } else {
         FormationProtocol.StructureResult<T> result = this.buildStructure(validator);
         T structureFound = result.structureFound;
         BlockPos pointerPos = this.pointer.getTilePos();
         if (structureFound != null && structureFound.locations.contains(pointerPos)) {
            this.pointer.setMultiblockData(this.manager, structureFound);
            structureFound.setFormedForce(true);
            MultiblockCache<T> cache = null;
            UUID idToUse = this.manager.getUniqueInventoryID();
            if (!result.idsFound.isEmpty()) {
               MultiblockCache.RejectContents rejectContents = new MultiblockCache.RejectContents();

               for (Entry<UUID, MultiblockCache<T>> entry : result.idsFound.entrySet()) {
                  if (cache == null) {
                     cache = entry.getValue();
                  } else {
                     cache.merge(entry.getValue(), rejectContents);
                  }
               }

               this.manager.replaceCaches(result.idsFound().keySet(), idToUse, cache);
               if (!rejectContents.rejectedItems.isEmpty()) {
                  Vec3 dropPosition = Vec3.m_82512_(pointerPos);
                  Player nearestPlayer = world.m_45924_(dropPosition.f_82479_, dropPosition.f_82480_, dropPosition.f_82481_, 25.0, true);
                  if (nearestPlayer != null) {
                     dropPosition = nearestPlayer.m_20182_();
                  }

                  for (ItemStack rejectedItem : rejectContents.rejectedItems) {
                     world.m_7967_(new ItemEntity(world, dropPosition.f_82479_, dropPosition.f_82480_, dropPosition.f_82481_, rejectedItem));
                  }
               }

               if (!rejectContents.rejectedGases.isEmpty() && IRadiationManager.INSTANCE.isRadiationEnabled()) {
                  double radiation = 0.0;

                  for (GasStack rejectedGas : rejectContents.rejectedGases) {
                     radiation += rejectedGas.mapAttributeToDouble(
                        GasAttributes.Radiation.class, (stored, attribute) -> stored.getAmount() * attribute.getRadioactivity()
                     );
                  }

                  if (radiation > 0.0) {
                     Coord4D dumpLocation = new Coord4D(structureFound.getBounds().getCenter(), world);
                     IRadiationManager.INSTANCE.radiate(dumpLocation, radiation);
                  }
               }
            }

            boolean trackCache = cache == null;
            if (trackCache) {
               cache = this.manager.createCache();
            }

            cache.apply(structureFound);
            structureFound.inventoryID = idToUse;
            structureFound.onCreated(world);
            if (trackCache) {
               cache.sync(structureFound);
               this.manager.trackCache(idToUse, cache);
            }

            return FormationProtocol.FormationResult.SUCCESS;
         } else {
            this.pointer.getStructure().removeMultiblock(world);
            return result.result();
         }
      }
   }

   protected static Component text(BlockPos pos) {
      return MekanismLang.GENERIC_PARENTHESIS
         .translate(new Object[]{MekanismLang.GENERIC_BLOCK_POS.translate(new Object[]{pos.m_123341_(), pos.m_123342_(), pos.m_123343_()})});
   }

   public static int explore(BlockPos start, Predicate<BlockPos> checker) {
      return explore(start, checker, 5832);
   }

   public static int explore(BlockPos start, Predicate<BlockPos> checker, int maxCount) {
      if (!checker.test(start)) {
         return 0;
      } else {
         Queue<BlockPos> openSet = new LinkedList<>();
         Set<BlockPos> traversed = new ObjectOpenHashSet();
         openSet.add(start);
         traversed.add(start);

         while (!openSet.isEmpty()) {
            BlockPos ptr = openSet.poll();
            int traversedSize = traversed.size();
            if (traversedSize >= maxCount) {
               return traversedSize;
            }

            for (Direction side : EnumUtils.DIRECTIONS) {
               BlockPos offset = ptr.m_121945_(side);
               if (!traversed.contains(offset) && checker.test(offset)) {
                  openSet.add(offset);
                  traversed.add(offset);
               }
            }
         }

         return traversed.size();
      }
   }

   private FormationProtocol.StructureResult<T> fail(FormationProtocol.FormationResult result) {
      return new FormationProtocol.StructureResult<>(result, null, null);
   }

   private FormationProtocol.StructureResult<T> form(T structureFound, Map<UUID, MultiblockCache<T>> idsFound) {
      return new FormationProtocol.StructureResult<>(FormationProtocol.FormationResult.SUCCESS, structureFound, idsFound);
   }

   public static enum CasingType {
      FRAME,
      VALVE,
      OTHER,
      INVALID;

      boolean isFrame() {
         return this == FRAME;
      }

      boolean isValve() {
         return this == VALVE;
      }
   }

   public static class FormationResult {
      public static final FormationProtocol.FormationResult SUCCESS = new FormationProtocol.FormationResult(true, null, false);
      public static final FormationProtocol.FormationResult FAIL = new FormationProtocol.FormationResult(false, null, false);
      private final Component resultText;
      private final boolean formed;
      private final boolean noIgnore;

      private FormationResult(boolean formed, Component resultText, boolean noIgnore) {
         this.formed = formed;
         this.resultText = resultText;
         this.noIgnore = noIgnore;
      }

      public static FormationProtocol.FormationResult fail(ILangEntry text, BlockPos pos) {
         return fail(text, pos, false);
      }

      public static FormationProtocol.FormationResult fail(ILangEntry text, BlockPos pos, boolean noIgnore) {
         return fail(text.translateColored(EnumColor.GRAY, EnumColor.INDIGO, FormationProtocol.text(pos)), noIgnore);
      }

      public static FormationProtocol.FormationResult fail(ILangEntry text) {
         return fail(text, false);
      }

      public static FormationProtocol.FormationResult fail(ILangEntry text, boolean noIgnore) {
         return fail(text.translateColored(EnumColor.GRAY), noIgnore);
      }

      public static FormationProtocol.FormationResult fail(Component text) {
         return fail(text, false);
      }

      public static FormationProtocol.FormationResult fail(Component text, boolean noIgnore) {
         return new FormationProtocol.FormationResult(false, text, noIgnore);
      }

      public boolean isFormed() {
         return this.formed;
      }

      public boolean isNoIgnore() {
         return this.noIgnore;
      }

      public Component getResultText() {
         return this.resultText;
      }
   }

   public static enum StructureRequirement {
      IGNORED,
      FRAME,
      OTHER,
      INNER;

      public static final FormationProtocol.StructureRequirement[] REQUIREMENTS = values();

      boolean needsFrame() {
         return this == FRAME;
      }

      boolean isCasing() {
         return this != INNER;
      }
   }

   private record StructureResult<T extends MultiblockData>(FormationProtocol.FormationResult result, T structureFound, Map<UUID, MultiblockCache<T>> idsFound) {
   }
}

package mekanism.common.inventory.container.sync.dynamic;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.MekAnnotationScanner;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.network.to_client.container.property.PropertyType;
import mekanism.common.util.LambdaMetaFactoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

public class SyncMapper extends MekAnnotationScanner.BaseAnnotationScanner {
   public static final SyncMapper INSTANCE = new SyncMapper();
   public static final String DEFAULT_TAG = "default";
   private final List<SyncMapper.SpecialPropertyHandler<?>> specialProperties = new ArrayList<>();
   private final Map<Class<?>, SyncMapper.PropertyDataClassCache> syncablePropertyMap = new Object2ObjectOpenHashMap();

   private SyncMapper() {
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               IExtendedFluidTank.class, SyncMapper.SpecialPropertyData.create(FluidStack.class, IFluidTank::getFluid, IExtendedFluidTank::setStackUnchecked)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               IGasTank.class, SyncMapper.SpecialPropertyData.create(GasStack.class, IChemicalTank::getStack, IChemicalTank::setStackUnchecked)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               IInfusionTank.class, SyncMapper.SpecialPropertyData.create(InfusionStack.class, IChemicalTank::getStack, IChemicalTank::setStackUnchecked)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               IPigmentTank.class, SyncMapper.SpecialPropertyData.create(PigmentStack.class, IChemicalTank::getStack, IChemicalTank::setStackUnchecked)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               ISlurryTank.class, SyncMapper.SpecialPropertyData.create(SlurryStack.class, IChemicalTank::getStack, IChemicalTank::setStackUnchecked)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               IEnergyContainer.class, SyncMapper.SpecialPropertyData.create(FloatingLong.class, IEnergyContainer::getEnergy, IEnergyContainer::setEnergy)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               BasicHeatCapacitor.class,
               SyncMapper.SpecialPropertyData.create(double.class, BasicHeatCapacitor::getHeatCapacity, BasicHeatCapacitor::setHeatCapacityFromPacket),
               SyncMapper.SpecialPropertyData.create(double.class, IHeatCapacitor::getHeat, IHeatCapacitor::setHeat)
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               MergedTank.class,
               SyncMapper.SpecialPropertyData.create(
                  FluidStack.class, obj -> obj.getFluidTank().getFluid(), (obj, val) -> obj.getFluidTank().setStackUnchecked(val)
               ),
               SyncMapper.SpecialPropertyData.create(GasStack.class, obj -> obj.getGasTank().getStack(), (obj, val) -> obj.getGasTank().setStackUnchecked(val)),
               SyncMapper.SpecialPropertyData.create(
                  InfusionStack.class, obj -> obj.getInfusionTank().getStack(), (obj, val) -> obj.getInfusionTank().setStackUnchecked(val)
               ),
               SyncMapper.SpecialPropertyData.create(
                  PigmentStack.class, obj -> obj.getPigmentTank().getStack(), (obj, val) -> obj.getPigmentTank().setStackUnchecked(val)
               ),
               SyncMapper.SpecialPropertyData.create(
                  SlurryStack.class, obj -> obj.getSlurryTank().getStack(), (obj, val) -> obj.getSlurryTank().setStackUnchecked(val)
               )
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               MergedChemicalTank.class,
               SyncMapper.SpecialPropertyData.create(GasStack.class, obj -> obj.getGasTank().getStack(), (obj, val) -> obj.getGasTank().setStackUnchecked(val)),
               SyncMapper.SpecialPropertyData.create(
                  InfusionStack.class, obj -> obj.getInfusionTank().getStack(), (obj, val) -> obj.getInfusionTank().setStackUnchecked(val)
               ),
               SyncMapper.SpecialPropertyData.create(
                  PigmentStack.class, obj -> obj.getPigmentTank().getStack(), (obj, val) -> obj.getPigmentTank().setStackUnchecked(val)
               ),
               SyncMapper.SpecialPropertyData.create(
                  SlurryStack.class, obj -> obj.getSlurryTank().getStack(), (obj, val) -> obj.getSlurryTank().setStackUnchecked(val)
               )
            )
         );
      this.specialProperties
         .add(
            new SyncMapper.SpecialPropertyHandler<>(
               VoxelCuboid.class,
               SyncMapper.SpecialPropertyData.create(BlockPos.class, VoxelCuboid::getMinPos, VoxelCuboid::setMinPos),
               SyncMapper.SpecialPropertyData.create(BlockPos.class, VoxelCuboid::getMaxPos, VoxelCuboid::setMaxPos)
            )
         );
   }

   @Override
   protected Map<ElementType, Type[]> getSupportedTypes() {
      return Collections.singletonMap(ElementType.FIELD, new Type[]{Type.getType(ContainerSync.class)});
   }

   @Override
   protected void collectScanData(Map<String, Class<?>> classNameCache, Map<Class<?>, List<AnnotationData>> knownClasses, Set<IModFileInfo> modFileData) {
      Map<Class<?>, List<SyncMapper.PropertyFieldInfo>> rawPropertyMap = new Object2ObjectOpenHashMap();
      List<String> fallbackTagsList = Collections.singletonList("default");

      label85:
      for (Entry<Class<?>, List<AnnotationData>> entry : knownClasses.entrySet()) {
         Class<?> annotatedClass = entry.getKey();
         List<SyncMapper.PropertyFieldInfo> propertyInfo = new ArrayList<>();
         rawPropertyMap.put(annotatedClass, propertyInfo);
         Iterator var10 = entry.getValue().iterator();

         while (true) {
            AnnotationData data;
            String fieldName;
            SyncMapper.PropertyField newField;
            while (true) {
               if (!var10.hasNext()) {
                  continue label85;
               }

               data = (AnnotationData)var10.next();
               fieldName = data.memberName();
               Field field = getField(annotatedClass, fieldName);
               if (field != null) {
                  String getterName = getAnnotationValue(data, "getter", "");
                  Class<?> fieldType = field.getType();
                  SyncMapper.SpecialPropertyHandler<?> handler = this.specialProperties
                     .stream()
                     .filter(h -> h.fieldType.isAssignableFrom(fieldType))
                     .findFirst()
                     .orElse(null);

                  try {
                     if (handler != null) {
                        newField = createSpecialProperty(handler, field, annotatedClass, getterName);
                        break;
                     }

                     PropertyType type = PropertyType.getFromType(fieldType);
                     String setterName = getAnnotationValue(data, "setter", "");
                     if (type != null) {
                        newField = new SyncMapper.PropertyField(
                           new SyncMapper.TrackedFieldData(
                              LambdaMetaFactoryUtil.createGetter(field, annotatedClass, getterName),
                              LambdaMetaFactoryUtil.createSetter(field, annotatedClass, setterName),
                              type
                           )
                        );
                        break;
                     }

                     if (fieldType.isEnum()) {
                        newField = new SyncMapper.PropertyField(
                           new SyncMapper.EnumFieldData(
                              LambdaMetaFactoryUtil.createGetter(field, annotatedClass, getterName),
                              LambdaMetaFactoryUtil.createSetter(field, annotatedClass, setterName),
                              fieldType
                           )
                        );
                        break;
                     }

                     if (fieldType.isArray()) {
                        Class<?> arrayFieldType = fieldType.getComponentType();
                        PropertyType arrayType = PropertyType.getFromType(arrayFieldType);
                        if (arrayType != null) {
                           newField = new SyncMapper.PropertyField(
                              new SyncMapper.ArrayFieldData(LambdaMetaFactoryUtil.createGetter(field, annotatedClass, getterName), arrayType)
                           );
                           break;
                        }

                        Mekanism.logger.error("Attempted to sync an invalid array field '{}' in class '{}'.", fieldName, annotatedClass.getSimpleName());
                     } else {
                        Mekanism.logger.error("Attempted to sync an invalid field '{}' in class '{}'.", fieldName, annotatedClass.getSimpleName());
                     }
                  } catch (Throwable var22) {
                     Mekanism.logger
                        .error("Failed to create sync data for field '{}' in class '{}'.", new Object[]{fieldName, annotatedClass.getSimpleName(), var22});
                  }
               }
            }

            String fullPath = annotatedClass.getName() + "#" + fieldName;

            for (String tag : getAnnotationValue(data, "tags", fallbackTagsList)) {
               propertyInfo.add(new SyncMapper.PropertyFieldInfo(fullPath, tag, newField));
            }
         }
      }

      for (MekAnnotationScanner.BaseAnnotationScanner.ClassBasedInfo<SyncMapper.PropertyFieldInfo> classPropertyInfo : combineWithParents(rawPropertyMap)) {
         SyncMapper.PropertyDataClassCache cache = new SyncMapper.PropertyDataClassCache();
         classPropertyInfo.infoList().sort(Comparator.comparing(info -> info.fieldPath + "|" + info.tag));

         for (SyncMapper.PropertyFieldInfo field : classPropertyInfo.infoList()) {
            cache.propertyFieldMap.put(field.tag, field.field);
         }

         this.syncablePropertyMap.put(classPropertyInfo.clazz(), cache);
      }
   }

   public void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier) {
      this.setup(container, holderClass, holderSupplier, "default");
   }

   public void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier, String tag) {
      SyncMapper.PropertyDataClassCache cache = this.syncablePropertyMap
         .computeIfAbsent(holderClass, clazz -> getData(this.syncablePropertyMap, (Class<?>)clazz, SyncMapper.PropertyDataClassCache.EMPTY));

      for (SyncMapper.PropertyField field : cache.propertyFieldMap.get(tag)) {
         for (SyncMapper.TrackedFieldData data : field.trackedData) {
            data.track(container, holderSupplier);
         }
      }
   }

   private static <O> SyncMapper.PropertyField createSpecialProperty(
      SyncMapper.SpecialPropertyHandler<O> handler, Field field, Class<?> objType, String getterName
   ) throws Throwable {
      SyncMapper.PropertyField ret = new SyncMapper.PropertyField();
      Function<Object, O> fieldGetter = LambdaMetaFactoryUtil.createGetter(field, objType, getterName);

      for (SyncMapper.SpecialPropertyData<O> data : handler.specialData) {
         SyncMapper.TrackedFieldData trackedField = SyncMapper.TrackedFieldData.create(
            data.propertyType, obj -> data.get(fieldGetter.apply(obj)), (obj, val) -> data.set(fieldGetter.apply(obj), val)
         );
         if (trackedField != null) {
            ret.addTrackedData(trackedField);
         }
      }

      return ret;
   }

   protected static class ArrayFieldData extends SyncMapper.TrackedFieldData {
      protected ArrayFieldData(Function<Object, Object> getter, PropertyType propertyType) {
         super(getter, null, propertyType);
      }

      @Override
      protected void track(MekanismContainer container, Supplier<Object> holderSupplier) {
         Object holder = holderSupplier.get();
         if (holder != null) {
            Object field = this.get(holder);
            if (field.getClass().isArray()) {
               int length = Array.getLength(field);

               for (int i = 0; i < length; i++) {
                  int index = i;
                  container.track(this.create(() -> {
                     Object obj = holderSupplier.get();
                     return obj == null ? this.getDefault() : Array.get(this.get(obj), index);
                  }, value -> {
                     Object obj = holderSupplier.get();
                     if (obj != null) {
                        Array.set(this.get(obj), index, value);
                     }
                  }));
               }
            } else {
               Mekanism.logger.error("Unexpected field type '{}' is not an array.", field.getClass());
            }
         } else {
            Mekanism.logger.error("Unable to get holder object to add array tracker to.");
         }
      }

      @Override
      protected void set(Object dataObj, Object value) {
         throw new UnsupportedOperationException("Unsupported, uses overridden.");
      }

      @Override
      protected ISyncableData createSyncableData(Supplier<Object> obj) {
         throw new UnsupportedOperationException("Unsupported, uses overridden.");
      }
   }

   protected static class EnumFieldData extends SyncMapper.TrackedFieldData {
      private final Object[] constants;

      private EnumFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter, Class<?> enumClass) {
         super(getter, setter);
         this.constants = enumClass.getEnumConstants();
      }

      @Override
      protected ISyncableData create(Supplier<Object> getter, Consumer<Object> setter) {
         return this.createData((Enum[])this.constants, getter, setter);
      }

      protected <ENUM extends Enum<ENUM>> ISyncableData createData(ENUM[] constants, Supplier<Object> getter, Consumer<Object> setter) {
         return SyncableEnum.create(val -> constants[val], constants[0], () -> (ENUM)getter.get(), setter::accept);
      }

      @Override
      protected Object getDefault() {
         return this.constants[0];
      }
   }

   private static class PropertyDataClassCache {
      private static final SyncMapper.PropertyDataClassCache EMPTY = new SyncMapper.PropertyDataClassCache();
      private final Multimap<String, SyncMapper.PropertyField> propertyFieldMap = LinkedHashMultimap.create();
   }

   private static class PropertyField {
      private final List<SyncMapper.TrackedFieldData> trackedData = new ArrayList<>();

      private PropertyField(SyncMapper.TrackedFieldData... data) {
         Collections.addAll(this.trackedData, data);
      }

      private void addTrackedData(SyncMapper.TrackedFieldData data) {
         this.trackedData.add(data);
      }
   }

   private record PropertyFieldInfo(String fieldPath, String tag, SyncMapper.PropertyField field) {
   }

   protected static class SpecialPropertyData<O> {
      private final Class<?> propertyType;
      private final Function<O, ?> getter;
      private final BiConsumer<O, Object> setter;

      private SpecialPropertyData(Class<?> propertyType, Function<O, ?> getter, BiConsumer<O, Object> setter) {
         this.propertyType = propertyType;
         this.getter = getter;
         this.setter = setter;
      }

      protected Object get(O obj) {
         return this.getter.apply(obj);
      }

      protected void set(O obj, Object val) {
         this.setter.accept(obj, val);
      }

      protected static <O, V> SyncMapper.SpecialPropertyData<O> create(Class<V> propertyType, Function<O, V> getter, BiConsumer<O, V> setter) {
         return new SyncMapper.SpecialPropertyData<>(propertyType, getter, setter);
      }
   }

   private static class SpecialPropertyHandler<O> {
      private final Class<O> fieldType;
      private final List<SyncMapper.SpecialPropertyData<O>> specialData;

      @SafeVarargs
      private SpecialPropertyHandler(Class<O> fieldType, SyncMapper.SpecialPropertyData<O>... data) {
         this.fieldType = fieldType;
         this.specialData = List.of(data);
      }
   }

   protected static class TrackedFieldData {
      private PropertyType propertyType;
      private final Function<Object, Object> getter;
      private final BiConsumer<Object, Object> setter;

      protected TrackedFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter) {
         this.getter = getter;
         this.setter = setter;
      }

      private TrackedFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter, PropertyType propertyType) {
         this(getter, setter);
         this.propertyType = propertyType;
      }

      protected void track(MekanismContainer container, Supplier<Object> holderSupplier) {
         container.track(this.createSyncableData(holderSupplier));
      }

      protected Object get(Object dataObj) {
         return this.getter.apply(dataObj);
      }

      protected void set(Object dataObj, Object value) {
         this.setter.accept(dataObj, value);
      }

      protected ISyncableData createSyncableData(Supplier<Object> obj) {
         return this.create(() -> {
            Object dataObj = obj.get();
            return dataObj == null ? this.getDefault() : this.get(dataObj);
         }, val -> {
            Object dataObj = obj.get();
            if (dataObj != null) {
               this.set(dataObj, val);
            }
         });
      }

      protected ISyncableData create(Supplier<Object> getter, Consumer<Object> setter) {
         return this.propertyType.create(getter, setter);
      }

      protected Object getDefault() {
         return this.propertyType.getDefault();
      }

      protected static SyncMapper.TrackedFieldData create(Class<?> propertyType, Function<Object, Object> getter, BiConsumer<Object, Object> setter) {
         if (propertyType.isEnum()) {
            return new SyncMapper.EnumFieldData(getter, setter, propertyType);
         } else if (propertyType.isArray()) {
            Class<?> arrayType = propertyType.getComponentType();
            PropertyType type = PropertyType.getFromType(arrayType);
            if (type == null) {
               Mekanism.logger.error("Tried to create property data for invalid array type '{}'.", arrayType.getName());
               return null;
            } else {
               return new SyncMapper.ArrayFieldData(getter, type);
            }
         } else {
            PropertyType type = PropertyType.getFromType(propertyType);
            if (type == null) {
               Mekanism.logger.error("Tried to create property data for invalid type '{}'.", propertyType.getName());
               return null;
            } else {
               return new SyncMapper.TrackedFieldData(getter, setter, type);
            }
         }
      }
   }
}

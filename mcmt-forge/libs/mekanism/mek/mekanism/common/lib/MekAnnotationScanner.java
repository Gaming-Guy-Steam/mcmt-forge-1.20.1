package mekanism.common.lib;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation.EnumHolder;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public class MekAnnotationScanner {
   public static void collectScanData() {
      Map<String, Class<?>> classNameCache = new Object2ObjectOpenHashMap();
      Map<MekAnnotationScanner.BaseAnnotationScanner, MekAnnotationScanner.ScanData> scanners = new Object2ObjectArrayMap();
      Map<ElementType, List<MekAnnotationScanner.ScanData>> elementBasedScanData = new EnumMap<>(ElementType.class);
      addScanningSupport(scanners, elementBasedScanData, SyncMapper.INSTANCE);

      try {
         for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (AnnotationData data : scanData.getAnnotations()) {
               gatherScanData(elementBasedScanData, classNameCache, data, scanData.getIModInfoData());
            }
         }
      } catch (Throwable var9) {
         Mekanism.logger.error("Failed to gather scan data", var9);
      }

      for (Entry<MekAnnotationScanner.BaseAnnotationScanner, MekAnnotationScanner.ScanData> entry : scanners.entrySet()) {
         MekAnnotationScanner.ScanData scannerData = entry.getValue();
         Map<Class<?>, List<AnnotationData>> knownClasses = scannerData.knownClasses;
         if (!knownClasses.isEmpty()) {
            try {
               entry.getKey().collectScanData(classNameCache, knownClasses, scannerData.modFileData);
            } catch (Throwable var8) {
               Mekanism.logger.error("Failed to collect scan data", var8);
            }
         }
      }
   }

   private static void gatherScanData(
      Map<ElementType, List<MekAnnotationScanner.ScanData>> elementBasedScanData,
      Map<String, Class<?>> classNameCache,
      AnnotationData data,
      List<IModFileInfo> modFileData
   ) {
      ElementType targetType = data.targetType();

      for (MekAnnotationScanner.ScanData scannerData : elementBasedScanData.getOrDefault(targetType, Collections.emptyList())) {
         for (Type type : scannerData.supportedTypes.get(targetType)) {
            if (type.equals(data.annotationType())) {
               Class<?> clazz = getClassForName(classNameCache, data.clazz().getClassName());
               if (clazz != null) {
                  scannerData.knownClasses.computeIfAbsent(clazz, c -> new ArrayList<>()).add(data);
                  scannerData.modFileData.addAll(modFileData);
               }

               return;
            }
         }
      }
   }

   private static void addScanningSupport(
      Map<MekAnnotationScanner.BaseAnnotationScanner, MekAnnotationScanner.ScanData> scanners,
      Map<ElementType, List<MekAnnotationScanner.ScanData>> elementBasedScanData,
      MekAnnotationScanner.BaseAnnotationScanner... baseScanners
   ) {
      for (MekAnnotationScanner.BaseAnnotationScanner baseScanner : baseScanners) {
         if (baseScanner.isEnabled()) {
            MekAnnotationScanner.ScanData scanData = new MekAnnotationScanner.ScanData(baseScanner);
            scanners.put(baseScanner, scanData);

            for (ElementType elementType : scanData.supportedTypes.keySet()) {
               elementBasedScanData.computeIfAbsent(elementType, type -> new ArrayList<>()).add(scanData);
            }
         }
      }
   }

   @Nullable
   private static Class<?> getClassForName(Map<String, Class<?>> classNameCache, String className) {
      if (classNameCache.containsKey(className)) {
         return classNameCache.get(className);
      } else {
         Class<?> clazz;
         try {
            clazz = Class.forName(className);
         } catch (ClassNotFoundException var4) {
            Mekanism.logger.error("Failed to find class '{}'", className);
            clazz = null;
         } catch (NoClassDefFoundError var5) {
            Mekanism.logger.error("Failed to load class '{}'", className);
            throw var5;
         }

         classNameCache.put(className, clazz);
         return clazz;
      }
   }

   public abstract static class BaseAnnotationScanner {
      protected boolean isEnabled() {
         return true;
      }

      protected abstract Map<ElementType, Type[]> getSupportedTypes();

      protected abstract void collectScanData(
         Map<String, Class<?>> classNameCache, Map<Class<?>, List<AnnotationData>> knownClasses, Set<IModFileInfo> modFileData
      );

      @Nullable
      protected static Class<?> getAnnotationValue(Map<String, Class<?>> classNameCache, AnnotationData data, String key) {
         Type type = (Type)data.annotationData().get(key);
         return type == null ? null : MekAnnotationScanner.getClassForName(classNameCache, type.getClassName());
      }

      protected static <T> T getAnnotationValue(AnnotationData data, String key, T defaultValue) {
         return data.annotationData().getOrDefault(key, defaultValue);
      }

      protected static <T extends Enum<T>> T getAnnotationValue(AnnotationData data, String key, T defaultValue) {
         Map<String, Object> annotationData = data.annotationData();
         if (!annotationData.containsKey(key)) {
            return defaultValue;
         } else if (annotationData.get(key) instanceof EnumHolder enumHolder) {
            try {
               return Enum.valueOf(defaultValue.getDeclaringClass(), enumHolder.getValue());
            } catch (IllegalArgumentException var7) {
               Mekanism.logger.error("Could not find enum value of: {}. Defaulting.", enumHolder.getValue());
               return defaultValue;
            }
         } else {
            Mekanism.logger.warn("Unknown property value for enum should have been an enum holder. Defaulting.");
            return defaultValue;
         }
      }

      protected static <T> T getAnnotationValue(AnnotationData data, String key, T defaultValue, Predicate<T> validator) {
         Map<String, Object> annotationData = data.annotationData();
         if (annotationData.containsKey(key)) {
            T value = (T)annotationData.get(key);
            if (validator.test(value)) {
               return value;
            }
         }

         return defaultValue;
      }

      protected static <T extends Enum<T>> T getAnnotationValue(AnnotationData data, String key, T defaultValue, Predicate<T> validator) {
         Map<String, Object> annotationData = data.annotationData();
         if (annotationData.containsKey(key)) {
            if (annotationData.get(key) instanceof EnumHolder enumHolder) {
               try {
                  T returnValue = Enum.valueOf(defaultValue.getDeclaringClass(), enumHolder.getValue());
                  if (validator.test(returnValue)) {
                     return returnValue;
                  }
               } catch (IllegalArgumentException var8) {
                  Mekanism.logger.error("Could not find enum value of: {}. Defaulting.", enumHolder.getValue());
               }
            } else {
               Mekanism.logger.warn("Unknown property value for enum should have been an enum holder. Defaulting.");
            }
         }

         return defaultValue;
      }

      @Nullable
      protected static Field getField(Class<?> annotatedClass, String fieldName) {
         Field field;
         try {
            field = annotatedClass.getDeclaredField(fieldName);
         } catch (NoSuchFieldException var4) {
            Mekanism.logger.error("Failed to find field '{}' for class '{}'", fieldName, annotatedClass.getSimpleName());
            return null;
         }

         field.setAccessible(true);
         return field;
      }

      @Nullable
      protected static Method getMethod(Class<?> annotatedClass, String methodName, String methodDescriptor) {
         MethodType methodType;
         try {
            methodType = MethodType.fromMethodDescriptorString(methodDescriptor, annotatedClass.getClassLoader());
         } catch (TypeNotPresentException | IllegalArgumentException var7) {
            Mekanism.logger.error("Failed to generate method type. {}", var7.getMessage());
            return null;
         }

         Method method;
         try {
            method = annotatedClass.getDeclaredMethod(methodName, methodType.parameterList().toArray(new Class[0]));
         } catch (NoSuchMethodException var6) {
            Mekanism.logger
               .error(
                  "Failed to find method '{}' with descriptor '{}' for class '{}'", new Object[]{methodName, methodDescriptor, annotatedClass.getSimpleName()}
               );
            return null;
         }

         method.setAccessible(true);
         return method;
      }

      protected static <DATA> DATA getData(Map<Class<?>, DATA> map, Class<?> clazz, DATA empty) {
         Class<?> current = clazz;

         while (current.getSuperclass() != null) {
            current = current.getSuperclass();
            DATA superCache = map.get(current);
            if (superCache != null) {
               return superCache;
            }
         }

         return empty;
      }

      protected static <INFO> List<MekAnnotationScanner.BaseAnnotationScanner.ClassBasedInfo<INFO>> combineWithParents(Map<Class<?>, List<INFO>> flatMap) {
         Map<Class<?>, List<INFO>> map = new Object2ObjectOpenHashMap();

         for (Entry<Class<?>, List<INFO>> entry : flatMap.entrySet()) {
            Class<?> clazz = entry.getKey();
            List<INFO> info = entry.getValue();
            Class<?> current = clazz;

            while (current.getSuperclass() != null) {
               current = current.getSuperclass();
               List<INFO> superInfo = map.get(current);
               if (superInfo != null) {
                  info.addAll(superInfo);
                  break;
               }

               superInfo = flatMap.get(current);
               if (superInfo != null) {
                  info.addAll(superInfo);
               }
            }

            map.put(clazz, info);
         }

         return map.entrySet()
            .stream()
            .map(entryx -> new MekAnnotationScanner.BaseAnnotationScanner.ClassBasedInfo<>((Class<?>)entryx.getKey(), (List<INFO>)entryx.getValue()))
            .sorted(Comparator.comparing(MekAnnotationScanner.BaseAnnotationScanner.ClassBasedInfo::className))
            .toList();
      }

      protected record ClassBasedInfo<INFO>(Class<?> clazz, String className, List<INFO> infoList) {
         public ClassBasedInfo(Class<?> clazz, List<INFO> infoList) {
            this(clazz, clazz.getName(), infoList);
         }
      }
   }

   private static class ScanData {
      private final Map<Class<?>, List<AnnotationData>> knownClasses = new Object2ObjectOpenHashMap();
      private final Set<IModFileInfo> modFileData = new HashSet<>();
      private final Map<ElementType, Type[]> supportedTypes;

      public ScanData(MekAnnotationScanner.BaseAnnotationScanner scanner) {
         this.supportedTypes = scanner.getSupportedTypes();
      }
   }
}

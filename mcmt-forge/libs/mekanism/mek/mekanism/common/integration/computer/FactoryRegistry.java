package mekanism.common.integration.computer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.common.Mekanism;
import net.minecraftforge.common.util.Lazy;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FactoryRegistry {
   private static final Map<Class<?>, Lazy<? extends ComputerMethodFactory<?>>> factories = new HashMap<>();
   public static final Map<Class<?>, Lazy<? extends ComputerMethodFactory<?>>> interfaceFactories = new HashMap<>();
   private static final Map<Class<?>, List<Class<?>>> superClasses = new HashMap<>();
   private static final Map<Class<?>, List<? extends ComputerMethodFactory<?>>> hierarchyHandlers = new HashMap<>();

   public static void load() {
      boolean hasRegistry = false;

      for (IComputerMethodRegistry registry : ServiceLoader.load(IComputerMethodRegistry.class)) {
         registry.register();
         hasRegistry = true;
      }

      if (!hasRegistry) {
         Mekanism.logger.error("Expected to find at least one IComputerMethodRegistry, but didn't find any");
      }
   }

   public static <T> void register(Class<T> subject, Supplier<ComputerMethodFactory<T>> factorySupplier, Class<?>... parents) {
      factories.put(subject, Lazy.of(factorySupplier));
      if (parents != null && parents.length > 0) {
         superClasses.put(subject, Arrays.asList(parents));
      }
   }

   public static <T> void registerInterface(Class<T> subject, Supplier<ComputerMethodFactory<T>> factorySupplier) {
      interfaceFactories.put(subject, Lazy.of(factorySupplier));
   }

   public static void bindTo(BoundMethodHolder holder, @NotNull Object subject) {
      bindTo(holder, subject, subject.getClass());
   }

   public static void bindTo(BoundMethodHolder holder, @Nullable Object subject, @NotNull Class<?> subjectClass) {
      for (ComputerMethodFactory computerMethodFactory : getHandlersForHierarchy(subjectClass)) {
         computerMethodFactory.bindTo(subject, holder);
      }

      for (Entry<Class<?>, Lazy<? extends ComputerMethodFactory<?>>> interfaceEntry : interfaceFactories.entrySet()) {
         if (interfaceEntry.getKey().isAssignableFrom(subjectClass)) {
            ComputerMethodFactory computerMethodFactory = (ComputerMethodFactory)interfaceEntry.getValue().get();
            computerMethodFactory.bindTo(subject, holder);
         }
      }
   }

   public static Map<Class<?>, List<MethodHelpData>> getHelpData() {
      return Stream.of(factories.entrySet().stream(), interfaceFactories.entrySet().stream())
         .flatMap(s -> s.map(entry -> Pair.of((Class)entry.getKey(), ((ComputerMethodFactory)((Lazy)entry.getValue()).get()).getHelpData())))
         .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (a, b) -> {
            a.addAll(b);
            return a;
         }, () -> new TreeMap<>(Comparator.comparing(Class::getName))));
   }

   private static synchronized List<? extends ComputerMethodFactory<?>> getHandlersForHierarchy(Class<?> target) {
      List<? extends ComputerMethodFactory<?>> handlers = hierarchyHandlers.get(target);
      if (handlers != null) {
         return handlers;
      } else {
         handlers = buildHandlersForHierarchy(target);
         hierarchyHandlers.put(target, handlers);
         return handlers;
      }
   }

   private static List<? extends ComputerMethodFactory<?>> buildHandlersForHierarchy(Class<?> target) {
      if (factories.containsKey(target)) {
         List<ComputerMethodFactory<?>> outList = new ArrayList<>();

         for (Class<?> aClass : superClasses.getOrDefault(target, Collections.emptyList())) {
            Lazy<? extends ComputerMethodFactory<?>> computerMethodFactoryLazy = factories.get(aClass);
            if (computerMethodFactoryLazy != null) {
               outList.add((ComputerMethodFactory<?>)computerMethodFactoryLazy.get());
            }
         }

         outList.add((ComputerMethodFactory<?>)factories.get(target).get());
         return outList;
      } else {
         Class<?> parent = target.getSuperclass();
         return parent != Object.class && parent != null ? getHandlersForHierarchy(parent) : Collections.emptyList();
      }
   }
}

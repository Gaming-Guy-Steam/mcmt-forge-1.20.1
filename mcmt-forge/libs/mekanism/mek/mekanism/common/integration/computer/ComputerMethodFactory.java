package mekanism.common.integration.computer;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ComputerMethodFactory<T> {
   protected static String[] NO_STRINGS = new String[0];
   protected static Class<?>[] NO_CLASSES = new Class[0];
   private static final Comparator<MethodData<?>> METHODDATA_COMPARATOR = Comparator.comparing(MethodData::name).thenComparing(md -> md.argumentNames().length);
   protected static Lookup lookup = MethodHandles.lookup();
   private final List<MethodData<T>> methods = new ArrayList<>();
   private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

   protected static MethodHandle getMethodHandle(Class<?> containingClass, String methodName, Class<?>... params) {
      try {
         Method method = containingClass.getDeclaredMethod(methodName, params);
         method.setAccessible(true);
         return lookup.unreflect(method);
      } catch (ReflectiveOperationException var4) {
         throw new RuntimeException("Couldn't get method handle for " + methodName, var4);
      }
   }

   protected static MethodHandle getGetterHandle(Class<?> containingClass, String fieldName) {
      try {
         Field field = containingClass.getDeclaredField(fieldName);
         field.setAccessible(true);
         return lookup.unreflectGetter(field);
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException("Couldn't get getter MethodHandle for " + fieldName, var3);
      }
   }

   protected void register(MethodData.Builder<T> methodData) {
      this.register(methodData.build());
   }

   protected void register(MethodData<T> methodData) {
      if (!this.methodsKnown.add(new ObjectIntImmutablePair(methodData.name(), methodData.argumentNames().length))) {
         throw new RuntimeException("Duplicate method name " + methodData.name() + "_" + methodData.argumentNames().length);
      } else {
         this.methods.add(methodData);
      }
   }

   void bindTo(@Nullable T subject, BoundMethodHolder holder) {
      WeakReference<T> weakSubject = subject != null ? new WeakReference<>(subject) : null;

      for (MethodData<T> methodData : this.methods) {
         if (methodData.supports(subject)) {
            holder.register(methodData, weakSubject, false);
         }
      }
   }

   public List<MethodHelpData> getHelpData() {
      return this.methods.stream().sorted(METHODDATA_COMPARATOR).map(MethodHelpData::from).collect(Collectors.toList());
   }

   @FunctionalInterface
   public interface ComputerFunctionCaller<T> {
      Object apply(@Nullable T t, BaseComputerHelper u) throws ComputerException;
   }
}

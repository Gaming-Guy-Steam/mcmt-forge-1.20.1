package mekanism.common.integration.computer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public abstract class BoundMethodHolder {
   private static final Comparator<BoundMethodHolder.BoundMethodData<?>> METHODDATA_COMPARATOR = Comparator.comparing(BoundMethodHolder.BoundMethodData::name)
      .thenComparing(md -> md.argumentNames().length);
   private static final MethodData<ListMultimap<String, BoundMethodHolder.BoundMethodData<?>>> HELP_METHOD = MethodData.builder(
         "help", BoundMethodHolder::generateHelp
      )
      .returnType(Map.class)
      .returnExtra(String.class, MethodHelpData.class)
      .build();
   private static final MethodData<ListMultimap<String, BoundMethodHolder.BoundMethodData<?>>> HELP_METHOD_WITH_NAME = MethodData.builder(
         "help", BoundMethodHolder::generateHelpSpecific
      )
      .returnType(Map.class)
      .returnExtra(String.class, MethodHelpData.class)
      .arguments(new String[]{"methodName"}, new Class[]{String.class})
      .build();
   protected final ListMultimap<String, BoundMethodHolder.BoundMethodData<?>> methods = ArrayListMultimap.create();
   private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();
   protected Lazy<String[]> methodNames = Lazy.of(() -> (String[])this.methods.keys().toArray(new String[0]));

   protected BoundMethodHolder() {
      this.register(HELP_METHOD, new WeakReference<>(this.methods), true);
      this.register(HELP_METHOD_WITH_NAME, new WeakReference<>(this.methods), true);
   }

   public <T> void register(MethodData<T> method, @Nullable WeakReference<T> subject, boolean isHelpMethod) {
      if (!this.methodsKnown.add(new ObjectIntImmutablePair(method.name(), method.argumentNames().length))) {
         throw new RuntimeException("Duplicate method name " + method.name() + "_" + method.argumentNames().length);
      } else {
         this.methods.put(method.name(), new BoundMethodHolder.BoundMethodData<>(method, subject, isHelpMethod));
      }
   }

   public static Object generateHelp(ListMultimap<String, BoundMethodHolder.BoundMethodData<?>> methods, BaseComputerHelper helper) {
      if (methods == null) {
         return helper.voidResult();
      } else {
         Map<String, MethodHelpData> helpItems = new HashMap<>();
         methods.values()
            .stream()
            .sorted(METHODDATA_COMPARATOR)
            .forEach(
               md -> helpItems.put(md.name() + "(" + String.join(", ", md.argumentNames()) + ")", MethodHelpData.from((BoundMethodHolder.BoundMethodData<?>)md))
            );
         return helper.convert(helpItems, helper::convert, helper::convert);
      }
   }

   public static Object generateHelpSpecific(ListMultimap<String, BoundMethodHolder.BoundMethodData<?>> methods, BaseComputerHelper helper) throws ComputerException {
      if (methods == null) {
         return helper.voidResult();
      } else {
         String methodName = helper.getString(0);
         Map<String, MethodHelpData> helpItems = new HashMap<>();
         methods.values()
            .stream()
            .sorted(METHODDATA_COMPARATOR)
            .filter(md -> md.name().equalsIgnoreCase(methodName))
            .forEach(
               md -> helpItems.put(md.name() + "(" + String.join(", ", md.argumentNames()) + ")", MethodHelpData.from((BoundMethodHolder.BoundMethodData<?>)md))
            );
         return helpItems.isEmpty() ? helper.convert("Method name not found: " + methodName) : helper.convert(helpItems, helper::convert, helper::convert);
      }
   }

   public record BoundMethodData<T>(MethodData<T> method, @Nullable WeakReference<T> subject, boolean isHelpMethod) {
      public Object call(BaseComputerHelper helper) throws ComputerException {
         return this.method.handler().apply(this.unwrappedSubject(), helper);
      }

      @Nullable
      private T unwrappedSubject() {
         return this.subject == null ? null : this.subject.get();
      }

      public String name() {
         return this.method.name();
      }

      public boolean threadSafe() {
         return this.method.threadSafe();
      }

      public String[] argumentNames() {
         return this.method.argumentNames();
      }

      public Class<?>[] argClasses() {
         return this.method.argClasses();
      }

      public Class<?> returnType() {
         return this.method.returnType();
      }

      public ComputerMethodFactory.ComputerFunctionCaller<T> handler() {
         return this.method.handler();
      }

      @Override
      public boolean equals(Object o) {
         return this == o ? true : o instanceof BoundMethodHolder.BoundMethodData<?> that && this.method.equals(that.method) && this.subjectEquals(that);
      }

      private boolean subjectEquals(BoundMethodHolder.BoundMethodData<?> that) {
         T mySubject = this.unwrappedSubject();
         Object otherSubject = that.unwrappedSubject();
         return this.isHelpMethod ? mySubject == otherSubject : Objects.equals(mySubject, otherSubject);
      }

      @Override
      public int hashCode() {
         int result = this.method.hashCode();
         T subject = this.unwrappedSubject();
         return 31 * result + (subject != null ? subject.hashCode() : 0);
      }
   }
}

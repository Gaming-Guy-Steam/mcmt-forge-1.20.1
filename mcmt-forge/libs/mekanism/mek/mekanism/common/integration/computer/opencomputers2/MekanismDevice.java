package mekanism.common.integration.computer.opencomputers2;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import li.cil.oc2.api.bus.device.rpc.RPCDevice;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCMethodGroup;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerEnergyHelper;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class MekanismDevice<TILE extends BlockEntity & IComputerTile> extends BoundMethodHolder implements RPCDevice {
   private final Lazy<List<RPCMethodGroup>> methodGroups = Lazy.of(this::buildMethodGroups);
   private final List<String> name;
   private final WeakReference<TILE> attachedTile;

   public static <TILE extends BlockEntity & IComputerTile> MekanismDevice<TILE> create(TILE tile) {
      MekanismDevice<TILE> device = new MekanismDevice<>(tile);
      FactoryRegistry.bindTo(device, null, ComputerEnergyHelper.class);
      tile.getComputerMethods(device);
      return device;
   }

   public MekanismDevice(TILE tile) {
      this.name = Collections.singletonList(tile.getComputerName());
      this.attachedTile = new WeakReference<>(tile);
   }

   public List<String> getTypeNames() {
      return this.name;
   }

   public List<RPCMethodGroup> getMethodGroups() {
      return (List<RPCMethodGroup>)this.methodGroups.get();
   }

   @Override
   public boolean equals(Object obj) {
      TILE attached = this.attachedTile.get();
      return obj == this
         || obj instanceof MekanismDevice<?> other && attached != null && attached == other.attachedTile.get() && this.methods.equals(other.methods);
   }

   @Override
   public int hashCode() {
      int result = this.methodGroups.hashCode();
      result = 31 * result + this.name.hashCode();
      TILE tile = this.attachedTile.get();
      result = 31 * result + (tile != null ? tile.hashCode() : 0);
      return 31 & result + this.methods.hashCode();
   }

   private List<RPCMethodGroup> buildMethodGroups() {
      return this.methods.keySet().stream().map(key -> {
         List<BoundMethodHolder.BoundMethodData<?>> overloads = this.methods.get(key);
         if (overloads.size() == 1) {
            return new MekanismDevice.Method(key, overloads.get(0));
         } else {
            Set<RPCMethod> set = new HashSet<>();

            for (BoundMethodHolder.BoundMethodData<?> md : overloads) {
               set.add(new MekanismDevice.Method(key, md));
            }

            return new MekanismDevice.MethodGroup(key, set);
         }
      }).toList();
   }

   private static class Method implements RPCMethod {
      private final String name;
      private final BoundMethodHolder.BoundMethodData<?> methodData;
      private final Class<?> returnType;
      private final Lazy<RPCParameter[]> params = Lazy.of(this::buildOCParams);

      private Method(String name, BoundMethodHolder.BoundMethodData<?> methodData) {
         this.name = name;
         this.methodData = methodData;
         this.returnType = BaseComputerHelper.convertType(methodData.returnType());
      }

      public boolean isSynchronized() {
         return !this.methodData.threadSafe();
      }

      public Class<?> getReturnType() {
         return this.returnType;
      }

      public RPCParameter[] getParameters() {
         return (RPCParameter[])this.params.get();
      }

      @Nullable
      public Object invoke(RPCInvocation invocation) throws Throwable {
         return this.methodData.call(new OC2ComputerHelper(invocation));
      }

      public String getName() {
         return this.name;
      }

      private RPCParameter[] buildOCParams() {
         RPCParameter[] parameters = new RPCParameter[this.methodData.argumentNames().length];

         for (int i = 0; i < parameters.length; i++) {
            parameters[i] = new MekanismDevice.Param(this.methodData.argumentNames()[i], BaseComputerHelper.convertType(this.methodData.argClasses()[i]));
         }

         return parameters;
      }

      public Optional<RPCMethod> findOverload(RPCInvocation invocation) {
         return Optional.ofNullable(invocation.getParameters().size() == this.methodData.argumentNames().length ? this : null);
      }

      public Optional<String> getDescription() {
         return Optional.ofNullable(this.methodData.method().methodDescription());
      }
   }

   private static class MethodGroup implements RPCMethodGroup {
      private final String name;
      private final Set<RPCMethod> children;

      private MethodGroup(String name, Set<RPCMethod> children) {
         this.name = name;
         this.children = children;
      }

      public String getName() {
         return this.name;
      }

      public Set<RPCMethod> getOverloads() {
         return this.children;
      }

      public Optional<RPCMethod> findOverload(RPCInvocation invocation) {
         return this.children.stream().filter(m -> m.getParameters().length == invocation.getParameters().size()).findFirst();
      }
   }

   private static class Param implements RPCParameter {
      private final Optional<String> name;
      private final Class<?> returnType;

      private Param(String name, Class<?> returnType) {
         this.name = Optional.of(name);
         this.returnType = returnType;
      }

      public Class<?> getType() {
         return this.returnType;
      }

      public Optional<String> getName() {
         return this.name;
      }
   }
}

package mekanism.common.lib.distribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class Target<HANDLER, TYPE extends Number & Comparable<TYPE>, EXTRA> {
   protected final Collection<HANDLER> handlers;
   protected final Collection<Target.HandlerType<HANDLER, TYPE>> needed;
   private int handlerCount = 0;
   protected EXTRA extra;

   protected Target() {
      this.handlers = new LinkedList<>();
      this.needed = new LinkedList<>();
   }

   protected Target(Collection<HANDLER> allHandlers) {
      this.handlers = Collections.unmodifiableCollection(allHandlers);
      this.needed = new ArrayList<>(allHandlers.size() / 2);
   }

   protected Target(int expectedSize) {
      this.handlers = new ArrayList<>(expectedSize);
      this.needed = new ArrayList<>(expectedSize / 2);
   }

   public void addHandler(HANDLER handler) {
      this.handlers.add(handler);
      this.handlerCount++;
   }

   public int getHandlerCount() {
      return this.handlerCount;
   }

   public void sendRemainingSplit(SplitInfo<TYPE> splitInfo) {
      for (Target.HandlerType<HANDLER, TYPE> recipient : this.needed) {
         this.acceptAmount(recipient.handler(), splitInfo, splitInfo.getRemainderAmount());
      }
   }

   protected abstract void acceptAmount(HANDLER handler, SplitInfo<TYPE> splitInfo, TYPE amount);

   protected abstract TYPE simulate(HANDLER handler, EXTRA extra);

   public void sendPossible(EXTRA toSend, SplitInfo<TYPE> splitInfo) {
      for (HANDLER entry : this.handlers) {
         TYPE amountNeeded = this.simulate(entry, toSend);
         if (amountNeeded.compareTo(splitInfo.getShareAmount()) <= 0) {
            this.acceptAmount(entry, splitInfo, amountNeeded);
         } else {
            this.needed.add(new Target.HandlerType<>(entry, amountNeeded));
         }
      }
   }

   public void shiftNeeded(SplitInfo<TYPE> splitInfo) {
      Iterator<Target.HandlerType<HANDLER, TYPE>> iterator = this.needed.iterator();

      while (iterator.hasNext()) {
         Target.HandlerType<HANDLER, TYPE> needInfo = iterator.next();
         TYPE amountNeeded = needInfo.amount();
         if (amountNeeded.compareTo(splitInfo.getShareAmount()) <= 0) {
            this.acceptAmount(needInfo.handler(), splitInfo, amountNeeded);
            iterator.remove();
         }
      }
   }

   protected record HandlerType<HANDLER, TYPE extends Number & Comparable<TYPE>>(HANDLER handler, TYPE amount) {
   }
}

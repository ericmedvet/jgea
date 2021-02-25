package it.units.malelab.jgea.core.listener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class RobustListenerFactory<E> implements Listener.Factory<E> {

  private static final Logger L = Logger.getLogger(RobustListenerFactory.class.getName());
  private final static int SHUTDOWN_FLAG = -10;

  private final AtomicInteger counter;

  public RobustListenerFactory() {
    counter = new AtomicInteger(0);
  }

  @Override
  public Listener<E> build() {
    return new Listener<E>() {
      @Override
      public void listen(E e) {
        if (counter.get() == SHUTDOWN_FLAG) {
          L.warning("listen() invoked on a shutdown factory");
          return;
        }
        counter.incrementAndGet();
        try {
          innerListen(e);
        } finally {
          synchronized (counter) {
            counter.decrementAndGet();
            counter.notifyAll();
          }
        }
      }
    };
  }

  @Override
  public void shutdown() {
    while (counter.get() > 0) {
      synchronized (counter) {
        try {
          counter.wait();
        } catch (InterruptedException e) {
          //ignore
        }
      }
    }
    counter.set(SHUTDOWN_FLAG);
    innerShutdown();
  }

  protected abstract void innerListen(E e);

  protected abstract void innerShutdown();

}

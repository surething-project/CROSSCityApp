package pt.ulisboa.tecnico.cross.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class QueuingLock {
  // Maximum expected time for the endorsement acquisition protocol.
  private final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

  private final List<Object> queueSeats = Collections.synchronizedList(new ArrayList<>());
  private final Handler queueHandler = new Handler(Looper.getMainLooper());
  private final AtomicBoolean locked = new AtomicBoolean(false);
  private String lockHolder;

  public void lock(String name) {
    if (!locked.compareAndSet(false, true)) {
      Object queueSeat = new Object();
      synchronized (queueSeat) {
        queueSeats.add(queueSeat);
        while (!locked.compareAndSet(false, true)) {
          try {
            queueSeat.wait();
          } catch (InterruptedException ignored) {
          }
        }
        queueSeats.remove(queueSeat);
      }
    }
    lockHolder = name;
    queueHandler.postDelayed(() -> unlock(name), TIMEOUT);
  }

  public void unlock(String name) {
    if (Objects.equals(lockHolder, name) && locked.compareAndSet(true, false)) {
      if (!queueSeats.isEmpty()) {
        Object queueSeat = queueSeats.get(0);
        synchronized (queueSeat) {
          queueSeat.notify();
        }
      }
    }
  }

  public void lockAsync(String name, Runnable runnable) {
    new Thread(
            () -> {
              lock(name);
              runnable.run();
            })
        .start();
  }

  public void unlockAsync(String name) {
    new Thread(() -> unlock(name)).start();
  }

  public boolean locked() {
    return locked.get();
  }
}

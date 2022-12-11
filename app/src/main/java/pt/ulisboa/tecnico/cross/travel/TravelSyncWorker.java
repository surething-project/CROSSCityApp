package pt.ulisboa.tecnico.cross.travel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TravelSyncWorker extends Worker {

  public TravelSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    return TravelManager.get().sync() ? Result.success() : Result.retry();
  }
}

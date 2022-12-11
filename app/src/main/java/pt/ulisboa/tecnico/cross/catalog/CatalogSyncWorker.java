package pt.ulisboa.tecnico.cross.catalog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import timber.log.Timber;

public class CatalogSyncWorker extends Worker {

  public CatalogSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    try {
      CatalogManager.get().sync();
      return Result.success();
    } catch (IOException | InterruptedException e) {
      Timber.e(e, "Catalog synchronization failed.");
      return Result.retry();
    }
  }
}

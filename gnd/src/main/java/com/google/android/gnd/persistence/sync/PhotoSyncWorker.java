/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gnd.persistence.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gnd.persistence.remote.FirestoreStorageManager;
import java.io.File;
import timber.log.Timber;

/**
 * A worker that uploads photos from observations to the FirestoreStorage in the background. The
 * source file and remote destination path are provided in a {@link Data} object. This worker should
 * only run when the device has a network connection.
 */
public class PhotoSyncWorker extends Worker {

  private static final String SOURCE_FILE_PATH_PARAM_KEY = "sourceFilePath";
  private static final String DESTINATION_PATH_PARAM_KEY = "destinationPath";

  private final FirestoreStorageManager storageManager;
  private final String localSourcePath;
  private final String remoteDestinationPath;

  public PhotoSyncWorker(
      @NonNull Context context,
      @NonNull WorkerParameters workerParams,
      FirestoreStorageManager storageManager) {
    super(context, workerParams);
    this.storageManager = storageManager;
    this.localSourcePath = workerParams.getInputData().getString(SOURCE_FILE_PATH_PARAM_KEY);
    this.remoteDestinationPath = workerParams.getInputData().getString(DESTINATION_PATH_PARAM_KEY);
  }

  public static Data createInputData(String sourceFilePath, String destinationPath) {
    return new Data.Builder()
        .putString(SOURCE_FILE_PATH_PARAM_KEY, sourceFilePath)
        .putString(DESTINATION_PATH_PARAM_KEY, destinationPath)
        .build();
  }

  @NonNull
  @Override
  public Result doWork() {
    Timber.d("Attempting photo sync: %s, %s", localSourcePath, remoteDestinationPath);
    File file = new File(localSourcePath);
    if (file.exists()) {
      Timber.d("Starting photo upload: %s, %s", localSourcePath, remoteDestinationPath);
      storageManager.uploadMediaFromFile(new File(localSourcePath), remoteDestinationPath);
      return Result.success();
    } else {
      Timber.e("Photo not found %s, %s", localSourcePath, remoteDestinationPath);
      return Result.failure();
    }
  }
}

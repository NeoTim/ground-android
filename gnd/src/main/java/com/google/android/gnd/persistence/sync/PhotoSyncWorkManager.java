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

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gnd.persistence.local.LocalValueStore;
import javax.inject.Inject;
import javax.inject.Provider;

/** Enqueues photo upload work to be done in the background. */
public class PhotoSyncWorkManager extends BaseWorkManager {

  private final LocalValueStore localValueStore;

  @Inject
  public PhotoSyncWorkManager(
      Provider<WorkManager> workManagerProvider, LocalValueStore localValueStore) {
    super(workManagerProvider);
    this.localValueStore = localValueStore;
  }

  @Override
  Class<PhotoSyncWorker> getWorkerClass() {
    return PhotoSyncWorker.class;
  }

  @Override
  protected NetworkType preferredNetworkType() {
    return localValueStore.shouldUploadMediaOverWifiOnly()
        ? NetworkType.UNMETERED
        : NetworkType.CONNECTED;
  }

  /**
   * Enqueues a worker that uploads selected/captured photo to the remote FirestoreStorage once a
   * network connection is available. The returned {@code Completable} completes immediately as soon
   * as the worker is added to the work queue (not once the sync job completes).
   */
  public void enqueueSyncWorker(@NonNull String localPath, @NonNull String remotePath) {
    Data inputData = PhotoSyncWorker.createInputData(localPath, remotePath);
    OneTimeWorkRequest request = buildWorkerRequest(inputData);
    getWorkManager().enqueue(request);
  }
}

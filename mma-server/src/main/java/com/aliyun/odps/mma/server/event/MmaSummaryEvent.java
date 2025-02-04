/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.odps.mma.server.event;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.aliyun.odps.mma.server.task.TaskProgress;

public class MmaSummaryEvent extends BaseMmaEvent {

  private static final int MAX_NUM_TASKS = 15;

  private int numPendingJobs;
  private int numRunningJobs;
  private int numFailedJobs;
  private int numSucceededJobs;
  private Map<String, TaskProgress> taskToProgress;

  public MmaSummaryEvent(
      int numPendingJobs,
      int numRunningJobs,
      int numFailedJobs,
      int numSucceededJobs,
      Map<String, TaskProgress> taskToProgress) {
    this.numPendingJobs = numPendingJobs;
    this.numRunningJobs = numRunningJobs;
    this.numFailedJobs = numFailedJobs;
    this.numSucceededJobs = numSucceededJobs;
    this.taskToProgress = taskToProgress;
  }

  @Override
  public MmaEventType getType() {
    return MmaEventType.SUMMARY;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Job Summary:\n");
    sb.append("> pending: ").append(numPendingJobs).append("\n\n");
    sb.append("> running: ").append(numRunningJobs).append("\n\n");
    sb.append("> failed: ").append(numFailedJobs).append("\n\n");
    sb.append("> succeeded: ").append(numSucceededJobs).append("\n\n");
    sb.append("Running Tasks:\n");

    for (Entry<String, TaskProgress> entry :
        taskToProgress.entrySet().stream().limit(MAX_NUM_TASKS).collect(Collectors.toList())) {
      sb.append("> task id: ").append(entry.getKey()).append("\n\n");
    }
    if (taskToProgress.entrySet().size() > MAX_NUM_TASKS) {
      sb.append("> ...\n\n");
      sb.append("> total number of running tasks: ").append(taskToProgress.size()).append("\n\n");
    }
    return sb.toString();
  }
}

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

package com.aliyun.odps.mma.server.task;

import java.util.List;

import com.aliyun.odps.mma.config.JobConfiguration;
import com.aliyun.odps.mma.server.action.ActionExecutionContext;
import com.aliyun.odps.mma.server.action.McToMcTableDataTransmissionAction;
import com.aliyun.odps.mma.server.action.McVerificationAction;
import com.aliyun.odps.mma.server.action.VerificationAction;
import com.aliyun.odps.mma.server.job.Job;
import com.aliyun.odps.mma.meta.MetaSource.TableMetaModel;

public class OssToMcTableDataTransmissionTask extends TableDataTransmissionTask {

  public OssToMcTableDataTransmissionTask(
      String id,
      String rootJobId,
      JobConfiguration config,
      TableMetaModel ossTableMetaModel,
      TableMetaModel mcTableMetaModel,
      Job job,
      List<Job> subJobs) {
    super(id, rootJobId, config, ossTableMetaModel, mcTableMetaModel, job, subJobs);
    init();
  }

  private void init() {
    ActionExecutionContext context = new ActionExecutionContext(config);
    String executionProject = config.getOrDefault(
        JobConfiguration.JOB_EXECUTION_MC_PROJECT,
        config.get(JobConfiguration.DEST_CATALOG_NAME));
    McToMcTableDataTransmissionAction action = new McToMcTableDataTransmissionAction(
        id + ".DataTransmission",
        config.get(JobConfiguration.DATA_DEST_MC_ACCESS_KEY_ID),
        config.get(JobConfiguration.DATA_DEST_MC_ACCESS_KEY_SECRET),
        executionProject,
        config.get(JobConfiguration.DATA_DEST_MC_ENDPOINT),
        source,
        dest,
        this,
        context);
    dag.addVertex(action);

    McVerificationAction mcVerificationAction = new McVerificationAction(
        id + ".McDataVerification",
        config.get(JobConfiguration.DATA_DEST_MC_ACCESS_KEY_ID),
        config.get(JobConfiguration.DATA_DEST_MC_ACCESS_KEY_SECRET),
        executionProject,
        config.get(JobConfiguration.DATA_DEST_MC_ENDPOINT),
        source,
        true,
        this,
        context);
    dag.addVertex(mcVerificationAction);

    McVerificationAction ossVerificationAction = new McVerificationAction(
        id + ".OssDataVerification",
        config.get(JobConfiguration.DATA_DEST_MC_ACCESS_KEY_ID),
        config.get(JobConfiguration.DATA_DEST_MC_ACCESS_KEY_SECRET),
        executionProject,
        config.get(JobConfiguration.DATA_DEST_MC_ENDPOINT),
        dest,
        false,
        this,
        context);
    dag.addVertex(ossVerificationAction);

    VerificationAction verificationAction = new VerificationAction(
        id + ".FinalVerification",
        source,
        this,
        context);
    dag.addVertex(verificationAction);

    dag.addEdge(action, mcVerificationAction);
    dag.addEdge(action, ossVerificationAction);
    dag.addEdge(ossVerificationAction, verificationAction);
    dag.addEdge(mcVerificationAction, verificationAction);
  }

  @Override
  void updateMetadata() {
    job.setStatus(this);
  }
}

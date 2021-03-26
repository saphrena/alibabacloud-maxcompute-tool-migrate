/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.aliyun.odps.datacarrier.taskscheduler.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aliyun.odps.datacarrier.taskscheduler.DataSource;
import com.aliyun.odps.datacarrier.taskscheduler.MmaConfig;
import com.aliyun.odps.datacarrier.taskscheduler.MmaException;
import com.aliyun.odps.datacarrier.taskscheduler.meta.MmaMetaManager;

public class AddMigrationJobAction extends DefaultAction {
  private static final Logger LOG = LogManager.getLogger(AddMigrationJobAction.class);

  private MmaConfig.TableMigrationConfig config;
  private MmaMetaManager mmaMetaManager;

  public AddMigrationJobAction(
      String id,
      MmaConfig.TableMigrationConfig config,
      MmaMetaManager mmaMetaManager) {
    super(id);
    this.config = config;
    this.mmaMetaManager = mmaMetaManager;
  }


  @Override
  public Object call() throws MmaException {
    try {
      LOG.info("Add migration job {}", MmaConfig.TableMigrationConfig.toJson(config));
      mmaMetaManager.addMigrationJob(
          DataSource.ODPS,
          actionExecutionContext.getJobId(),
          config);
    } catch (Exception e) {
      LOG.error("Action {} Exception when create table migration job, config {}",
                id, MmaConfig.TableMigrationConfig.toJson(config), e);
      throw new MmaException("Create table migration job for " + id, e);
    }
    return null;
  }

  @Override
  public String getName() { return "Add migration job"; }
}

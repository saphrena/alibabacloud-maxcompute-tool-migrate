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
 *
 */

package com.aliyun.odps.mma.server.action;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aliyun.odps.Odps;
import com.aliyun.odps.mma.config.MmaConfig.OssConfig;
import com.aliyun.odps.mma.exception.MmaException;
import com.aliyun.odps.mma.server.OdpsUtils;
import com.aliyun.odps.mma.server.OssUtils;
import com.aliyun.odps.mma.server.task.Task;
import com.aliyun.odps.mma.util.GsonUtils;

public class OssToMcFunctionAction extends DefaultAction {

  private static final Logger LOG = LogManager.getLogger(OssToMcFunctionAction.class);
  private final OssConfig ossConfig;
  private final String metafile;
  private final Odps odps;
  private final boolean update;

  public OssToMcFunctionAction(
      String id,
      OssConfig ossConfig,
      String metafile,
      Odps odps,
      boolean update,
      Task task,
      ActionExecutionContext context) {
    super(id, task, context);
    this.ossConfig = ossConfig;
    this.metafile = metafile;
    this.odps = odps;
    this.update = update;
  }


  @Override
  void handleResult(Object result) {
  }

  @Override
  public String getName() {
    return "function restoration";
  }

  @Override
  public Object getResult() {
    return null;
  }

  @Override
  public Object call() throws Exception {
    if (!OssUtils.exists(ossConfig, metafile)) {
      throw new MmaException(String.format("ActionId: %s, OSS file %s not found", id, metafile));
    }
    String content = OssUtils.readFile(ossConfig, metafile);
    McFunctionInfo functionInfo = GsonUtils.GSON.fromJson(content, McFunctionInfo.class);
    OdpsUtils.createFunction(odps, odps.getDefaultProject(), functionInfo, update);

    LOG.info("Restore function {} succeed", content);
    return null;
  }

}

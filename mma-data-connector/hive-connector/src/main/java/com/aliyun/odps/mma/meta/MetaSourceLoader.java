/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.aliyun.odps.mma.meta;

import org.apache.hadoop.hive.metastore.api.MetaException;

public class MetaSourceLoader implements MetaLoader{

  public MetaSourceLoader() {

  }

  @Override
  public MetaSource load(MetaConfig config) throws MetaSourceLoadException {
    try {
      HiveMetaConfig hiveMetaConfig = (HiveMetaConfig) config;
      if(hiveMetaConfig.isUseHms()){
        return HiveMetaSourceHmsImpl.getInstance(hiveMetaConfig);
      } else {
        return new HiveMetaSourceJdbcImpl(hiveMetaConfig);
      }
    } catch (MetaException | ClassNotFoundException e) {
      throw new MetaSourceLoadException("ERROR: MetaSource Impl Load Fail", e);
    }
  }

}

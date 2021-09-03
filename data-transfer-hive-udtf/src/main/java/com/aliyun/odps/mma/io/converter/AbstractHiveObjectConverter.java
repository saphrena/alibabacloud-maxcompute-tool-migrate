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

package com.aliyun.odps.mma.io.converter;

import com.aliyun.odps.type.TypeInfo;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;

public abstract class AbstractHiveObjectConverter {

  /**
   * Converts a hive object to an odps object
   * @param objectInspector
   * @param o
   * @param odpsTypeInfo
   * @return odps object
   */
  public abstract Object convert(ObjectInspector objectInspector, Object o, TypeInfo odpsTypeInfo);
}

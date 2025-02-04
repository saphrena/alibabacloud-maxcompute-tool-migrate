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

package com.aliyun.odps.mma.sql;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class SqlCompatibilityChecker {

  /**
   * A factory creating a SqlCompatibilityChecker implementation
   * @param defaultProject the project name which sql is submitted to
   * @param meta the Meta object, including meta in all projects
   * @param defaultSettings the default settings (like set odps.sql.type.system.odps2 = true;)
   * @return a SqlCompatibilityChecker implementation
   */
  public static SqlCompatibilityChecker create(String defaultProject, Meta meta, Map<String, String> defaultSettings) {
    return new SimpleSqlCompatibilityChecker(defaultProject, meta, defaultSettings);
  }

  /**
   * Create a SqlCompatibilityChecker implementation with omitted default settings
   * @param defaultProject the project name which sql is submitted to
   * @param meta the Meta object, including meta in all projects
   * @return a SqlCompatibilityChecker implementation
   */
  public static SqlCompatibilityChecker create(String defaultProject, Meta meta) {
    return create(defaultProject, meta, null);
  }

  /**
   * Check whether a sql is compatible
   * @param settings the sql flags
   * @param sql the sql statement
   * @return compatibility description
   */
  public abstract CompatibilityDescription check(String sql, Map<String, String> settings);

  /**
   * Check whether a sql is compatible or not with omitted settings
   * @param sql the sql statement
   * @return compatibility description
   */
  public CompatibilityDescription check(String sql) {
    return check(sql, null);
  }

  protected final Meta meta;
  protected final Map<String, String> defaultSettings;

  protected SqlCompatibilityChecker(Meta meta, Map<String, String> defaultSettings) {
    this.meta = meta;
    this.defaultSettings = defaultSettings;
  }
}


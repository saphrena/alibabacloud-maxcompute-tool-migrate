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

package com.aliyun.odps.mma.server.ui.jobs;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.ul;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aliyun.odps.mma.config.JobConfiguration;
import com.aliyun.odps.mma.job.JobStatus;
import com.aliyun.odps.mma.server.job.Job;
import com.aliyun.odps.mma.server.job.JobManager;
import com.aliyun.odps.mma.server.task.Task;
import com.aliyun.odps.mma.server.ui.WebUiPage;
import com.aliyun.odps.mma.server.ui.WebUiTab;
import com.aliyun.odps.mma.server.ui.utils.UiUtils;
import com.aliyun.odps.mma.util.Constants;
import j2html.tags.DomContent;

public class JobPage extends WebUiPage {

  private static final Logger LOG = LogManager.getLogger(JobPage.class);

  private static final int SUB_JOB_TABLE_SIZE_LIMIT = 100;

  private WebUiTab parent;
  private JobManager jobManager;

  JobPage(String prefix, JobsTab parent, JobManager jobManager) {
    super(prefix);
    this.parent = parent;
    this.jobManager = Validate.notNull(jobManager);
  }

  @Override
  public String render(HttpServletRequest request) {
    // TODO: root job id
    String parameterRootJobId = request.getParameter(Constants.ROOT_JOB_ID_PARAM);
    String parameterJobId = request.getParameter(Constants.JOB_ID_PARAM);
    if (StringUtils.isBlank(parameterJobId)) {
      throw new IllegalArgumentException(
          String.format("Parameter %s not found", Constants.JOB_ID_PARAM));
    }

    Job job;
    if (StringUtils.isBlank(parameterRootJobId)) {
      job = jobManager.getJobById(parameterJobId);
    } else {
      job = getJobFromRootJob(parameterRootJobId, parameterJobId);
    }

    if (job == null) {
      throw new IllegalArgumentException("Job not found: " + parameterJobId);
    }

    String title = "Details for " + parameterJobId;

    List<DomContent> content = new LinkedList<>();
    // Summary
    content.add(
      div(
          ul(
              li(
                  strong("Status: "),
                  span(job.getStatus().name())
              ),
              li(
                  strong("Object type: "),
                  span(job.getJobConfiguration().get(JobConfiguration.OBJECT_TYPE))
              ),
              li(
                  strong("Source: "),
                  span(UiUtils.getSource(job))
              ),
              li(
                  strong("Destination: "),
                  span(UiUtils.getDestination(job))
              ),
              li(
                  strong("Start time: "),
                  span(UiUtils.formatDate(job.getStartTime()))
              ),
              li(
                  strong("Duration: "),
                  span(UiUtils.formatDuration(job.getStartTime(), job.getEndTime()))
              ),
              li(
                  strong("Info: "),
                  span(StringUtils.defaultString(job.getInfo(), "N/A"))
              )
          ).withClass("unstyled")
      )
    );

    if (job.hasSubJob()) {
      // Sub job table
      // A table job may contain thousands of sub jobs. Thus, display the sub job table only when
      List<Job> subJobs = job.getSubJobs();
      List<Job> subJobsToShow = new LinkedList<>();
      content.add(
          h4("Sub jobs (" + subJobs.size() + ")").withId("jobs")
      );
      if (subJobs.size() > SUB_JOB_TABLE_SIZE_LIMIT) {
        content.add(
            h5("Displaying only 100 sub jobs since the number of sub jobs is too large")
        );
        List<Job> failedSubJobs = subJobs
            .stream()
            .filter(j -> JobStatus.FAILED.equals(j.getStatus()))
            .collect(Collectors.toList());
        if (failedSubJobs.size() > 100) {
          subJobsToShow.addAll(failedSubJobs.subList(0, 100));
        } else {
          subJobsToShow.addAll(failedSubJobs);
          subJobsToShow.addAll(
              subJobs
                  .stream()
                  .filter(j -> !JobStatus.FAILED.equals(j.getStatus()))
                  .limit(100 - failedSubJobs.size())
                  .collect(Collectors.toList())
          );
        }
      } else {
        subJobsToShow.addAll(subJobs);
      }

      String subJobTableParameterPath =
          String.join("/", parent.getBasePath(), parent.getPrefix());
      content.add(
          UiUtils.jobsTable(
              subJobTableParameterPath,
              request,
              "jobs",
              "jobs",
              parameterRootJobId,
              parameterJobId,
              subJobsToShow,
              LOG)
      );
    }

    String taskTableParameterPath =
        String.join("/", parent.getBasePath(), getPrefix());
    List<Task> tasks = job.getTasks();
    if (!tasks.isEmpty()) {
      content.add(
          h4("Tasks (" + tasks.size() + ")").withId("tasks")
      );
      content.add(
          UiUtils.tasksTable(
              taskTableParameterPath,
              parameterRootJobId,
              job.getId(),
              request,
              "tasks",
              "tasks",
              tasks,
              LOG)
      );
    }

    return UiUtils.basicMmaPage(title, content, parent);
  }

  /**
   * Traverse the job tree to find the job. Using breadth-first search to achieve better
   * performance.
   *
   * @return job or null.
   */
  private Job getJobFromRootJob(String rootJobId, String jobId) {
    Job root = jobManager.getJobById(rootJobId);
    Queue<Job> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      Job currJob = queue.remove();
      if (jobId.equals(currJob.getId())) {
        return currJob;
      } else if (currJob.hasSubJob()){
        queue.addAll(currJob.getSubJobs());
      }
    }

    return null;
  }
}

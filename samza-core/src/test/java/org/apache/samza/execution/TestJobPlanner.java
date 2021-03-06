/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.samza.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.samza.application.LegacyTaskApplication;
import org.apache.samza.application.descriptors.ApplicationDescriptorImpl;
import org.apache.samza.config.JobConfig;
import org.apache.samza.config.MapConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestJobPlanner {

  @Test
  public void testJobNameIdConfigGeneration() {
    Map<String, String> testConfig = new HashMap<>();
    testConfig.put("app.name", "samza-app");
    testConfig .put("app.id", "id");
    MapConfig generatedConfig = JobPlanner.generateSingleJobConfig(testConfig);
    Assert.assertEquals(generatedConfig.get("job.name"), "samza-app");
    Assert.assertEquals(generatedConfig.get("job.id"), "id");
  }

  @Test
  public void testAppConfigPrecedence() {
    Map<String, String> testConfig = new HashMap<>();
    testConfig.put("app.name", "samza-app");
    testConfig .put("app.id", "id");
    testConfig .put("job.id", "should-not-exist-id");
    testConfig .put("job.name", "should-not-exist-name");
    MapConfig generatedConfig = JobPlanner.generateSingleJobConfig(testConfig);
    Assert.assertEquals(generatedConfig.get("job.name"), "samza-app");
    Assert.assertEquals(generatedConfig.get("job.id"), "id");
  }

  @Test
  public void testJobNameId() {
    Map<String, String> testConfig = new HashMap<>();
    testConfig .put("job.id", "should-exist-id");
    testConfig .put("job.name", "should-exist-name");
    MapConfig generatedConfig = JobPlanner.generateSingleJobConfig(testConfig);
    Assert.assertEquals(generatedConfig.get("job.name"), "should-exist-name");
    Assert.assertEquals(generatedConfig.get("job.id"), "should-exist-id");
  }

  @Test
  public void testRunIdisConfiguredForAllTypesOfApps() {
    Map<String, String> testConfig = new HashMap<>();
    testConfig.put("app.id", "should-exist-id");
    testConfig.put("app.name", "should-exist-name");

    ApplicationDescriptorImpl applicationDescriptor = Mockito.mock(ApplicationDescriptorImpl.class);

    Mockito.when(applicationDescriptor.getConfig()).thenReturn(new MapConfig(testConfig));
    Mockito.when(applicationDescriptor.getAppClass()).thenReturn(LegacyTaskApplication.class);

    JobPlanner jobPlanner = new JobPlanner(applicationDescriptor) {
      @Override
      public List<JobConfig> prepareJobs() {
        return null;
      }
    };

    ExecutionPlan plan = jobPlanner.getExecutionPlan("custom-run-id");
    Assert.assertNotNull(plan.getApplicationConfig().getRunId(), "custom-run-id");
  }

}

/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.testutil.tool.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.testutil.TestUtilRuntimeException;

/**
 * 
 * 
 * @author SAP AG
 */
public class SupaPerformanceTestClient extends AbstractTestClient {

  private static final String SUPA_CONFIG = "target/classes/performance/Supa-Config.properties";
  private static final Logger LOG = LoggerFactory.getLogger(SupaPerformanceTestClient.class);

  private final SupaController supa;
  private int runsPerTest;
  private int warmupRuns;

  public boolean supaStart;
  private boolean supaExit;
  public String supaHome;
  public String supaConfig;

  SupaPerformanceTestClient(final CallerConfig config, final String supaBaseUrl) throws URISyntaxException {
    super(config);
    supa = new SupaController(supaBaseUrl);
  }

  /**
   * 
   * @return
   */
  public String runMeasurement() {
    if (supaStart) {
      boolean success = startSupa();
      if (!success) {
        throw new TestUtilRuntimeException("SUPA was not started, check SUPA log.");
      }
    }

    List<TestRequest> testRequests = config.getTestRequests();

    for (TestRequest testRequest : testRequests) {
      String scenarioName = supa.getCurrentStepName();
      LOG.info("\n####\nPrepare for test step: {}", scenarioName);
      // run warmup
      if (warmupRuns > 0) {
        LOG.info("Start warmup ({})", warmupRuns);
        call(testRequest, warmupRuns);
      }
      // run test
      for (int i = 1; i <= runsPerTest; i++) {
        LOG.info("Start run '{}/{}'.", i, runsPerTest);
        supa.begin();
        call(testRequest, testRequest.getCallCount());
        String result = supa.stop();

        LOG.info("...result: {}", result);
      }
      supa.nextStep();
    }

    String resultsPath = supa.finish();
    if (supaExit) {
      supa.shutdownSupaServer();
    }
    return resultsPath;
  }

  /**
   * 
   */
  private boolean startSupa() {
    try {
      File supaJarFile = new File(supaHome + "/supaStarter.jar");
      if (!supaJarFile.exists()) {
        throw new IllegalArgumentException("No supaStarter.jar found at " + supaJarFile.getAbsolutePath());
      }
      File supaConfigFile = new File(supaConfig);
      if (!supaConfigFile.exists()) {
        throw new IllegalArgumentException("No configuration for supa found at " + supaConfigFile.getAbsolutePath());
      }

      String execCommand = "java -jar " + supaJarFile.getAbsolutePath() + " " + supaConfigFile.getAbsolutePath() + " -server";
      File tmpDir = File.createTempFile("odata", null).getParentFile();
      LOG.info("Start supa with command {} and temp dir {}", execCommand, tmpDir.getAbsolutePath());

      Process supaServer = Runtime.getRuntime().exec(execCommand, null, tmpDir);
      InputStream in = supaServer.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

      String read = reader.readLine();
      reader.close();

      if (read == null) {
        LOG.info("SUPA Failure, got NULL during read.");
        supaServer.destroy();
        return false;
      } else if (read.contains("Hit Return key to close window ...")) {
        supaServer.destroy();
        LOG.info("SUPA Failure with message: {}", read);
        return false;
      } else if (read.contains("Startup")) {
        LOG.info("SUPA Started...{}", read);
        return true;
      } else {
        throw new RuntimeException("Unknown output: " + read);
      }
    } catch (IOException e) {
      throw new RuntimeException("Exception occured: " + e.getMessage(), e);
    }
  }

  //
  // Builder below
  // 

  public static SupaPerformanceTestClientBuilder create(final CallerConfig config, final String supaBaseUrl) throws URISyntaxException {
    return new SupaPerformanceTestClientBuilder(config, supaBaseUrl);
  }

  public static class SupaPerformanceTestClientBuilder {
    private final SupaPerformanceTestClient client;

    public SupaPerformanceTestClientBuilder(final CallerConfig config, final String supaBaseUrl) throws URISyntaxException {
      client = new SupaPerformanceTestClient(config, supaBaseUrl);
      // set default values
      client.runsPerTest = 3;
      client.warmupRuns = -1;
      client.supaExit = false;
      client.supaStart = false;
      client.supaConfig = SUPA_CONFIG;
    }

    public SupaPerformanceTestClientBuilder runsPerTest(final int runsPerTest) {
      client.runsPerTest = runsPerTest;
      return this;
    }

    public SupaPerformanceTestClientBuilder warmupRuns(final int warmupRuns) {
      client.warmupRuns = warmupRuns;
      return this;
    }

    public SupaPerformanceTestClient build() {
      return client;
    }

    public SupaPerformanceTestClientBuilder startSupa(final String supaHome) {
      return this.startSupa(supaHome, SUPA_CONFIG);
    }

    public SupaPerformanceTestClientBuilder startSupa(final String supaHome, final String supaConfig) {
      client.supaStart = true;
      client.supaHome = supaHome;
      client.supaConfig = supaConfig;
      return this;
    }

    public SupaPerformanceTestClientBuilder exitSupa() {
      client.supaExit = true;
      return this;
    }
  }
}

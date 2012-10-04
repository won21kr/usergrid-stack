/*******************************************************************************
 * Copyright 2012 Apigee Corporation
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
package org.usergrid.tools;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.usergrid.utils.JsonUtils.mapToFormattedJsonString;

import java.util.Properties;

import me.prettyprint.hector.testutils.EmbeddedServerHelper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.usergrid.management.ManagementService;
import org.usergrid.persistence.EntityManagerFactory;
import org.usergrid.persistence.cassandra.EntityManagerFactoryImpl;
import org.usergrid.persistence.cassandra.Setup;
import org.usergrid.services.ServiceManagerFactory;

public abstract class ToolBase {

	public static final int MAX_ENTITY_FETCH = 100;

	/** Verbose option: -v */
	static final String VERBOSE = "v";

	boolean isVerboseEnabled = false;

	static final Logger logger = LoggerFactory
			.getLogger(ToolBase.class);

	/**
     * 
     */
    protected static final String PATH_REPLACEMENT = "USERGIRD-PATH-BACKSLASH";

	EmbeddedServerHelper embedded = null;

	EntityManagerFactory emf;

	ServiceManagerFactory smf;

	ManagementService managementService;

	Properties properties;

	boolean use_remote = false;

	public void startTool(String[] args) {
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;
		try {
			line = parser.parse(createOptions(), args);
		} catch (ParseException exp) {
			printCliHelp("Parsing failed.  Reason: " + exp.getMessage());
		}

		if (line == null) {
			return;
		}

		if (line.hasOption("remote")) {
			use_remote = true;
		}

		if (line.hasOption("host")) {
			use_remote = true;
			System.setProperty("cassandra.remote.url",
					line.getOptionValue("host"));
		}
		System.setProperty("cassandra.use_remote", Boolean.toString(use_remote));

		if (use_remote) {
			logger.info("Using remote Cassandra instance");
		} else {
			logger.info("Using local Cassandra instance");
		}

		try {
			runTool(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void printCliHelp(String message) {
		System.out.println(message);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar usergrid-tools-0.0.1-SNAPSHOT.jar "
				+ getToolName(), createOptions());
		System.exit(-1);
	}

	public String getToolName() {
		return ClassUtils.getShortClassName(this.getClass());
	}

	@SuppressWarnings("static-access")
	public Options createOptions() {

		Option hostOption = OptionBuilder.withArgName("host").hasArg()
				.withDescription("Cassandra host").create("host");

		Option remoteOption = OptionBuilder.withDescription(
				"Use remote Cassandra instance").create("remote");

		Option verbose = OptionBuilder
				.withDescription(
						"Print on the console an echo of the content written to the file")
				.create(VERBOSE);

		Options options = new Options();
		options.addOption(hostOption);
		options.addOption(remoteOption);
		options.addOption(verbose);

		return options;
	}

	public void startEmbedded() throws Exception {
		// assertNotNull(client);

		String maven_opts = System.getenv("MAVEN_OPTS");
		logger.info("Maven options: " + maven_opts);

		logger.info("Starting Cassandra");
		embedded = new EmbeddedServerHelper();
		embedded.setup();
	}

	public void startSpring() {

		// copy("/testApplicationContext.xml", TMP);

		String[] locations = { "toolsApplicationContext.xml" };
		ApplicationContext ac = new ClassPathXmlApplicationContext(locations);

		AutowireCapableBeanFactory acbf = ac.getAutowireCapableBeanFactory();
		acbf.autowireBeanProperties(this,
				AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		acbf.initializeBean(this, "testClient");

		assertNotNull(emf);
		assertTrue(
				"EntityManagerFactory is instance of EntityManagerFactoryImpl",
				emf instanceof EntityManagerFactoryImpl);

	}

	public void setupCassandra() throws Exception {

		Setup setup = ((EntityManagerFactoryImpl) emf).getSetup();
		logger.info("Setting up Usergrid schema");
		setup.setup();
		logger.info("Usergrid schema setup");

		setup.checkKeyspaces();

		logger.info("Setting up Usergrid management services");

		managementService.setup();

		logger.info("Usergrid management services setup");
	}

	public void teardownEmbedded() {
		logger.info("Stopping Cassandra");
		EmbeddedServerHelper.teardown();
	}

	void setVerbose(CommandLine line) {
		if (line.hasOption(VERBOSE)) {
			isVerboseEnabled = true;
		}
	}

	/**
	 * Log the content in the default logger(info)
	 * 
	 * @param content
	 */
	void echo(String content) {
		if (isVerboseEnabled) {
			logger.info(content);
		}
	}

	/**
	 * Print the object in JSon format.
	 * 
	 * @param obj
	 */
	void echo(Object obj) {
		echo(mapToFormattedJsonString(obj));
	}

	@Autowired
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@Autowired
	public void setServiceManagerFactory(ServiceManagerFactory smf) {
		this.smf = smf;
		logger.info("ManagementResource.setServiceManagerFactory");
	}

	@Autowired
	public void setManagementService(ManagementService managementService) {
		this.managementService = managementService;
	}

	@Autowired
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public abstract void runTool(CommandLine line) throws Exception;

}

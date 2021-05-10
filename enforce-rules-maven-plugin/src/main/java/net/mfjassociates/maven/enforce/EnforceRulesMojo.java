package net.mfjassociates.maven.enforce;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.maven.cli.logging.Slf4jConfiguration;
import org.apache.maven.cli.logging.Slf4jConfigurationFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * Goal which calls the maven-enforcer-plugin enforce goal to process the rules from the command line
 */
@Mojo(name = "enforce-rules", defaultPhase = LifecyclePhase.VALIDATE)
public class EnforceRulesMojo extends AbstractMojo {

    /**
     * maven-enforcer-plugin version to execute.
     */
    @Parameter (property="version", required = true)
    private String version;
    
    /**
     * Absolute path of the XML file that contains only the maven-enforcer-plugin rules configuration 
     */
    
    @Parameter (property="xmlConfiguration", required = true)
    private String configurationFilename;

    /**
     * The project currently being build.
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    /**
     * The current Maven session.
     */
    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    /**
     * The Maven BuildPluginManager component.
     */
    @Component
    private BuildPluginManager pluginManager;

    /**
     * Disable logging on executed mojos
     */
    @Parameter ( defaultValue = "false")
    private boolean quiet;

    /**
     * Ignore injected maven projetc
     */
    @Parameter ( defaultValue = "false")
    private boolean ignoreMavenProject;


	public void execute() throws MojoExecutionException {

        getLog().info("Executing with maven project " + mavenProject + " for session " + mavenSession);

        if ( quiet )
        {
            disableLogging();
        }
        Xpp3Dom confiuration=null;
        try {
			confiuration = Xpp3DomBuilder.build(new FileReader(configurationFilename));
		} catch (org.codehaus.plexus.util.xml.pull.XmlPullParserException | IOException e) {
			throw new MojoExecutionException("Unable to locate configuration xml file "+configurationFilename, e);
		}
        executeMojo(
        	plugin(groupId("org.apache.maven.plugins"), artifactId("maven-enforcer-plugin"), version(version)),
        	goal("enforce"),
        	confiuration,
            (ignoreMavenProject ?
                executionEnvironment( mavenSession, pluginManager) :
                executionEnvironment( mavenProject, mavenSession, pluginManager)));
	}
    private void disableLogging() throws MojoExecutionException {
        // Maven < 3.1
        Logger logger;
        try {
            logger = (Logger) FieldUtils.readField(getLog(), "logger", true);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Unable to access logger field ", e);
        }
        logger.setThreshold(5);

        // Maven >= 3.1
        ILoggerFactory slf4jLoggerFactory = LoggerFactory.getILoggerFactory();
        Slf4jConfiguration slf4jConfiguration = Slf4jConfigurationFactory.getConfiguration(slf4jLoggerFactory);
        slf4jConfiguration.setRootLoggerLevel(Slf4jConfiguration.Level.ERROR);
        slf4jConfiguration.activate();
    }
}

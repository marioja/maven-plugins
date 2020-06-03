package net.mfjassociates.maven.mojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class SCMInfoMojoTest {
	
	SCMInfoMojo tested = new SCMInfoMojo();
	private static final String URL = "http://svnserver/repos/app1/branches/test-scm/trunk";
	private static final String SCM_URL = "scm:svn:http://svnserver/repos/app1/branches/test-scm/trunk";

	@Test
	void testExecute() throws NoSuchFieldException, IllegalAccessException, MojoExecutionException, MojoFailureException, IOException {
		MavenProject project=new MavenProject();
		Path tempDir = Files.createTempDirectory("scminfo");
		Path tempPom = tempDir.resolve("pom.xml");
		Files.copy(Paths.get("src/test/resources/pom.xml"), tempPom, StandardCopyOption.REPLACE_EXISTING);
		project.setFile(tempPom.toFile());
		setDeclaredField(tested, "project", project);
		setDeclaredField(tested, "scmtype", "svn");
		tested.execute();
		DefaultModelReader reader = new DefaultModelReader();
		Path tempUpdatedPom=tempDir.resolve(SCMInfoMojo.NEW_POM);
		Model m = reader.read(tempUpdatedPom.toFile(), null);
		assertNotNull(m.getScm(), "Model.getScm()");
		assertEquals(URL, m.getScm().getUrl(), "Model.getScm().getURL()");
		assertEquals(SCM_URL, m.getScm().getConnection(), "Model.getScm().getConnection()");
		assertEquals(SCM_URL, m.getScm().getDeveloperConnection(), "Model.getScm().getDeveloperConnection()");
		tempPom.toFile().delete();
		tempUpdatedPom.toFile().delete();
	}

    private void setDeclaredField( Object pojo, String fieldName, Object propertyValue )
            throws NoSuchFieldException, IllegalAccessException
        {
            Field field = pojo.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( pojo, propertyValue );
        }
}

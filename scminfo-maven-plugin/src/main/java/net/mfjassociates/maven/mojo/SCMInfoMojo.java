package net.mfjassociates.maven.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="getinfo")
public class SCMInfoMojo extends AbstractMojo {
	
	private static final String START = "$URL: ";
	private static final String END = " $";
	private static final String NEW_POM = ".update.pom.xml";
	
    /**
     * The Maven Project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Component(role = ModelBuilder.class)
    private DefaultModelBuilder defaultModelBuilder;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
    	try {
			updatePom();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void updatePom() throws IOException {
		DefaultModelReader reader=new DefaultModelReader();
		Model m=reader.read(project.getFile(), null);
		Scm scm = m.getScm();
		AtomicBoolean updated=new AtomicBoolean(false);
		if (scm!=null) {
			scm.setUrl(reformat(scm.getUrl(), START, END, "", updated));
			scm.setDeveloperConnection(reformat(scm.getDeveloperConnection(), START, END, "scm:svn:", updated));
			scm.setConnection(reformat(scm.getConnection(), START, END, "scm:svn:", updated));
			DefaultModelWriter writer=new DefaultModelWriter();
			File updatedPom=project.getFile().toPath().resolveSibling(NEW_POM).toFile();
			writer.write(updatedPom, null, m);
			project.setPomFile(updatedPom);
			if (updated.get()) getLog().info("Updated pom file for "+m.getArtifactId()+" into "+updatedPom.getName());
		}
	}

	// $URL: http://cbsasvnserver1.omega.dce-eir.net/apps/dcscripts/branches/DevCenterWork/maven/du-template/trunk/pom.xml $
	private String reformat(String svnKeywords, String start, String end, String prefix, AtomicBoolean updated) {
		String result=svnKeywords;
		int lme=svnKeywords.length()-Objects.requireNonNull(end, "end delimiter must not be null").length();
		if (svnKeywords.indexOf(start)==0 && svnKeywords.indexOf(end)==lme) {
			lme=svnKeywords.lastIndexOf("/");
			updated.set(true);
			result=prefix+svnKeywords.substring(Objects.requireNonNull(start, "start delimieter must not be null").length(), lme);
		}
		return result;
	}
	public static void main(String[] args) {
		SCMInfoMojo a = new SCMInfoMojo();
		System.out.println(a.reformat("$URL: http://cbsasvnserver1.omega.dce-eir.net/apps/dcscripts/branches/DevCenterWork/maven/du-template/trunk/pom.xml $", START, END, "", new AtomicBoolean(false)));
	}
}

package net.mfjassociates.maven.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name="getinfo")
public class SCMInfoMojo extends AbstractMojo {
	
	private static final String NEW_POM = ".updated-pom.xml";
	
    /**
     * The Maven Project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Parameter( defaultValue = "svn", readonly = true, required = true )
    private String scmtype;
    
    private enum SUPPORTED_SCMTYPES {
    	svn
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
    	try {
			updatePom();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void updatePom() throws IOException, MojoFailureException {
		SUPPORTED_SCMTYPES myscmtype;
		try {
			myscmtype = SUPPORTED_SCMTYPES.valueOf(scmtype);
		} catch (IllegalArgumentException e) {
			throw new MojoFailureException("The SCM type '"+scmtype+"' is not supported by this mojo");
		}
		DefaultModelReader reader=new DefaultModelReader();
		Model m=reader.read(project.getFile(), null);
		Scm scm = m.getScm();
		AtomicBoolean updated=new AtomicBoolean(false);
		if (scm!=null) {
			switch (myscmtype) {
			case svn:
				updateSVN(scm, updated);
				break;
			// default case is not required because it will fail before getting here if the scmtype is not in the enum
			}
			DefaultModelWriter writer=new DefaultModelWriter();
			File updatedPom=project.getFile().toPath().resolveSibling(NEW_POM).toFile();
			writer.write(updatedPom, null, m);
			project.setPomFile(updatedPom);
			if (updated.get()) getLog().info("Updated pom file for "+m.getArtifactId()+" into "+updatedPom.getName());
		}
	}

	private static final String SVN_START = "$URL: ";
	private static final String SVN_END = " $";
	private void updateSVN(Scm scm, AtomicBoolean updated) {
		scm.setUrl(reformat(scm.getUrl(), SVN_START, SVN_END, "", updated));
		scm.setDeveloperConnection(reformat(scm.getDeveloperConnection(), SVN_START, SVN_END, "scm:svn:", updated));
		scm.setConnection(reformat(scm.getConnection(), SVN_START, SVN_END, "scm:svn:", updated));
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
	public static void main(String[] args) throws MojoFailureException {
		SCMInfoMojo a = new SCMInfoMojo();
		String scmtype="svn";
		try {
			SUPPORTED_SCMTYPES t = SUPPORTED_SCMTYPES.valueOf(scmtype);
		} catch (IllegalArgumentException e) {
			throw new MojoFailureException("The SCM type '"+scmtype+"' is not supported by this mojo");
		}
		System.out.println(a.reformat("$URL: http://cbsasvnserver1.omega.dce-eir.net/apps/dcscripts/branches/DevCenterWork/maven/du-template/trunk/pom.xml $", SVN_START, SVN_END, "", new AtomicBoolean(false)));
	}
}

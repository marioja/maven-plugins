//File touchFile = new File( basedir, "target/touch.txt" );
File buildLog = new File((String) basedir, "build.log")
return buildLog.getText().replace("\r\n","\n").contains("Found Banned Dependency: org.slf4j:slf4j-api:jar:1.7.28");

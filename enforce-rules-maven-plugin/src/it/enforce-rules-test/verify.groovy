//File touchFile = new File( basedir, "target/touch.txt" );
File buildLog = new File((String) basedir, "build.log")
return buildLog.getText().replace("\r\n","\n").contains("""Found Banned Dependency: org.slf4j:slf4j-api:jar:1.7.28
Use 'mvn dependency:tree' to locate the source of the banned dependencies.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE""") && buildLog.getText().replace("\r\n","\n") ==~ /(?s:.)*\[INFO] --- enforce-rules-(?s:.)*\n\[INFO] Executing with maven project(?s:.)*\n\[INFO] ------------------------------------------------------------------------(?s:.)*/;

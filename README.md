eclipse-p2repo-index
====================

Simplest possible Maven plugin (Mojo), to generate raw index.html file when eclipse repositories are built with tycho.
This initial version is just to prevent 404 error when people try to browse repositories from web...
Hence, it doesn't pretend to be nice but useful and efficient, so any graphical contribution is welcome :)

To use it, once packaged, add the following plugin information in the tycho repository plugin : 

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.worldline.mojo</groupId>
			<artifactId>p2repo-index-plugin</artifactId>
			<version>0.2.0-SNAPSHOT</version>
			<executions>
				<execution>
					<id>generate-index</id>
					<phase>package</phase>
					<goals>
						<goal>generate-index</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

On top of that, it can be addressed in command line mode, for instance, for explicit execution in Jenkins Jobs:
```
mvn com.worldline.mojo:p2repo-index-plugin:LATEST:generate-index -DrepositoryPath=<...> -DdocumentationUrl=<...>
```
where
- repositoryPath is to explicitely specify where Update Site to complete is, in case the current execution folder is not in the eclipse-repository maven module or in the parent project
- documentationUrl is to enable link in generated index to propose redirection to effective documentation. If not specified, it will retrieve it as the URL parameter of the currently processed pom.

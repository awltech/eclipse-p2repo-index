eclipse-p2repo-index
====================

Simplest possible Maven plugin (Mojo), to generate raw index.html file when eclipse repositories are built with tycho.
This initial version is just to prevent 404 error when people try to browse repositories from web...
Hence, it doesn't pretend to be nice but useful and efficient, so any graphical contribution is welcome :)

To use it, once packaged, add the following plugin information in the tycho repository plugin : 

<build>
	<plugins>
		<plugin>
			<groupId>com.worldline.sdco.mojos</groupId>
			<artifactId>p2repo-index-plugin</artifactId>
			<version>0.0.1-SNAPSHOT</version>
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

eclipse-p2repo-index
====================

Simplest possible Maven plugin (Mojo), to generate raw index.html file when eclipse repositories are built with tycho.
This initial version is just to prevent 404 error when people try to browse repositories from web...
Hence, it doesn't pretend to be nice but useful and efficient, so any graphical contribution is welcome :)

To use it, once packaged, add the following plugin information in the tycho repository plugin : 

&lt;build&gt;
	&lt;plugins&gt;
		&lt;plugin&gt;
			&lt;groupId&gt;com.worldline.mojo&lt;/groupId&gt;
			&lt;artifactId&gt;p2repo-index-plugin&lt;/artifactId&gt;
			&lt;version&gt;0.0.1-SNAPSHOT&lt;/version&gt;
			&lt;executions&gt;
				&lt;execution&gt;
					&lt;id&gt;generate-index&lt;/id&gt;
					&lt;phase&gt;package&lt;/phase&gt;
					&lt;goals&gt;
						&lt;goal&gt;generate-index&lt;/goal&gt;
					&lt;/goals&gt;
				&lt;/execution&gt;
			&lt;/executions&gt;
		&lt;/plugin&gt;
	&lt;/plugins&gt;
&lt;/build&gt;

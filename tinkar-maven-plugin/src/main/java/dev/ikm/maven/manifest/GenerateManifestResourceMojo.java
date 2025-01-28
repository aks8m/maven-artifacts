/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.maven.manifest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Mojo(name = "generate-manifest-resource", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateManifestResourceMojo extends AbstractMojo {

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Parameter(property = "packagerName",required = true)
    private String packagerName;

    @Parameter(property = "originVersion",required = true)
    private String originVersion;

    @Parameter(property = "originURL",required = true)
    private String originURL;

    @Parameter(property = "originNamespace",required = true)
    private String originNamespace;

    @Parameter(property = "additionalManifestParametersMap")
    private Map<String, String> additionalManifestParametersMap;

    /**
     * Date format string for Packager Data MANIFEST.MF Attribute
     */
    private final static String DATE_FORMAT = "yyyy-MM-dd' T'HH:mm:ss.SSSZ";

    /**
     * MANIFEST.MF Main Attributes
     */
    private final static String PACKAGER_NAME = "Packager-Name";
    private final static String PACKAGER_DATE = "Package-Date";
    private final static String ORIGIN_VERSION = "Origin-Version";
    private final static String ORIGIN_URL = "Origin-URL";
    private final static String ORIGIN_NAMESPACE = "Origin-Namespace";

    /**
     * Executes the Mojo to write a MANIFEST.MF file
     * @throws MojoExecutionException - Exception generated by maven plugin archetype
     */
    @Override
    public void execute() throws MojoExecutionException {

        //Check if originSources isDirectory(), if missing then create it
        if (!outputDirectory.isDirectory()) {
            try {
                Files.createDirectories(outputDirectory.toPath());
            } catch (IOException e) {
                getLog().error(e.getMessage(), e);
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        //Create Path to manifest file itself
        Path manifestMF = outputDirectory.toPath().resolve("MANIFEST.MF");

        //Get the current date and correctly format it
        Date now = new Date();
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        String dateTimeAsString = df.format(now);

        //Write the MANIFEST.MF file
        try(OutputStream outputStream = new FileOutputStream(manifestMF.toFile());
            Writer writer = new OutputStreamWriter(outputStream)){
            writer.write(PACKAGER_NAME + ": " + packagerName + "\n");
            writer.write(PACKAGER_DATE + ": " + dateTimeAsString + "\n");
            writer.write(ORIGIN_VERSION + ": " + originVersion + "\n");
            writer.write(ORIGIN_URL + ": " + originURL + "\n");
            writer.write(ORIGIN_NAMESPACE + ": " + originNamespace + "\n");

            //Handle additional manifest information
            if (!additionalManifestParametersMap.isEmpty()) {
                additionalManifestParametersMap.forEach((key, value) -> {
                    try {
                        writer.write(key + ": " + value + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}

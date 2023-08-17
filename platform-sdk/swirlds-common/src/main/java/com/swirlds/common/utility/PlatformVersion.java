/*
 * Copyright (C) 2022-2023 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swirlds.common.utility;

import static com.swirlds.common.utility.CommonUtils.nullToBlank;

import com.swirlds.base.utility.ToStringBuilder;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.system.SoftwareVersion;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 * Represents a unique platform or core system version number and associated commit identifier. Provides methods to
 * support reading from a version descriptor via a variety of sources.
 */
public final class PlatformVersion implements SoftwareVersion {

    /**
     * Constant representing an empty/zero version.
     */
    public static final PlatformVersion ZERO = new PlatformVersion(SemanticVersion.ZERO, StringUtils.EMPTY);

    /**
     * Constant representing the properties file generated by the {@code git-commit-id-plugin} during the build process.
     */
    private static final String GIT_PROPERTIES_FILE = "git.properties";

    /**
     * Constant representing the key of the version number in the properties file.
     */
    private static final String GIT_BUILD_VERSION = "git.build.version";

    /**
     * Constant representing the key of the commit id in the properties file.
     */
    private static final String GIT_COMMIT_ID = "git.commit.id.full";

    /**
     * The version and license template to be used by the {@link #license()} method.
     */
    private static final String LICENSE_TEMPLATE =
            """
                    Swirlds browser v. %s (commit: %s)
                    (c)2016-2022 Swirlds Inc
                    This is an early alpha version.
                    The Swirlds™ software is covered by one or more patents
                    (see www.swirlds.com/ip). The browser is free to download,
                    to experiment with, and to test in building apps. To deploy
                    or use those apps, contact sales@swirlds.com""";

    private static final long CLASS_ID = 0x7f90a5b72e7dc0b1L;
    private static final int COMMIT_MAX_LENGTH = 200;

    private static final class ClassVersion {
        public static final int ORIGINAL = 1;
    }

    private SemanticVersion versionNumber;
    private String commit;

    /**
     * Needed for {@link com.swirlds.common.constructable.RuntimeConstructable}
     */
    public PlatformVersion() {}

    /**
     * @param versionNumber
     * 		the semantic version number.
     * @param commit
     * 		the Git commit id of this version.
     */
    public PlatformVersion(final SemanticVersion versionNumber, final String commit) {
        this.versionNumber = versionNumber;
        this.commit = commit;
    }

    /**
     * Reads a version descriptor from the {@link Path} specified by the {@code file} argument. If the
     * version descriptor cannot be read, then a {@link InvalidSemanticVersionException} is thrown.
     *
     * @return an instance of {@link PlatformVersion} if a version descriptor was read successfully.
     * @throws IllegalArgumentException
     * 		if the {@code file} argument is a {@code null} reference.
     * @throws InvalidSemanticVersionException
     * 		if a version descriptor cannot be read or an exception occurs while reading the version descriptor.
     */
    public static PlatformVersion fromJarFile(@NonNull final Path file) {
        Objects.requireNonNull(file, "file must not be null");

        try (final JarFile jarFile = new JarFile(file.toFile())) {
            final ZipEntry ze = jarFile.getEntry(GIT_PROPERTIES_FILE);

            if (ze == null || ze.isDirectory()) {
                throw new InvalidSemanticVersionException(
                        String.format("The JAR file did not contain a valid version descriptor: %s", file));
            }

            try (final InputStream inputStream = jarFile.getInputStream(ze)) {
                return fromStream(inputStream);
            }
        } catch (final IOException e) {
            throw new InvalidSemanticVersionException(String.format("Unable to read the JAR file: %s", file), e);
        }
    }

    /**
     * Reads a version descriptor from the {@link InputStream} specified by the {@code stream} argument. If the
     * version descriptor cannot be read, then a {@link InvalidSemanticVersionException} is thrown.
     *
     * @return an instance of {@link PlatformVersion} if a version descriptor was read successfully.
     * @throws IllegalArgumentException
     * 		if the {@code stream} argument is a {@code null} reference.
     * @throws InvalidSemanticVersionException
     * 		if a version descriptor cannot be read or an exception occurs while reading the version descriptor.
     */
    public static PlatformVersion fromStream(@NonNull final InputStream stream) throws IOException {
        Objects.requireNonNull(stream, "stream must not be null");

        final Properties properties = new Properties();
        properties.load(stream);
        return fromProperties(properties);
    }

    /**
     * Reads a version descriptor from the {@link Properties} specified by the {@code properties} argument. If the
     * version descriptor cannot be read, then a {@link InvalidSemanticVersionException} is thrown.
     *
     * @return an instance of {@link PlatformVersion} if a version descriptor was read successfully.
     * @throws IllegalArgumentException
     * 		if the {@code properties} argument is a {@code null} reference.
     * @throws InvalidSemanticVersionException
     * 		if a version descriptor cannot be read or an exception occurs while reading the version descriptor.
     */
    public static PlatformVersion fromProperties(@NonNull final Properties properties) {
        Objects.requireNonNull(properties, "properties must not be null");

        final String versionString = properties.getProperty(GIT_BUILD_VERSION);
        final String commitId = nullToBlank(properties.getProperty(GIT_COMMIT_ID));

        if (versionString == null || versionString.isBlank()) {
            throw new InvalidSemanticVersionException(
                    String.format("The version descriptor is missing the mandatory property: %s", GIT_BUILD_VERSION));
        }

        return new PlatformVersion(SemanticVersion.parse(versionString), commitId);
    }

    /**
     * Searches for a version descriptor using the {@link ClassLoader} specified by the {@code loader} argument. If no
     * version descriptor is found, then a {@link InvalidSemanticVersionException} is thrown.
     *
     * @return an instance of {@link PlatformVersion} if a version descriptor was found.
     * @throws IllegalArgumentException
     * 		if the {@code loader} argument is a {@code null} reference.
     * @throws InvalidSemanticVersionException
     * 		if a version descriptor cannot be found or an exception occurs while reading the version descriptor.
     */
    public static PlatformVersion fromClassLoader(@NonNull final ClassLoader loader) {
        Objects.requireNonNull(loader, "loader must not be null");

        try (final InputStream inputStream = loader.getResourceAsStream(GIT_PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new InvalidSemanticVersionException(
                        "Unable to locate a valid version descriptor via the ClassLoader");
            }

            return fromStream(inputStream);
        } catch (final IOException e) {
            throw new InvalidSemanticVersionException("Failed to read the version descriptor from the class path", e);
        }
    }

    /**
     * Searches for a version descriptor using the {@link ClassLoader} of the {@link PlatformVersion} class. If no
     * version descriptor is found, then a {@link InvalidSemanticVersionException} is thrown.
     *
     * @return an instance of {@link PlatformVersion} if a version descriptor was found.
     * @throws InvalidSemanticVersionException
     * 		if a version descriptor cannot be found or an exception occurs while reading the version descriptor.
     */
    public static PlatformVersion locate() {
        return locate(PlatformVersion.class);
    }

    /**
     * Searches for a version descriptor using the {@link ClassLoader} of the {@code sourceClass} argument. If no
     * version descriptor is found, then a {@link InvalidSemanticVersionException} is thrown.
     *
     * @param sourceClass
     * 		the class whose {@link ClassLoader} is used to search for the version descriptor.
     * @return an instance of {@link PlatformVersion} if a version descriptor was found.
     * @throws IllegalArgumentException
     * 		if the {@code sourceClass} argument is a {@code null} reference.
     * @throws InvalidSemanticVersionException
     * 		if a version descriptor cannot be found or an exception occurs while reading the version descriptor.
     */
    public static PlatformVersion locate(@NonNull final Class<?> sourceClass) {
        Objects.requireNonNull(sourceClass, "sourceClass must not be null");
        final ClassLoader classLoader = sourceClass.getClassLoader();
        return fromClassLoader(classLoader);
    }

    /**
     * Searches for a version descriptor using the {@link ClassLoader} of the {@link PlatformVersion} class. If no
     * version descriptor is found, then the {@link #ZERO} value is returned. This method suppresses all
     * exceptions encountered while searching for the version descriptor.
     *
     * @return an instance of {@link PlatformVersion} if a version descriptor was found; otherwise the
     *        {@code defaultVersion} value.
     * @throws IllegalArgumentException
     * 		if the {@code sourceClass} argument is a {@code null} reference.
     */
    public static PlatformVersion locateOrDefault() {
        return locateOrDefault(ZERO);
    }

    /**
     * Searches for a version descriptor using the {@link ClassLoader} of the {@link PlatformVersion} class. If no
     * version descriptor is found, then the {@code defaultValue} argument is returned. This method suppresses all
     * exceptions encountered while searching for the version descriptor.
     *
     * @param defaultVersion
     * 		the value to be returned if a version descriptor was not found.
     * @return an instance of {@link PlatformVersion} if a version descriptor was found; otherwise the
     *        {@code defaultVersion} value.
     * @throws IllegalArgumentException
     * 		if the {@code sourceClass} argument is a {@code null} reference.
     */
    public static PlatformVersion locateOrDefault(final PlatformVersion defaultVersion) {
        return locateOrDefault(PlatformVersion.class, defaultVersion);
    }

    /**
     * Searches for a version descriptor using the {@link ClassLoader} of the {@code sourceClass} argument. If no
     * version descriptor is found, then the {@code defaultValue} argument is returned. This method suppresses all
     * exceptions encountered while searching for the version descriptor.
     *
     * @param sourceClass
     * 		the class whose {@link ClassLoader} is used to search for the version descriptor.
     * @param defaultVersion
     * 		the value to be returned if a version descriptor was not found.
     * @return an instance of {@link PlatformVersion} if a version descriptor was found; otherwise the
     *        {@code defaultVersion} value.
     * @throws IllegalArgumentException
     * 		if the {@code sourceClass} argument is a {@code null} reference.
     */
    @SuppressWarnings("squid:S1166")
    public static PlatformVersion locateOrDefault(
            @NonNull final Class<?> sourceClass, final PlatformVersion defaultVersion) {
        Objects.requireNonNull(sourceClass, "sourceClass must not be null");
        try {
            return locate(sourceClass);
        } catch (final InvalidSemanticVersionException ignored) {
            return defaultVersion;
        }
    }

    /**
     * Builds a license statement include the version number and commit id from a standard template.
     *
     * @return a multi-line string containing a license statement and version information.
     * @see #LICENSE_TEMPLATE
     */
    public String license() {
        return String.format(LICENSE_TEMPLATE, versionNumber(), commit());
    }

    public SemanticVersion versionNumber() {
        return versionNumber;
    }

    public String commit() {
        return commit;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var that = (PlatformVersion) obj;
        return Objects.equals(this.versionNumber, that.versionNumber) && Objects.equals(this.commit, that.commit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionNumber, commit);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("versionNumber", versionNumber)
                .append("commit", commit)
                .toString();
    }

    @Override
    public long getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getVersion() {
        return ClassVersion.ORIGINAL;
    }

    @Override
    public void serialize(final SerializableDataOutputStream out) throws IOException {
        out.writeSerializable(versionNumber, false);
        out.writeNormalisedString(commit);
    }

    @Override
    public void deserialize(final SerializableDataInputStream in, final int version) throws IOException {
        versionNumber = in.readSerializable(false, SemanticVersion::new);
        commit = in.readNormalisedString(COMMIT_MAX_LENGTH);
    }

    @Override
    public int compareTo(final SoftwareVersion that) {
        if (this == that) {
            return 0;
        }

        if (!(that instanceof PlatformVersion pv)) {
            return -1;
        }

        return new CompareToBuilder()
                .append(versionNumber, pv.versionNumber)
                .append(commit, pv.commit)
                .build();
    }
}

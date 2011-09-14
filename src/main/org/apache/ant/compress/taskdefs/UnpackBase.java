/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ant.compress.taskdefs;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.ant.compress.util.CompressorStreamFactory;
import org.apache.ant.compress.util.StreamHelper;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.taskdefs.Unpack;

/**
 * Expands a resource that has been compressed.
 *
 */
public abstract class UnpackBase extends Unpack {
    private static final int BUFFER_SIZE = 8 * 1024;

    private final String defaultExtension;
    private CompressorStreamFactory factory;

    protected UnpackBase(String defaultExtension,
                         CompressorStreamFactory factory) {
        this(defaultExtension);
        setFactory(factory);
    }

    /**
     * @since Apache Compress Antlib 1.1
     */
    protected UnpackBase(String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    /**
     * @since Apache Compress Antlib 1.1
     */
    protected final void setFactory(CompressorStreamFactory factory) {
        this.factory = factory;
    }

    /**
     * Get the default extension.
     */
    protected final String getDefaultExtension() {
        return defaultExtension;
    }

    /**
     * Implement the gunzipping.
     */
    protected void extract() {
        if (source.lastModified() > dest.lastModified()) {
            log("Expanding " + source.getAbsolutePath() + " to "
                + dest.getAbsolutePath());

            FileOutputStream out = null;
            CompressorInputStream zIn = null;
            InputStream fis = null;
            try {
                out = new FileOutputStream(dest);
                zIn = StreamHelper.getInputStream(factory, srcResource);
                if (zIn == null) {
                    fis = srcResource.getInputStream();
                    zIn = factory.getCompressorStream(new BufferedInputStream(fis));
                }
                IOUtils.copy(zIn, out, BUFFER_SIZE);
            } catch (IOException ioe) {
                String msg = "Problem expanding " + ioe.getMessage();
                throw new BuildException(msg, ioe, getLocation());
            } finally {
                FileUtils.close(fis);
                FileUtils.close(out);
                FileUtils.close(zIn);
            }
        }
    }

    /**
     * Yes, we can.
     */
    protected final boolean supportsNonFileResources() {
        return true;
    }
}

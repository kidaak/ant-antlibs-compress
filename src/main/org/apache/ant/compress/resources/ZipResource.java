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
package org.apache.ant.compress.resources;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.util.Date;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * A Resource representation of an entry in a zipfile.
 */
public class ZipResource extends CommonsCompressArchiveResource {

    private String encoding;
    private ZipExtraField[] extras;

    /**
     * Default constructor.
     */
    public ZipResource() {
    }

    /**
     * Construct a ZipResource representing the specified
     * entry in the specified zipfile.
     * @param z the zipfile as File.
     * @param enc the encoding used for filenames.
     * @param e the ZipEntry.
     */
    public ZipResource(File z, String enc, ZipArchiveEntry e) {
        super(z, e);
        setEncoding(enc);
    }

    /**
     * Construct a ZipResource representing the specified
     * entry in the specified zip archive.
     * @param z the zipfile as File.
     * @param enc the encoding used for filenames.
     * @param e the ZipEntry.
     */
    public ZipResource(Resource z, String enc, ZipArchiveEntry e) {
        super(z, e);
        setEncoding(enc);
    }

    /**
     * Set the zipfile that holds this ZipResource.
     * @param z the zipfile as a File.
     */
    public void setZipfile(File z) {
        setArchive(z);
    }

    /**
     * Set the zipfile that holds this ZipResource.
     * @param z the zipfile as a Resource.
     */
    public void setZipfile(Resource z) {
        addConfigured(z);
    }

    /**
     * Get the zipfile that holds this ZipResource.
     * @return the zipfile as a File or null if it is not a file.
     */
    public File getZipfile() {
        FileProvider fp = (FileProvider) getArchive().as(FileProvider.class);
        return fp != null ? fp.getFile() : null;
    }

    /**
     * Set the encoding to use with the zipfile.
     * @param enc the String encoding.
     */
    public void setEncoding(String enc) {
        checkAttributesAllowed();
        encoding = enc;
    }

    /**
     * Get the encoding to use with the zipfile.
     * @return String encoding.
     */
    public String getEncoding() {
        return isReference()
            ? ((ZipResource) getCheckedRef()).getEncoding() : encoding;
    }

    /**
     * Overrides the super version.
     * @param r the Reference to set.
     */
    public void setRefid(Reference r) {
        if (encoding != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if the zip file cannot be opened,
     *         or the entry cannot be read.
     */
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        File f = getZipfile();
        if (f == null) {
            return super.getInputStream();
        }

        final ZipFile z = new ZipFile(f, getEncoding());
        ZipArchiveEntry ze = z.getEntry(getName());
        if (ze == null) {
            z.close();
            throw new BuildException("no entry " + getName() + " in "
                                     + getArchive());
        }
        return new FilterInputStream(z.getInputStream(ze)) {
            public void close() throws IOException {
                FileUtils.close(in);
                z.close();
            }
            protected void finalize() throws Throwable {
                try {
                    close();
                } finally {
                    super.finalize();
                }
            }
        };
    }

    /**
     * Retrieves extra fields.
     * @return an array of the extra fields
     */
    public ZipExtraField[] getExtraFields() {
        if (extras == null) {
            return new ZipExtraField[0];
        }
        return extras;
    }

    /**
     * fetches information from the named entry inside the archive.
     */
    protected void fetchEntry() {
        File f = getZipfile();
        if (f == null) {
            super.fetchEntry();
            return;
        }

        ZipFile z = null;
        try {
            z = new ZipFile(getZipfile(), getEncoding());
            setEntry(z.getEntry(getName()));
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_DEBUG);
            throw new BuildException(e);
        } finally {
            ZipFile.closeQuietly(z);
        }
    }

    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            extras = ((ZipArchiveEntry) e).getExtraFields();
        }
    }

    protected ArchiveInputStream getArchiveStream(InputStream is)
        throws IOException {
        return new ZipArchiveInputStream(is, getEncoding(), true);
    }

    protected Date getLastModified(ArchiveEntry entry) {
        return new Date(((ZipArchiveEntry) entry).getTime());
    }

    protected int getMode(ArchiveEntry e) {
        return ((ZipArchiveEntry) e).getUnixMode();
    }

    protected String getArchiveType() {
        return "zip";
    }
}
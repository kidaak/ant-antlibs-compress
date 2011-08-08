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

import org.apache.ant.compress.util.ArStreamFactory;
import org.apache.ant.compress.resources.ArFileSet;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.Resource;

/**
 * Creates ar archives.
 */
public class Ar extends ArchiveBase {

    private static final String NO_DIRS_MESSAGE = "ar archives cannot store"
        + " directory entries";

    public Ar() {
        setFactory(new ArStreamFactory());
        setEntryBuilder(
              new ArchiveBase.EntryBuilder() {
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    boolean isDir = r.getResource().isDirectory();
                    if (isDir) {
                        throw new BuildException(NO_DIRS_MESSAGE);
                    }

                    int mode = ArchiveFileSet.DEFAULT_FILE_MODE;
                    if (r.getCollectionFlags().hasModeBeenSet()) {
                        mode = r.getResourceFlags().getMode();
                    }

                    int uid = 0;
                    if (r.getResourceFlags().hasUserIdBeenSet()) {
                        uid = r.getResourceFlags().getUserId();
                    } else if (r.getCollectionFlags().hasUserIdBeenSet()) {
                        uid = r.getCollectionFlags().getUserId();
                    }

                    int gid = 0;
                    if (r.getResourceFlags().hasGroupIdBeenSet()) {
                        gid = r.getResourceFlags().getGroupId();
                    } else if (r.getCollectionFlags().hasGroupIdBeenSet()) {
                        gid = r.getCollectionFlags().getGroupId();
                    }

                    return new ArArchiveEntry(r.getName(),
                                              r.getResource().getSize(),
                                              uid, gid, mode,
                                              round(r.getResource()
                                                    .getLastModified(), 1000)
                                              / 1000);
                }
            });
        setFileSetBuilder(new ArchiveBase.FileSetBuilder() {
                public ArchiveFileSet buildFileSet(Resource dest) {
                    ArchiveFileSet afs = new ArFileSet();
                    afs.setSrcResource(dest);
                    return afs;
                }
            });
    }

    public void setFilesOnly(boolean b) {
        if (!b) {
            throw new BuildException(NO_DIRS_MESSAGE);
        }
    }
}
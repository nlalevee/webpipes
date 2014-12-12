/*
 *  Copyright 2014 WebPipes contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.webpipes.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

public class FileResource extends StreamResource {

    private File file;

    private long timestamp;

    public FileResource(String path) {
        this(new File(path));
    }

    public FileResource(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    protected InputStream fetchStream() throws IOException {
        timestamp = file.lastModified();
        return new FileInputStream(file);
    }

    @Override
    public boolean refresh() throws IOException {
        long newJarTimestamp = file.lastModified();
        boolean update = newJarTimestamp != timestamp;
        if (update) {
            invalidateCachedContent();
        }
        return update;
    }

    @Override
    public Resource resolve(String relativePath) {
        String fullPath = FilenameUtils.getFullPath(file.getAbsolutePath()) + relativePath;
        String normalized = FilenameUtils.normalize(fullPath);
        return new FileResource(new File(normalized));
    }

}
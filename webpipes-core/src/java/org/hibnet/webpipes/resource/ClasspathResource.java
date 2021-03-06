/*
 *  Copyright 2014-2015 WebPipes contributors
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.hibnet.webpipes.WebpipeUtils;

public class ClasspathResource extends StreamResource {

    private String location;

    private ClassLoader classLoader;

    private Class<?> cls;

    private String protocol;

    private long timestamp;

    public ClasspathResource(String path, String location) {
        super(WebpipeUtils.idOf(ClasspathResource.class, location), WebpipeUtils.pathOf(path, "/webpipes/cp", location));
        this.location = location.startsWith("/") ? location : ("/" + location);
    }

    public ClasspathResource(String path, String location, Class<?> cls) {
        this(path, location.startsWith("/") ? location : (WebpipeUtils.getPackageDir(cls) + "/" + location));
        this.cls = cls;
    }

    public ClasspathResource(String path, String location, ClassLoader classLoader) {
        this(path, location);
        this.classLoader = classLoader;
    }

    @Override
    public Resource resolve(String relativePath) {
        return new ClasspathResource(null, URI.create(this.location).resolve(relativePath).getSchemeSpecificPart());
    }

    @Override
    protected InputStream fetchStream() throws IOException {
        URL url = getURL();
        protocol = url.getProtocol();
        File file = null;
        if ("file".equals(url.getProtocol())) {
            file = new File(url.getPath());
        } else if ("jar".equals(url.getProtocol())) {
            String path = url.getPath();
            file = new File(URI.create(path.substring(0, path.indexOf("!"))));
        } else {
            throw new IllegalStateException("Unsupported classpath url: " + url.toExternalForm());
        }
        timestamp = file.lastModified();
        InputStream is = url.openStream();
        if (is == null) {
            throw new IOException("Cannot find the resource '" + location + "' in the classpath");
        }
        return is;
    }

    @Override
    public boolean refresh() throws IOException {
        URL url = getURL();
        String newProtocol = url.getProtocol();
        boolean update = newProtocol != protocol;
        if (update) {
            invalidateOutputCache();
            return true;
        }
        File file = null;
        if ("file".equals(url.getProtocol())) {
            file = new File(url.getPath());
        } else if ("jar".equals(url.getProtocol())) {
            String path = url.getPath();
            file = new File(URI.create(path.substring(0, path.indexOf("!"))));
        } else {
            return false;
        }
        long newJarTimestamp = file.lastModified();
        update = newJarTimestamp != timestamp;
        if (update) {
            invalidateOutputCache();
        }
        return update;
    }

    private URL getURL() throws IOException {
        URL url;
        if (classLoader != null) {
            url = classLoader.getResource(location);
        } else if (cls != null) {
            url = cls.getResource(location);
        } else {
            url = Thread.currentThread().getContextClassLoader().getResource(location);
            if (url == null) {
                url = this.getClass().getResource(location);
            }
        }
        if (url == null) {
            throw new IOException("Cannot find the resource '" + location + "' in the classpath");
        }
        return url;
    }

}

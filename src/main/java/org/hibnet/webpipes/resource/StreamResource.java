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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public abstract class StreamResource extends Resource {

    private Charset encoding = StandardCharsets.UTF_8;

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    @Override
    public String fetchContent() throws IOException {
        try (InputStream is = fetchStream()) {
            return IOUtils.toString(is, encoding);
        }
    }

    protected abstract InputStream fetchStream() throws IOException;

}
/*
 *  Copyright 2015 WebPipes contributors
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
package org.hibnet.webpipes.processor.yui;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

public class YuiCssCompressorProcessor {

    private final class YuiCssCompressorWebpipe extends ProcessingWebpipe {

        private int linebreak;

        private YuiCssCompressorWebpipe(String path, Webpipe webpipe, int linebreak) {
            super(WebpipeUtils.idOf(YuiCssCompressorProcessor.class, webpipe, linebreak), path, "yuicss", webpipe);
            this.linebreak = linebreak;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            String content = getChildWebpipe().getOutput().getContent();
            Writer writer = new StringWriter();
            try {
                YuiProxy.Instance.compressCSS(new StringReader(content), writer, linebreak);
                content = writer.toString();
            } finally {
                writer.close();
            }
            return new WebpipeOutput(content);
        }
    }

    public Webpipe createProcessingWebpipe(String path, Webpipe source, int linebreak) {
        return new YuiCssCompressorWebpipe(path, source, linebreak);
    }

    public ProcessingWebpipeFactory createFactory(final int linebreak) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(String path, Webpipe source) {
                return new YuiCssCompressorWebpipe(path, source, linebreak);
            }
        };
    }

}

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
package org.hibnet.webpipes.processor.sass;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.js.StatelessJsProcessor;

/**
 * A processor using sass engine:
 */
public class SassCssJsProcessor extends StatelessJsProcessor {

    public SassCssJsProcessor() {
        super("sasscssjs");
    }

    @Override
    protected void initEngine() throws Exception {
        eval("var exports = {};");
        evalFromClasspath("/org/hibnet/webpipes/processor/sass/sass-0.5.0.min.js");
    }

    @Override
    public WebpipeOutput process(Webpipe webpipe) throws Exception {
        String content = invokeMethod("exports", "render", webpipe.getOutput().getContent());
        return new WebpipeOutput(content);
    }

}

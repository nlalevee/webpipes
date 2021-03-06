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
package org.hibnet.webpipes.processor.lesscss;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.hibnet.webpipes.processor.less.LessCssProcessor;
import org.junit.Test;

public class LessCssProcessorTest extends AbstractProcessorTest {

    private LessCssProcessor processor = new LessCssProcessor();

    @Test
    public void testFromFolder() throws Exception {
        testFiles(processor, ".css", ".css");
    }

    @Test
    public void shouldFailWhenInvalidLessCssIsProcessed() throws Exception {
        testInvalidFiles(packageDir + "/invalid/*.css", processor);
    }

}

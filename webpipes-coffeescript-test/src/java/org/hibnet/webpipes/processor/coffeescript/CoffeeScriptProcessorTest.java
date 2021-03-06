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
package org.hibnet.webpipes.processor.coffeescript;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.junit.Test;

public class CoffeeScriptProcessorTest extends AbstractProcessorTest {

    private CoffeeScriptProcessor processor = new CoffeeScriptProcessor();

    @Test
    public void testExceptions() throws Exception {
        testInvalidFiles(packageDir + "/exceptions/*.js", processor.createFactory(null));
    }

    @Test
    public void testSimple() throws Exception {
        testFiles(packageDir + "/simple", processor.createFactory(null), ".js", ".js");
    }

    @Test
    public void testAdvanced() throws Exception {
        testFiles(packageDir + "/advanced", processor.createFactory(null), ".js", ".js");
    }

}

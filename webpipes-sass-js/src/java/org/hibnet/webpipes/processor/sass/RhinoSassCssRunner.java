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
import org.hibnet.webpipes.processor.rhino.SimpleRhinoRunner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A processor using sass engine:
 */
public class RhinoSassCssRunner extends SimpleRhinoRunner {

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        evaluate(context, globalScope, "var exports = {};", "initSass");
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/sass/sass-0.5.0.min.js");
    }

    @Override
    protected String run(Webpipe webpipe, Context context, Scriptable scope) throws Exception {
        String content = webpipe.getContent().getMain();
        String script = buildSimpleRunScript("exports.render", content);
        return evaluate(context, scope, script);
    }

}

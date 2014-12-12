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
package org.hibnet.webpipes.processor.rhino;

import java.io.IOException;
import java.net.URI;

import org.hibnet.webpipes.processor.ResourceProcessor;
import org.hibnet.webpipes.resource.ClasspathResource;
import org.hibnet.webpipes.resource.ClasspathResourceFactory;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;
import org.hibnet.webpipes.resource.WebJarResourceFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.ModuleScript;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RhinoBasedProcessor extends ResourceProcessor {

    protected static Resource commonsScript = new ClasspathResource("commons.js", RhinoBasedProcessor.class);

    protected static Resource envScript = new ClasspathResource("env.rhino.min.js", RhinoBasedProcessor.class);

    protected static Resource cycleScript = new ClasspathResource("cycle.js", RhinoBasedProcessor.class);

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private ScriptableObject globalScope;

    protected ResourceFactory resourceFactory;

    public RhinoBasedProcessor(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        Context context = enterContext();
        try {
            globalScope = context.initStandardObjects();
            initScope(context, globalScope);
            globalScope.sealObject();
        } catch (IOException e) {
            throw new RuntimeException("The resources necessary to initialize the processor could not be accessed", e);
        } finally {
            Context.exit();
        }
    }

    private Context enterContext() {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        // TODO redirect errors from System.err to LOG.error()
        context.setErrorReporter(new ToolErrorReporter(false));
        context.setLanguageVersion(Context.VERSION_1_8);
        return context;
    }

    abstract protected void initScope(Context context, ScriptableObject globalScope) throws IOException;

    protected void addCommon(Context context, Scriptable scope) throws IOException {
        evaluate(context, scope, commonsScript);
    }

    protected void addClientSideEnvironment(Context context, Scriptable scope) throws IOException {
        evaluate(context, scope, envScript);
    }

    public void addJSON(Context context, Scriptable scope) throws IOException {
        evaluateFromWebjar(context, scope, "20110223/json2.js");
        evaluate(context, scope, cycleScript);
    }

    @Override
    public String process(Resource resource, String content) throws Exception {
        Context context = enterContext();
        try {
            Scriptable scope = context.newObject(globalScope);
            scope.setPrototype(null);
            scope.setParentScope(globalScope);
            content = process(context, scope, resource, content);
        } finally {
            Context.exit();
        }
        return content;
    }

    abstract protected String process(Context context, Scriptable scope, Resource resource, String content) throws Exception;

    protected <T> T evaluate(Context context, Scriptable scope, String script, String sourceName) {
        @SuppressWarnings("unchecked")
        T result = (T) context.evaluateString(scope, script, sourceName, 1, null);
        return result;
    }

    protected <T> T evaluate(Context context, Scriptable scope, String script) {
        return evaluate(context, scope, script, this.getClass().getSimpleName());
    }

    protected <T> T evaluate(Context context, Scriptable scope, Resource script) throws IOException {
        return evaluate(context, scope, script.getContent(), script.getName());
    }

    protected <T> T evaluateFromClasspath(Context context, Scriptable scope, String path) throws IOException {
        return evaluate(context, scope, resourceFactory.get(ClasspathResourceFactory.TYPE, path));
    }

    protected <T> T evaluateFromWebjar(Context context, Scriptable scope, String path) throws IOException {
        return evaluate(context, scope, resourceFactory.get(WebJarResourceFactory.TYPE, path));
    }

    protected Scriptable setupModule(Context context, Scriptable scope, final Resource moduleResource, String moduleId) {
        RequireBuilder requireBuilder = new RequireBuilder();
        requireBuilder.setSandboxed(false);
        requireBuilder.setModuleScriptProvider(new ModuleScriptProvider() {
            @Override
            public ModuleScript getModuleScript(Context cx, String moduleId, URI moduleUri, URI baseUri, Scriptable paths) throws Exception {
                Script script = cx.compileString(moduleResource.getContent(), moduleResource.getName(), 1, null);
                return new ModuleScript(script, URI.create(moduleResource.getName()), URI.create(moduleResource.getName()));
            }
        });
        Require require = requireBuilder.createRequire(context, scope);
        return require.requireMain(context, moduleId);
    }

    protected NativeObject callModuleFunction(Context context, Scriptable scope, Scriptable module, String functionName, String[] args) {
        Function function = (Function) module.get(functionName, scope);
        return (NativeObject) function.call(context, scope, module, args);
    }

    protected String buildSimpleRunScript(String jsFunction, String content, String... extraArgs) {
        StringBuilder command = new StringBuilder(jsFunction);
        command.append("(");
        command.append(toJSMultiLineString(content));
        if (extraArgs != null) {
            for (String extraArg : extraArgs) {
                command.append(",");
                command.append(extraArg);
            }
        }
        command.append(");");
        return command.toString();
    }

    /**
     * Transforms a java multi-line string into javascript multi-line string. This technique was found at
     * {@link http://stackoverflow.com/questions/805107/multiline-strings-in-javascript/}
     *
     * @param data
     *            a string containing new lines.
     * @return a string which being evaluated on the client-side will be treated as a correct multi-line string.
     */
    protected String toJSMultiLineString(String data) {
        StringBuffer result = new StringBuffer("[");
        if (data != null) {
            String[] lines = data.split("\n");
            if (lines.length == 0) {
                result.append("\"\"");
            }
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                result.append("\"");
                result.append(line.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\r|\\n", ""));
                // this is used to force a single line to have at least one new line (otherwise cssLint fails).
                if (lines.length == 1) {
                    result.append("\\n");
                }
                result.append("\"");
                if (i < lines.length - 1) {
                    result.append(",");
                }
            }
        }
        result.append("].join(\"\\n\")");
        return result.toString();
    }

}
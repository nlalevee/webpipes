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
package org.hibnet.webpipes.processor.less;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;
import org.hibnet.webpipes.resource.Resource;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

/**
 * Yet another processor which compiles less to css. This implementation uses open source java library called less4j.
 */
public class Less4jProcessor implements ProcessingWebpipeFactory {

    private static final LessCompiler compiler = new DefaultLessCompiler();

    @Override
    public Webpipe createProcessingWebpipe(String path, Webpipe source) {
        return new Less4jWebpipe(path, source);
    }

    private static class Less4jWebpipe extends ProcessingWebpipe {

        private List<Resource> importedResources = new ArrayList<>();

        /**
         * Required to use the less4j import mechanism.
         */
        private class RelativeAwareLessSource extends StringSource {

            private Resource resource;

            public RelativeAwareLessSource(Resource resource, String content) {
                super(content, resource.getPath());
                this.resource = resource;
            }

            @Override
            public LessSource relativeSource(String relativePath) throws StringSourceException {
                try {
                    Resource relativeResource = resource.resolve(relativePath);
                    importedResources.add(relativeResource);
                    return new RelativeAwareLessSource(relativeResource, relativeResource.getOutput().getContent());
                } catch (Exception e) {
                    LOG.error("Failed to compute relative resource: {}", resource, e);
                    throw new StringSourceException();
                }
            }
        }

        public Less4jWebpipe(String path, Webpipe webpipe) {
            super(WebpipeUtils.idOf(Less4jProcessor.class, webpipe), path, "less4j", webpipe);
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            synchronized (importedResources) {
                importedResources.clear();

                Webpipe webpipe = getChildWebpipe();
                String content = webpipe.getOutput().getContent();
                StringSource lessSource;
                if (webpipe instanceof Resource) {
                    lessSource = new RelativeAwareLessSource((Resource) webpipe, content);
                } else {
                    lessSource = new StringSource(content, webpipe.getPath());
                }
                CompilationResult result = compiler.compile(lessSource);
                logWarnings(result);
                return new WebpipeOutput(result.getCss(), WebpipeUtils.parseSourceMap(result.getSourceMap()));
            }
        }

        @Override
        public boolean refresh() throws IOException {
            boolean refresh = false;
            synchronized (importedResources) {
                for (Webpipe webpipe : importedResources) {
                    refresh = refresh || webpipe.refresh();
                }
            }
            refresh = refresh || getChildWebpipe().refresh();
            if (refresh) {
                invalidateOutputCache();
            }
            return refresh;
        }

        private void logWarnings(CompilationResult result) {
            if (!result.getWarnings().isEmpty()) {
                LOG.warn("Less warnings are:");
                for (Problem problem : result.getWarnings()) {
                    LOG.warn(problemAsString(problem));
                }
            }
        }

        private String problemAsString(Problem problem) {
            return String.format("%s:%s %s.", problem.getLine(), problem.getCharacter(), problem.getMessage());
        }
    }

}

/*
 *    Copyright 2012 - 2015 pac4j organization
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.acuman.pac4j;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.matching.ExcludedPathMatcher;
import org.pac4j.core.matching.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * To match requests by excluding path.
 *
 * @author Jerome Leleu
 * @since 1.8.1
 */
public final class ExcludedPathsMatcher implements Matcher {

    private final static Logger logger = LoggerFactory.getLogger(ExcludedPathsMatcher.class);

    private List<ExcludedPathMatcher> excludedPathMatchers;

    public ExcludedPathsMatcher() {
        excludedPathMatchers = new ArrayList<>();
    }

    public ExcludedPathsMatcher(String... excludedPaths) {
        Assert.notNull(excludedPaths);
        excludedPathMatchers = Arrays.asList(excludedPaths)
                .stream()
                .map(ExcludedPathMatcher::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(WebContext context) {
        boolean result = excludedPathMatchers
                .stream()
                .map(m -> m.matches(context))
                .reduce(true, (a, b) -> a && b);

        return result;
    }
}

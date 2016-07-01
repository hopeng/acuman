package com.acuman.pac4j;

import org.junit.Test;
import org.mockito.Mockito;
import org.pac4j.core.context.WebContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ExcludedPathsMatcherTest {

    @Test
    public void matches() throws Exception {
        ExcludedPathsMatcher emptyMatcher = new ExcludedPathsMatcher();
        WebContext context = Mockito.mock(WebContext.class);
        when(context.getPath()).thenReturn("/anything");
        assertTrue(emptyMatcher.matches(context));

        ExcludedPathsMatcher matcher = new ExcludedPathsMatcher("^/v1/user$", "^/img/.*$");

        context = Mockito.mock(WebContext.class);
        when(context.getPath()).thenReturn("/v1/user");
        assertFalse(matcher.matches(context));

        context = Mockito.mock(WebContext.class);
        when(context.getPath()).thenReturn("/img/logo.png");
        assertFalse(matcher.matches(context));

        context = Mockito.mock(WebContext.class);
        when(context.getPath()).thenReturn("/v1/patients");
        assertTrue(matcher.matches(context));
    }

}
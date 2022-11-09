package org.jkube.gitbeaver.security;

import org.junit.jupiter.api.Test;

public class EnvUtiltest {

    public static final String ENVVAR = "TEST_ENV";

    @Test
    public void test() throws Exception {
        EnvUntil.checkSetInJVM(ENVVAR);
        EnvUntil.checkSetInSystem(ENVVAR);
        EnvUntil.clearInJVM(ENVVAR);
    }
}

package org.jkube.gitbeaver.security;

import org.jkube.gitbeaver.util.ExternalProcess;
import org.jkube.gitbeaver.util.FileUtil;
import org.jkube.logging.Log;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnvUntil {

    /**
     * see https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
     */
    public static void clear(String envvar) throws Exception {
        checkSetInJVM(envvar);
        checkSetInSystem(envvar);
        clearInSystem(envvar);
        clearInJVM(envvar);
        checkSetInJVM(envvar);
        checkSetInSystem(envvar);
    }

    private static void checkSetInJVM(String envvar) {
        Log.log(envvar+" is set in JVM to "+System.getenv());
    }

    private static void checkSetInSystem(String envvar) {
        FileUtil.store(Path.of("show.sh"), List.of("echo", "$"+envvar));
        List<String> out = new ExternalProcess().command("sh", "show.sh").execute().getOutput();
        Log.log(envvar+" is set system to "+String.join(" ", out));
    }

    private static void clearInSystem(String envvar) {
        if (new ExternalProcess().command("sh", "unset", envvar).execute().hasFailed()) {
            Log.warn("Could not unset masterkey env variable");
        }
    }

    public static void clearInJVM(String envvar) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.remove(envvar);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.remove(envvar);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.remove(envvar);
                }
            }
        }
    }
}

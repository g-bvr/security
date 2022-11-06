package org.jkube.gitbeaver.security;

import org.jkube.gitbeaver.interfaces.LogConsole;

public class NoLogConsole implements LogConsole {
    @Override
    public void command(String command) {
    }

    @Override
    public void ignore(String runtimeMessage) {
    }

    @Override
    public void success(String line) {
    }

    @Override
    public void log(String line) {
    }

    @Override
    public void warn(String line) {
    }

    @Override
    public void error(String line) {
    }
}

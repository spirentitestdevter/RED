/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.RedSystemProperties;
import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class WindowsProcessTreeHandler extends AProcessTreeHandler {

    private static String cPythonInterpreterPath;

    public WindowsProcessTreeHandler() {
        initialize();
    }

    private static synchronized void initialize() {
        // on non-Windows platforms the whole handler will not be used, so we don't care about
        // initialization
        if (RedSystemProperties.isWindowsPlatform()) {
            // we try to find python interpreter in users system in order to be able to quickly run
            // interruption script; if it is not found we will use interpreter provided by user
            // but we have to remember that it can be e.g. jython which have long startup time

            final EnumSet<SuiteExecutor> cPythons = EnumSet.of(SuiteExecutor.Python, SuiteExecutor.Python2,
                    SuiteExecutor.Python3);
            for (final SuiteExecutor interpreter : cPythons) {
                final Optional<PythonInstallationDirectory> installationDirectory = PythonInstallationDirectoryFinder
                        .whereIsPythonInterpreter(interpreter);
                if (installationDirectory.isPresent()) {
                    cPythonInterpreterPath = installationDirectory.get()
                            .toPath()
                            .resolve(interpreter.executableName())
                            .toAbsolutePath()
                            .toString();
                    return;

                }
            }
        }
    }

    @Override
    public boolean isSupportedOS() {
        return RedSystemProperties.isWindowsPlatform();
    }

    @Override
    public boolean isSupported(final Process process) {
        final String procClassName = process.getClass().getName();
        return procClassName.equals("java.lang.Win32Process") || procClassName.equals("java.lang.ProcessImpl");
    }

    @Override
    public long getProcessPid(final Process process) {
        try {
            final Field f = process.getClass().getDeclaredField("handle");
            f.setAccessible(true);

            try {
                final long handle = f.getLong(process);

                final Kernel32 kernel = Kernel32.INSTANCE;
                final HANDLE winHandle = new HANDLE();
                winHandle.setPointer(Pointer.createConstant(handle));
                return kernel.GetProcessId(winHandle);
            } finally {
                f.setAccessible(false);
            }
        } catch (final Exception e) {
        }
        return ProcessInformation.PROCESS_NOT_FOUND;
    }

    @Override
    protected List<String> getChildPidsCommand(final long processPid) {
        return Arrays.asList("cmd.exe", "/c", "wmic", "process", "where", "ParentProcessID=" + processPid, "get",
                "ProcessId", "/format:csv");
    }

    @Override
    protected List<String> getKillProcessCommand(final ProcessInformation procInformation) {
        return Arrays.asList("cmd.exe", "/c", "wmic", "process", "where", "ProcessID=" + procInformation.pid(),
                "delete");
    }

    @Override
    protected List<String> getKillProcessTreeCommand(final ProcessInformation procInformation) {
        return Arrays.asList("cmd.exe", "/c", "wmic", "process", "where",
                "\"ProcessID=" + procInformation.pid() + " or ParentProcessID=" + procInformation.pid() + "\"",
                "delete");
    }

    @Override
    protected List<String> getInterruptProcessCommand(final ProcessInformation procInformation,
            final String pythonExecutablePath) throws ProcessInterruptException {
        final String execToUse = cPythonInterpreterPath != null ? cPythonInterpreterPath : pythonExecutablePath;

        if (execToUse == null) {
            throw new ProcessInterruptException(
                    "Unable to generate interruption signal. Missing python executable path");
        }
        try {
            final File script = RedTemporaryDirectory.copyScriptFile("interruptor.py");
            return Arrays.asList(execToUse, script.getAbsolutePath(), Long.toString(procInformation.pid()));
        } catch (final IOException e) {
            throw new ProcessInterruptException("Unable to generate interruption signal", e);
        }
    }

    @Override
    protected boolean isInterruptionOutputValid(final int returnCode, final List<String> output) {
        return output.isEmpty();
    }
}

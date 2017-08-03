/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;

public class RobotBreakpoints {

    public Optional<org.rf.ide.core.execution.debug.RobotLineBreakpoint> getBreakpointAtLine(final int lineNumber,
            final URI fileUri) {
        final IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        if (!breakpointManager.isEnabled()) {
            return Optional.empty();
        }

        for (final IBreakpoint breakpoint : breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)) {
            if (breakpoint instanceof RobotLineBreakpoint) {
                final RobotLineBreakpoint lineBreakpoint = (RobotLineBreakpoint) breakpoint;

                try {
                    if (lineBreakpoint.getLineNumber() == lineNumber && lineBreakpoint.isEnabled()
                            && lineBreakpoint.getMarker().getResource().getLocationURI().equals(fileUri)) {
                        return Optional.of(lineBreakpoint);
                    }
                } catch (final CoreException e) {
                    // we'll look for next breakpoints
                }
            }
        }
        return Optional.empty();
    }

    public void enableBreakpointsDisabledByHitCounter() {
        final IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();

        for (final IBreakpoint breakpoint : breakpointManager.getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID)) {
            if (breakpoint instanceof RobotLineBreakpoint) {
                final RobotLineBreakpoint lineBreakpoint = (RobotLineBreakpoint) breakpoint;

                try {
                    lineBreakpoint.enableIfDisabledByHitCounter();
                } catch (final CoreException e) {
                    // we'll look for next breakpoints
                }
            }
        }
    }
}

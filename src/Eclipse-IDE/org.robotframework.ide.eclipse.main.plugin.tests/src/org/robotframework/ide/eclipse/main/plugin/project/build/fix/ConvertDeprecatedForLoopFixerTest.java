/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


public class ConvertDeprecatedForLoopFixerTest {

    @Test
    public void documentIsProperlyChangedAndEndStatementIsAdded_whenThereIsNoEndStatementAfterForRegion()
            throws IOException, CoreException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(8);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(13);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("        :F oR  ${x}    IN RANGE    1")
                .appendLine("        \\    kw    \\")
                .build();

        final Document document = new Document("        :F oR  ${x}    IN RANGE    1", "        \\    kw    \\");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(49);
        final Stream<IDocument> changedDocuments = Stream.of(fixer)
                .map(Fixers.byApplyingToDocument(marker, document, model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments)
                .containsExactly(
                        new Document("        FOR  ${x}    IN RANGE    1", "             kw    \\", "        END"));
    }

    @Test
    public void documentIsProperlyChangedAndEndStatementIsNotAdded_whenThereIsEndStatementAfterForRegion()
            throws CoreException, IOException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(0);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(3);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("FOR  ${x}    IN RANGE    1")
                .appendLine("    \\    kw")
                .appendLine("END")
                .build();

        final Document document = new Document("FOR  ${x}    IN RANGE    1", "    \\    kw", "END");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(38);
        final Stream<IDocument> changedDocuments = Stream.of(fixer)
                .map(Fixers.byApplyingToDocument(marker, document, model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments).containsExactly(new Document("FOR  ${x}    IN RANGE    1", "         kw", "END"));
    }

    @Test
    public void documentIsProperlyChangedAndEndStatementIsAdded_whenThereIsNoKeywordsAndEndStatementInForLoop()
            throws CoreException, IOException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(0);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(4);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine(":FOR  ${x}    IN RANGE    1")
                .appendLine("        kw")
                .build();

        final Document document = new Document(":FOR  ${x}    IN RANGE    1", "        kw");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(27);
        final Stream<IDocument> changedDocuments = Stream.of(fixer)
                .map(Fixers.byApplyingToDocument(marker, document, model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments).containsExactly(new Document("FOR  ${x}    IN RANGE    1", "END", "        kw"));
    }

    @Test
    public void documentIsProperlyChangedAndEndStatementIsAdded_whenForLoopIsWithTestCaseNameInTheSameLine()
            throws CoreException, IOException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(18);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(22);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("Test Case Name    :FOR  ${x}    IN RANGE    1")
                .appendLine("                  \\       kw")
                .build();

        final Document document = new Document("Test Case Name    :FOR  ${x}    IN RANGE    1",
                "                          kw");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(56);
        final Stream<IDocument> changedDocuments = Stream.of(fixer).map(Fixers.byApplyingToDocument(marker, document,
                model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments).containsExactly(new Document("Test Case Name    FOR  ${x}    IN RANGE    1",
                "                          kw", "                  END"));
    }
}

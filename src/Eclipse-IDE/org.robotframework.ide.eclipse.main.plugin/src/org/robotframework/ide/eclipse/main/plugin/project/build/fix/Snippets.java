/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

/**
 * @author Michal Anglart
 *
 */
public class Snippets {

    public static String createSnippetInfo(final IDocument document, final IRegion regionToChange,
            final String replacement) {
        final Optional<IRegion> snippetRegion = DocumentUtilities.getSnippet(document, regionToChange.getOffset(), 2);
        if (snippetRegion.isPresent()) {
            try {
                final String snippet = document.get(snippetRegion.get().getOffset(), snippetRegion.get().getLength());
                final int inSnippetOffset = regionToChange.getOffset() - snippetRegion.get().getOffset();
                final String snippetAfterChange = snippet.substring(0, inSnippetOffset) + "<b>" + replacement + "</b>"
                        + snippet.substring(inSnippetOffset + regionToChange.getLength());

                final String beforeSnippet = snippetRegion.get().getOffset() <= 0 ? "" : "(...)<br>";
                final String afterSnippet = snippetRegion.get().getOffset()
                        + snippetRegion.get().getLength() >= document.getLength() ? "" : "<br>(...)";

                return "<pre>" + beforeSnippet + convertNewLinesToBr(snippetAfterChange) + afterSnippet + "</pre>";
            } catch (final BadLocationException e) {
                return null;
            }
        }
        return null;
    }

    public static String createSnippetInfoWithoutContext(final IDocument document, final IRegion regionToChange,
            final String replacement) {
        try {
            final int regionNumberOfLines = document.getNumberOfLines(regionToChange.getOffset(),
                    regionToChange.getLength());
            final Optional<IRegion> snippetRegion = DocumentUtilities.getSnippet(document, regionToChange.getOffset(),
                    regionNumberOfLines);
            if (snippetRegion.isPresent()) {
                final String snippetAfterChange = "<b>" + replacement + "</b>";

                final String beforeSnippet = snippetRegion.get().getOffset() <= 0 ? "" : "(...)<br>";
                final String afterSnippet = snippetRegion.get().getOffset()
                        + snippetRegion.get().getLength() >= document.getLength() ? "" : "<br>(...)";

                return "<pre>" + beforeSnippet + convertNewLinesToBr(snippetAfterChange) + afterSnippet + "</pre>";

            }
        } catch (final BadLocationException e) {
            return null;
        }
        return null;
    }

    private static String convertNewLinesToBr(final String snippetAfterChange) {
        return snippetAfterChange.replaceAll("\r\n", "<br>").replaceAll("\n", "<br>");
    }
}

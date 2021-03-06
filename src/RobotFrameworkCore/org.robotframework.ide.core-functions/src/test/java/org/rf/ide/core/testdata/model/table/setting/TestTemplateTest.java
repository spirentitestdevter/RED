/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.table.setting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTemplateTest {

    private RobotToken decToken;
    private TestTemplate testable;

    @BeforeEach
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new TestTemplate(decToken);
    }

    @Test
    public void test_ifCellWillBeAdded_atArgumentPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addUnexpectedTrashArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION);
        assertThat(elementTokens.get(1).getText()).isEqualTo("value");
        assertThat(elementTokens.get(1).getTypes())
                .contains(RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.COMMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addUnexpectedTrashArgument(argToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 3);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION);
        assertThat(elementTokens.get(1)).isSameAs(argToken);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.UNKNOWN);
        assertThat(elementTokens.get(2).getText()).isEqualTo("value");
        assertThat(elementTokens.get(2).getTypes())
                .contains(RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.COMMENT);
    }
}

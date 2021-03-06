/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ListVariableModelUpdaterTest {

    @Test
    public void test_ableToHandle_listVariable_shouldReturn_TRUE() {
        assertThat(new ListVariableModelUpdater()
                .ableToHandle(new ListVariable("", RobotToken.create(""), VariableScope.GLOBAL))).isTrue();
    }

    @Test
    public void test_ableToHandle_dictionaryVariable_shouldReturn_FALSE() {
        assertThat(new ListVariableModelUpdater()
                .ableToHandle(new DictionaryVariable("", RobotToken.create(""), VariableScope.GLOBAL))).isFalse();
    }

    @Test
    public void test_ableToHandle_NULL_shouldReturn_FALSE() {
        assertThat(new ListVariableModelUpdater().ableToHandle(null)).isFalse();
    }

    @Test
    public void test_addOrSet_logicalTest() {
        // prepare
        final ListVariable mocked = mock(ListVariable.class);
        final RobotToken one = RobotToken.create("ok");
        @SuppressWarnings("unchecked")
        final List<RobotToken> toks = mock(List.class);
        when(toks.size()).thenReturn(1);
        when(toks.get(0)).thenReturn(one);

        // execute
        new ListVariableModelUpdater().addOrSet(mocked, 1, toks);

        // verify
        final InOrder order = inOrder(mocked, toks);
        order.verify(toks, times(1)).size();
        order.verify(toks, times(1)).get(0);
        order.verify(mocked, times(1)).addItem(one, 1);
        order.verifyNoMoreInteractions();
    }
}

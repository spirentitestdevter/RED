/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.testcases;

import java.util.regex.Pattern;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCasePostconditionRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?((\\[\\s*" + createUpperLowerCaseWord("Postcondition") + "\\s*\\]))");

    public TestCasePostconditionRecognizer() {
        super(EXPECTED, RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isOlderThan(new RobotVersion(3, 1));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new TestCasePostconditionRecognizer();
    }
}

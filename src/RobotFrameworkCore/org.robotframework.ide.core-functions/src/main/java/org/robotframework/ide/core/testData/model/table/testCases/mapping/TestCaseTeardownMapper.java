package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTeardown;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTeardownMapper extends ATestCaseSettingDeclarationMapper {

    public TestCaseTeardownMapper() {
        super(RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
        rt.setText(new StringBuilder(text));

        TestCase testCase = findOrCreateNearestTestCase(currentLine,
                processingState, robotFileOutput, rt, fp);
        TestCaseTeardown teardown = new TestCaseTeardown(rt);
        testCase.addTeardown(teardown);

        processingState.push(ParsingState.TEST_CASE_SETTING_TEARDOWN);

        return rt;
    }
}

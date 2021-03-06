/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.exec.descs.ForDescriptorInfo;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.ParsingState.TableType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public class ElementPositionResolver {

    public PositionInformation buildPositionDescription(final RobotLine currentLine, final RobotToken currentToken) {

        final PositionInformation posInfo = new PositionInformation();
        final List<IRobotLineElement> lineElements = new ArrayList<>(currentLine.getLineElements());
        lineElements.add(currentToken);
        final int numberOfElements = lineElements.size();
        for (int elemIndex = 0; elemIndex < numberOfElements; elemIndex++) {
            final IRobotLineElement elem = lineElements.get(elemIndex);
            if (elem instanceof Separator) {
                if (posInfo.getSeparatorsPosIndexes().isEmpty()) {
                    posInfo.setLineSeparator((SeparatorType) elem.getTypes().get(0));
                }

                posInfo.addSeparatorPosIndex(elemIndex);
            } else if (elem instanceof RobotToken) {
                if (elem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    posInfo.addPrettyAlignPosIndex(elemIndex);
                } else if (elem.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                    posInfo.addPreviousLineContinuePosIndex(elemIndex);
                } else {
                    posInfo.addRobotTokenPosIndex(elemIndex);
                }
            }
        }

        return posInfo;
    }

    public static class PositionInformation {

        private SeparatorType lineSeparator = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;

        private final List<Integer> robotTokensPosIndexes = new ArrayList<>();

        private final List<Integer> previousLineContinuePosIndexes = new ArrayList<>();

        private final List<Integer> prettyAlignPosIndexes = new ArrayList<>();

        private final List<Integer> separatorsPosIndexes = new ArrayList<>();

        private boolean wasLastSeparator = false;

        private boolean isFirstSeparator = false;

        private void setLineSeparator(final SeparatorType lineSeparator) {
            this.lineSeparator = lineSeparator;
        }

        private void addRobotTokenPosIndex(final int robotTokenPosIndex) {
            robotTokensPosIndexes.add(robotTokenPosIndex);
            this.wasLastSeparator = false;
        }

        private void addPrettyAlignPosIndex(final int prettyAlignPosIndex) {
            prettyAlignPosIndexes.add(prettyAlignPosIndex);
        }

        private void addPreviousLineContinuePosIndex(final int prevLineContIndex) {
            previousLineContinuePosIndexes.add(prevLineContIndex);
        }

        private void addSeparatorPosIndex(final int separatorPosIndex) {
            if (robotTokensPosIndexes.isEmpty() && separatorsPosIndexes.isEmpty()) {
                isFirstSeparator = true;
            }
            separatorsPosIndexes.add(separatorPosIndex);
            this.wasLastSeparator = true;
        }

        public SeparatorType getLineSeparator() {
            return lineSeparator;
        }

        public List<Integer> getRobotTokensPosIndexes() {
            return robotTokensPosIndexes;
        }

        public List<Integer> getSeparatorsPosIndexes() {
            return separatorsPosIndexes;
        }

        public List<Integer> getPrettyAlignPosIndexes() {
            return prettyAlignPosIndexes;
        }

        public List<Integer> getPreviousLineContinuePosIndexes() {
            return previousLineContinuePosIndexes;
        }

        public boolean isContinuePreviousLineTheFirstToken(final TableType type) {
            boolean result = false;
            if (!previousLineContinuePosIndexes.isEmpty()) {
                final int theFirstContinue = previousLineContinuePosIndexes.get(0);
                if (!robotTokensPosIndexes.isEmpty()) {
                    final int theFirstToken = robotTokensPosIndexes.get(0);
                    result = theFirstContinue < theFirstToken;
                } else {
                    result = true;
                }
            }

            if (result) {
                if (type == TableType.KEYWORD || type == TableType.TEST_CASE || type == TableType.TASKS) {
                    if (getLineSeparator() == SeparatorType.TABULATOR_OR_DOUBLE_SPACE) {
                        result = getSeparatorsPosIndexes().size() == 1;
                    } else {
                        result = getSeparatorsPosIndexes().size() >= 2;
                    }
                }
            }

            return result;
        }

        public int getColumnIndex() {
            int column = 0;
            if (!robotTokensPosIndexes.isEmpty() || !separatorsPosIndexes.isEmpty()) {
                final int numberOfSeparators = separatorsPosIndexes.size();
                if (getLineSeparator() == SeparatorType.PIPE) {
                    if (wasLastSeparator) {
                        column = numberOfSeparators - 1;
                    } else {
                        column = numberOfSeparators;
                    }
                } else {
                    if (wasLastSeparator) {
                        column = numberOfSeparators + 1;
                    } else {
                        column = robotTokensPosIndexes.size();
                        if (isFirstSeparator()) {
                            column++;
                        }
                    }
                }
            }
            return column;
        }

        public boolean isLastSeparator() {
            return wasLastSeparator;
        }

        public boolean isFirstSeparator() {
            return isFirstSeparator;
        }
    }

    public enum PositionExpected implements IPositionCheckable {
        TABLE_HEADER {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        SETTING_TABLE_ELEMENT_DECLARATION {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        TEST_CASE_NAME {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {

                if (!isReallyFirstElement(posInfo, currentToken)) {
                    final List<IRobotLineElement> lineElements = currentLine.getLineElements();
                    return lineElements.size() == 1
                            && lineElements.get(0).getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE);
                }
                return true;
            }
        },
        TASK_NAME {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {

                if (!isReallyFirstElement(posInfo, currentToken)) {
                    final List<IRobotLineElement> lineElements = currentLine.getLineElements();
                    return lineElements.size() == 1
                            && lineElements.get(0).getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE);
                }
                return true;
            }
        },
        USER_KEYWORD_NAME {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {

                if (!isReallyFirstElement(posInfo, currentToken)) {
                    final List<IRobotLineElement> lineElements = currentLine.getLineElements();
                    return lineElements.size() == 1
                            && lineElements.get(0).getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE);
                }
                return true;
            }
        },
        VARIABLE_DECLARATION_IN_VARIABLE_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_SETTING_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken(TableType.SETTINGS);
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_VARIABLE_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken(TableType.VARIABLES);
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_TESTCASE_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken(TableType.TEST_CASE);
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_TASK_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken(TableType.TASKS);
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_KEYWORD_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken(TableType.KEYWORD);
            }
        },
        LINE_CONTINUE_INLINED_FOR_TESTCASE_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return isInlined(posInfo);
            }
        },
        LINE_CONTINUE_INLINED_FOR_TASK_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return isInlined(posInfo);
            }
        },
        LINE_CONTINUE_INLINED_FOR_KEYWORD_TABLE {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return isInlined(posInfo);
            }
        },
        LINE_CONTINUE_INLINED_IN_FOR_LOOP {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                final List<IRobotLineElement> elements = new ArrayList<>(currentLine.getLineElements());
                if (currentToken != null) {
                    elements.add(currentToken);
                }
                final ForDescriptorInfo forDescInfo = ForDescriptorInfo.build(elements);
                if (forDescInfo.getForStartIndex() > -1) {
                    if (forDescInfo.getForLineContinueInlineIndex() == currentLine.getLineElements().size()) {
                        return true;
                    }
                }
                return false;
            }
        },
        TEST_CASE_EXEC_ROW_ACTION_NAME {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return !isReallyFirstElement(posInfo, currentToken);
            }
        },
        TASK_EXEC_ROW_ACTION_NAME {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return !isReallyFirstElement(posInfo, currentToken);
            }
        },
        KEYWORD_EXEC_ROW_ACTION_NAME {

            @Override
            public boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                    final RobotToken currentToken) {
                return !isReallyFirstElement(posInfo, currentToken);
            }
        };

        private static boolean isInlined(final PositionInformation posInfo) {
            boolean result = false;
            if (posInfo.getPreviousLineContinuePosIndexes().size() == 1
                    && posInfo.getRobotTokensPosIndexes().size() == 1) {
                final int theFirstPreviousContinue = posInfo.getPreviousLineContinuePosIndexes().get(0);
                final int theFirstToken = posInfo.getRobotTokensPosIndexes().get(0);
                if (theFirstToken < theFirstPreviousContinue) {
                    final SeparatorType separatorType = posInfo.getLineSeparator();
                    final List<Integer> separatorsPosIndexes = posInfo.getSeparatorsPosIndexes();
                    if (separatorType == SeparatorType.PIPE) {
                        if (separatorsPosIndexes.size() >= 2) {
                            final int theFirstSeparator = separatorsPosIndexes.get(0);
                            final int theSecondSeparator = separatorsPosIndexes.get(1);
                            result = theFirstSeparator < theFirstToken && theFirstToken < theSecondSeparator
                                    && theSecondSeparator < theFirstPreviousContinue;
                        }
                    } else {
                        if (separatorsPosIndexes.size() == 1) {
                            final int theFirstSeparator = separatorsPosIndexes.get(0);
                            result = theFirstToken < theFirstSeparator && theFirstSeparator < theFirstPreviousContinue;
                        }
                    }
                }
            }

            return result;
        }

        private static boolean isReallyFirstElement(final PositionInformation posInfo, final RobotToken currentToken) {
            boolean result = false;
            final SeparatorType separator = posInfo.getLineSeparator();
            if (separator == SeparatorType.PIPE) {
                if (posInfo.getSeparatorsPosIndexes().size() == 1 && (posInfo.getRobotTokensPosIndexes().size() == 1
                        || posInfo.getPreviousLineContinuePosIndexes().size() == 1)) {
                    result = posInfo.isFirstSeparator();
                }
            } else {
                final boolean meetFirstColumnPosition = (currentToken.getStartColumn() == 0
                        || (!posInfo.getPrettyAlignPosIndexes().isEmpty()
                                && posInfo.getPrettyAlignPosIndexes().get(0) == 0));

                result = meetFirstColumnPosition
                        && (posInfo.getRobotTokensPosIndexes().size() == 1
                                || posInfo.getPreviousLineContinuePosIndexes().size() == 1)
                        && posInfo.getSeparatorsPosIndexes().isEmpty() && !posInfo.isFirstSeparator();
            }
            return result;
        }
    }

    public boolean isCorrectPosition(final PositionExpected expected, final RobotLine currentLine,
            final RobotToken currentToken) {
        final PositionInformation posInfo = buildPositionDescription(currentLine, currentToken);
        return expected.isExpectedPosition(posInfo, currentLine, currentToken);
    }

    public interface IPositionCheckable {

        boolean isExpectedPosition(final PositionInformation posInfo, final RobotLine currentLine,
                final RobotToken currentToken);
    }
}

/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;

/**
 * <p>Converter to provide the following to logging framework:</p>
 * <ul>
 * <li>Extra detail for Throwables (stack trace etc)</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class PrefixedThrowableProxyConverter extends ThrowableProxyConverter {

    private static final String PREFIX = "! ";

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        final StringBuilder buf = new StringBuilder(32);
        IThrowableProxy currentThrowable = tp;
        while (currentThrowable != null) {
            subjoinThrowableProxy(buf, currentThrowable);
            currentThrowable = currentThrowable.getCause();
        }
        return buf.toString();
    }

    void subjoinThrowableProxy(StringBuilder buf, IThrowableProxy tp) {
        subjoinFirstLine(buf, tp);


        buf.append(CoreConstants.LINE_SEPARATOR);
        final StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        final int commonFrames = tp.getCommonFrames();

        int maxIndex = stepArray.length;
        if (commonFrames > 0) {
            maxIndex -= commonFrames;
        }

        for (int i = 0; i < maxIndex; i++) {
            final String string = stepArray[i].toString();
            buf.append(PREFIX);
            buf.append(string);
            extraData(buf, stepArray[i]); // allow other data to be added
            buf.append(CoreConstants.LINE_SEPARATOR);
        }

        if (commonFrames > 0) {
            buf.append("!... ").append(tp.getCommonFrames()).append(
                    " common frames omitted").append(CoreConstants.LINE_SEPARATOR);
        }
    }

    private void subjoinFirstLine(StringBuilder buf, IThrowableProxy tp) {
        final int commonFrames = tp.getCommonFrames();
        if (commonFrames > 0) {
            buf.append(CoreConstants.CAUSED_BY);
        }
        subjoinExceptionMessage(buf, tp);
    }

    private void subjoinExceptionMessage(StringBuilder buf, IThrowableProxy tp) {
        buf.append(PREFIX).append(tp.getClassName()).append(": ").append(tp.getMessage());
    }
}
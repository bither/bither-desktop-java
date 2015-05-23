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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;

import java.util.TimeZone;

/**
 * <p>Pattern to provide the following to logging framework:</p>
 * <ul>
 * <li>Log entry format</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class LogFormatter extends PatternLayout {

    public LogFormatter(LoggerContext context, TimeZone timeZone) {
        super();
        setOutputPatternAsHeader(false);
        getDefaultConverterMap().put("ex", PrefixedThrowableProxyConverter.class.getName());
        getDefaultConverterMap().put("xEx", PrefixedExtendedThrowableProxyConverter.class.getName());
        // This pattern allows time, log level then thread to be quickly located making unusual
        // activity such as WARN and ERROR stand out
        setPattern("[%d{ISO8601," + timeZone.getID() + "}] %-5level [%thread] %logger{16} - %msg %xEx%n");
        setContext(context);
    }

}

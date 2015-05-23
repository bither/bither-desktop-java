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

package net.bither.exception;

/**
 * <p>Runtime exception to provide the following to the UI:</p>
 * <ul>
 * <li>Wrapper for all caught exceptions for easier logging</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class UIException extends RuntimeException {

    public UIException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public UIException(String message, Throwable cause) {
        super(message, cause);
    }

    public UIException(String message) {
        super(message);
    }
}

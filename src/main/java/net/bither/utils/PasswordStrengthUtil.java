/*
 *
 *  * Copyright 2014 http://Bither.net
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package net.bither.utils;

import net.bither.bitherj.utils.CharSequenceUtil;

import java.awt.*;

public class PasswordStrengthUtil {
    public static final PasswordStrength PassingPasswordStrength = PasswordStrength.Normal;
    public static final PasswordStrength WarningPasswordStrength = PasswordStrength.Medium;

    public enum PasswordStrength {
        Weak(0), Normal(1), Medium(2), Strong(3), VeryStrong(4);

        private int value;

        PasswordStrength(int value) {
            this.value = value;
        }


        public String getName() {
            switch (this) {
                case Normal:
                    return LocaliserUtils.getString("password_strength_normal");
                case Medium:
                    return LocaliserUtils.getString("password_strength_medium");
                case Strong:
                    return LocaliserUtils.getString("password_strength_strong");
                case VeryStrong:
                    return LocaliserUtils.getString("password_strength_very_strong");
                default:
                    return LocaliserUtils.getString("password_strength_weak");
            }
        }

        public Color getColor() {
            switch (this) {
                case Normal:
                    return new Color(238, 95, 91);
                case Medium:
                    return new Color(255, 163, 33);
                case Strong:
                    return new Color(98, 196, 98);
                case VeryStrong:
                    return new Color(98, 196, 98);
                default:
                    return new Color(238, 95, 91);
            }
        }


        public int getValue() {
            return value;
        }

        public int getProgress() {
            return value + 1;
        }

        public boolean passed() {
            return getValue() >= PassingPasswordStrength.getValue();
        }

        public boolean warning() {
            return passed() && getValue() <= WarningPasswordStrength.getValue();
        }
    }

    public static PasswordStrength checkPassword(CharSequence password) {
        switch (CharSequenceUtil.getRating(password)) {
            case 0:
                return PasswordStrength.Weak;
            case 1:
                return PasswordStrength.Normal;
            case 2:
                return PasswordStrength.Medium;
            case 3:
                return PasswordStrength.Strong;
            default:
                return PasswordStrength.VeryStrong;
        }
    }

}

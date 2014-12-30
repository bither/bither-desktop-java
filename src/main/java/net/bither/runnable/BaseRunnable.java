/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.runnable;


import javax.swing.*;

public abstract class BaseRunnable implements Runnable {


    private RunnableListener runnableListener;

    public void setRunnableListener(RunnableListener runnableListener) {
        this.runnableListener = runnableListener;
    }

    protected void prepare() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (runnableListener != null) {
                    runnableListener.prepare();
                }
            }
        });

    }

    protected void success(final Object obj) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (runnableListener != null) {
                    runnableListener.success(obj);
                }
            }
        });

    }

    protected void error(final int errorCode, final String errorMsg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (runnableListener != null) {
                    runnableListener.error(errorCode, errorMsg);
                }

            }
        });

    }

}

/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem;

import net.bither.viewsystem.base.ViewEnum;
import net.bither.viewsystem.base.Viewable;
import net.bither.viewsystem.froms.ColdDefaultPanel;
import net.bither.viewsystem.froms.ShowTransactionsForm;

import java.util.EnumMap;
import java.util.Map;

public class ViewFactory {
    private Map<ViewEnum, Viewable> viewMap;

    public ViewFactory() {
        initialise();
    }

    public final void initialise() {
        viewMap = new EnumMap<ViewEnum, Viewable>(ViewEnum.class);
    }

    public Viewable getView(ViewEnum viewNumber) {
        Viewable viewToReturn = viewMap.get(viewNumber);

        if (viewToReturn == null) {
            viewToReturn = createView(viewNumber);
        }

        return viewToReturn;
    }

    private Viewable createView(ViewEnum viewNumber) {
        Viewable viewToReturn = null;

        switch (viewNumber) {

            case SAME_VIEW: {
                assert false;
                break;
            }
            case TRANSACTIONS_VIEW: {
                viewToReturn = new ShowTransactionsForm();
                break;
            }
            case COLD_WALLET_VIEW: {
                viewToReturn = new ColdDefaultPanel();
                break;
            }


            default: {
            }
        }

        if (viewToReturn != null) {
            viewMap.put(viewNumber, viewToReturn);
        }
        return viewToReturn;
    }
}

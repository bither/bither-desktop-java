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
package net.bither.viewsystem.base;


import net.bither.bitherj.core.Address;

public interface ViewSystem {
    /**
     * Display the view specified.
     */
    public void displayView(ViewEnum viewToDisplay);

    /**
     * Navigate away from a view - gives the view the opportunity to tidy up/ disappear etc.
     *
     * @param viewToNavigateAwayFrom - current view to navigate away from -one of the View constants.
     */
    public void navigateAwayFromView(ViewEnum viewToNavigateAwayFrom);

    /**
     * Tells the view system that the model data has changed (but the wallet is still the same).
     * Use this variant for when you want the UI to update immediately (typically after user generated events).
     */
    public void fireDataChangedUpdateNow(DisplayHint displayHint);

    /**
     * Tells the view system to recreate all views e.g. after a language change or wallet change.
     *
     * @param initUI Completely redraw everything on all screens = true
     */
    public void recreateAllViews(boolean initUI, ViewEnum initialView);

    /**
     * Tells the view system that an external process has modified one of the wallets.
     */
    public void fireFilesHaveBeenChangedByAnotherProcess(Address perWalletModelData);


    /**
     * Set the help context to display.
     *
     * @param helpContextToDisplay
     */
    public void setHelpContext(String helpContextToDisplay);
}

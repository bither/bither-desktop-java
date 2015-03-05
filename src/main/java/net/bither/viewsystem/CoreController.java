/*
 * The MIT License
 *
 * Copyright 2013 Cameron Garnham.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.bither.viewsystem;

import net.bither.Bither;
import net.bither.bitherj.core.Address;
import net.bither.platform.listener.*;
import net.bither.viewsystem.action.ExitAction;
import net.bither.viewsystem.base.DisplayHint;
import net.bither.viewsystem.base.ViewEnum;
import net.bither.viewsystem.base.ViewSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class CoreController implements GenericOpenURIEventListener, GenericPreferencesEventListener,
        GenericAboutEventListener, GenericQuitEventListener {


    private final Collection<ViewSystem> viewSystems;
    private volatile boolean applicationStarting = true;

    private Logger log = LoggerFactory.getLogger(CoreController.class);


    /**
     * The currently displayed view. One of the View constants.
     */
    private ViewEnum currentView = null;


    public CoreController() {
        viewSystems = new CopyOnWriteArrayList<ViewSystem>();


    }


    public final Collection<ViewSystem> getViewSystem() {
        return viewSystems;
    }

    /**
     * Register a new MultiBitViewSystem from the list of views that are managed.
     *
     * @param viewSystem system
     */
    public final void registerViewSystem(ViewSystem viewSystem) {
        viewSystems.add(viewSystem);
    }


    public final boolean getApplicationStarting() {
        return this.applicationStarting;
    }


    public final void setApplicationStarting(boolean applicationStarting) {
        this.applicationStarting = applicationStarting;
    }

    /**
     * Fire that the model data has changed.
     */

    public final void fireDataChangedUpdateNow() {
        //log.debug("fireDataChangedUpdateNow called");
        for (ViewSystem viewSystem : this.getViewSystem()) {
            viewSystem.fireDataChangedUpdateNow(DisplayHint.COMPLETE_REDRAW);
        }
    }


    /**
     * Display the view specified.
     *
     * @param viewToDisplay View to display. Must be one of the View constants
     */

    public void displayView(ViewEnum viewToDisplay) {
        log.debug("Displaying view '" + viewToDisplay + "'");

        // Tell all views to close the current view.
        for (ViewSystem viewSystem : getViewSystem()) {
            viewSystem.navigateAwayFromView(getCurrentView());
        }

        setCurrentView(viewToDisplay);

        // Tell all views which view to display.
        for (ViewSystem viewSystem : getViewSystem()) {
            viewSystem.displayView(getCurrentView());
        }
    }


    public ViewEnum getCurrentView() {
        return ViewEnum.DEFAULT_VIEW();
    }

    public void setCurrentView(ViewEnum view) {
        currentView = view;
    }

    @Override
    public void onPreferencesEvent(GenericPreferencesEvent event) {

    }

    @Override
    public void onAboutEvent(GenericAboutEvent event) {
        displayView(ViewEnum.TRANSACTIONS_VIEW);
    }

    @Override
    public synchronized void onOpenURIEvent(GenericOpenURIEvent event) {
        log.debug("Controller received open URI event with URI='{}'", event.getURI().toASCIIString());
        if (!getApplicationStarting()) {
            log.debug("Open URI event handled immediately");
        } else {
            log.debug("Open URI event not handled immediately because application is still starting. Remembering for later.");
            // Bither.setRememberedRawBitcoinURI(event.getURI().toASCIIString());
        }
    }

    @Override
    public void onQuitEvent(GenericQuitEvent event, GenericQuitResponse response) {

        ExitAction exitAction;
        if (getViewSystem() != null) {
            Iterator<ViewSystem> iterator = getViewSystem().iterator();
            ViewSystem viewSystemLoop = iterator.next();
            if (viewSystemLoop instanceof MainFrame) {
                exitAction = new ExitAction();
            } else {
                exitAction = new ExitAction();
            }
        } else {
            exitAction = new ExitAction();
        }
        exitAction.actionPerformed(null);
        response.performQuit();
    }

    public final void fireRecreateAllViews(boolean initUI) {

        for (ViewSystem viewSystem : getViewSystem()) {
            viewSystem.recreateAllViews(initUI, Bither.getCoreController().getCurrentView());
        }
    }


    public void fireFilesHaveBeenChangedByAnotherProcess(Address perWalletModelData) {
        //log.debug("fireFilesHaveBeenChangedByAnotherProcess called");
        for (ViewSystem viewSystem : Bither.getCoreController().getViewSystem()) {
            viewSystem.fireFilesHaveBeenChangedByAnotherProcess(perWalletModelData);
        }

        Bither.getCoreController().fireDataChangedUpdateNow();
    }


}

/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.studio;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import rapaio.graphics.base.*;
import rapaio.printer.local.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RapaioGraphicsToolWindowFactory implements ToolWindowFactory, ExtendedPrinter {

    private ToolWindow myToolWindow;
    private JPanel myToolWindowContent = new JPanel();
    private FigurePanel figurePanel;
    private Figure figure;
    private Lock figureLock = new ReentrantLock();

    public RapaioGraphicsToolWindowFactory() {
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        myToolWindow.getContentManager().addContent(content);
        RapaioStudioServer.getInstance().setExtendedPrinter(this);
        myToolWindow.getComponent().getParent().addComponentListener(new MainListener(this));
    }

    public void drawImage(Figure figure) {
        this.figure = figure;
        figureLock.lock();
        repaintFigure();
        figureLock.unlock();
    }

    public void repaintFigure() {
        if (figurePanel != null) {
            myToolWindowContent.remove(figurePanel);
        }
        try {
            if (figure == null) {
                return;
            }
            BufferedImage bi = ImageUtility.buildImage(figure, getWidth(), getHeight());
            figurePanel = new FigurePanel(bi);
            figurePanel.setVisible(true);
            myToolWindowContent.setLayout(new BorderLayout());
            myToolWindowContent.add(figurePanel, BorderLayout.CENTER);
            figurePanel.setVisible(true);
            figurePanel.revalidate();
            figurePanel.paintImmediately(myToolWindowContent.getVisibleRect());
            figurePanel.setSize(myToolWindowContent.getSize());
        } catch (RuntimeException ex) {
            Notifications.Bus.notify(
                    new Notification(RapaioStudioServer.RAPAIO_GROUP_ID_INFO, "Error after accept command.", ex.getMessage(), NotificationType.WARNING));
        }
    }

    @Override
    public int getWidth() {
        return myToolWindow.getComponent().getParent().getWidth();
    }

    @Override
    public int getHeight() {
        return myToolWindow.getComponent().getParent().getHeight();
    }

    private class MainListener implements ComponentListener {

        private final RapaioGraphicsToolWindowFactory parent;

        private MainListener(RapaioGraphicsToolWindowFactory parent) {
            this.parent = parent;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            if (figureLock.tryLock()) {
                parent.repaintFigure();
                figureLock.unlock();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            if (figureLock.tryLock()) {
                parent.repaintFigure();
                figureLock.unlock();
            }
        }

        @Override
        public void componentShown(ComponentEvent e) {
            if (figureLock.tryLock()) {
                parent.repaintFigure();
                figureLock.unlock();
            }
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }
}

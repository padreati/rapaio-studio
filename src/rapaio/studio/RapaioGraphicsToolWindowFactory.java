/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.studio;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;
import rapaio.printer.FigurePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RapaioGraphicsToolWindowFactory implements ToolWindowFactory, ExtendedPrinter {

    private ToolWindow myToolWindow;
    private JPanel myToolWindowContent;
    private FigurePanel figurePanel;
    private Figure figure;

    public RapaioGraphicsToolWindowFactory() {
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", true);
        toolWindow.getContentManager().addContent(content);
        RapaioStudioServer.getInstance().setExtendedPrinter(this);

        myToolWindow.getComponent().getParent().addComponentListener(new MainListener(this));
    }

    public void drawImage(Figure figure) {
        this.figure = figure;
        if (figurePanel != null) {
            myToolWindowContent.remove(figurePanel);
        }
        BufferedImage bi = ImageUtility.buildImage(figure, getWidth(), getHeight());
        figurePanel = new FigurePanel(bi);
        figurePanel.setVisible(true);
        myToolWindowContent.setLayout(new BorderLayout());
        myToolWindowContent.add(figurePanel, BorderLayout.CENTER);
        figurePanel.setVisible(true);
        figurePanel.paintImmediately(myToolWindowContent.getVisibleRect());
        figurePanel.setSize(myToolWindowContent.getSize());
    }

    public void repaintFigure() {
        if (figure == null)
            return;

        if (figurePanel != null) {
            myToolWindowContent.remove(figurePanel);
        }
        BufferedImage bi = ImageUtility.buildImage(figure, getWidth(), getHeight());
        figurePanel = new FigurePanel(bi);
        figurePanel.setVisible(true);
        myToolWindowContent.setLayout(new BorderLayout());
        myToolWindowContent.add(figurePanel, BorderLayout.CENTER);
        figurePanel.setVisible(true);
        figurePanel.paintImmediately(myToolWindowContent.getVisibleRect());
        figurePanel.setSize(myToolWindowContent.getSize());
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
            parent.repaintFigure();
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }
}

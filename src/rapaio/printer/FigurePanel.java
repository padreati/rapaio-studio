/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.printer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FigurePanel extends JPanel {

    protected volatile BufferedImage currentImage;
    protected volatile SwingWorker<BufferedImage, Object> drawWorker;
    boolean forceRedraw = true;

    public FigurePanel(BufferedImage image) {
        this.currentImage = image;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        final String drawingMessage = "Rendering Update...";
        FontMetrics fm = g.getFontMetrics();

        if (currentImage != null) {
            if (currentImage.getWidth() != getWidth() || currentImage.getHeight() != getHeight() || forceRedraw) {
                forceRedraw = false;
                if (drawWorker == null) {
                    createBackgroundImage();
                }

                g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
                g.drawString(drawingMessage, 3, getHeight() - fm.getHeight() / 2);
            } else {
                g.drawImage(currentImage, 0, 0, null);
            }
        } else if (currentImage == null) {
            if (drawWorker == null) {
                createBackgroundImage();
            }
            g.drawString(drawingMessage, getWidth() / 2 - fm.stringWidth(drawingMessage) / 2, getHeight() / 2 - fm.getHeight() / 2);
        }
    }

    /**
     * Creates a new worker to do the image rendering in the background.
     */
    private void createBackgroundImage() {
        drawWorker = new SwingWorker<BufferedImage, Object>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return currentImage;
            }

            @Override
            protected void done() {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentImage = get();
                            drawWorker = null;
                            revalidate();
                            repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.exit(0);
                        }
                    }
                });

            }
        };

        drawWorker.execute();
    }
}

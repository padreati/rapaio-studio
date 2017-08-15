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
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import rapaio.graphics.base.Figure;
import rapaio.printer.idea.ClassMarshaller;
import rapaio.printer.idea.CommandBytes;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class RapaioStudioServer implements ApplicationComponent {

    public static final int DEFAULT_PORT = 56339;
    public static final String RAPAIO_GROUP_ID_INFO = "RapaioInfo";
    private static RapaioStudioServer instance;

    public static RapaioStudioServer getInstance() {
        if (instance == null) {
            instance = new RapaioStudioServer();
        }
        return instance;
    }

    private ExtendedPrinter printer;
    private ServerSocket serverSocket;

    private RapaioStudioServer() {
    }

    public void initComponent() {
        try {
            getInstance().shutdown();
            getInstance().start();
        } catch (Exception ex) {
            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Error", ex.getMessage(), NotificationType.ERROR));

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
        }
    }

    public void disposeComponent() {
        try {
            getInstance().shutdown();
        } catch (Exception ex) {
            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Error", ex.getMessage(), NotificationType.ERROR));

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
        }
    }

    public void setExtendedPrinter(ExtendedPrinter printer) {
        this.printer = printer;
    }

    @NotNull
    public String getComponentName() {
        return "rapaio.studio.RapaioStudioServer";
    }

    private Thread listenerThread;

    public void start() throws IOException {
        listenerThread = new Thread(new Runnable() {

            public void run() {
                try {
                    serverSocket = ServerSocketFactory.getDefault().createServerSocket(DEFAULT_PORT);
                    while (true) {
                        try {
                            Socket s = serverSocket.accept();
                            if (serverSocket.isClosed()) {
                                return;
                            }
                            CommandBytes cb = new ClassMarshaller().unmarshall(s.getInputStream());
                            switch (cb.getType()) {
                                case DRAW:
                                    doDraw(cb);
                                    break;
                            }
                        } catch (Exception ex) {
                            Notifications.Bus.notify(
                                    new Notification(RAPAIO_GROUP_ID_INFO, "Error after accept command.", ex.getMessage(), NotificationType.ERROR));

                            StringWriter sw = new StringWriter();
                            ex.printStackTrace(new PrintWriter(sw));

                            Notifications.Bus.notify(
                                    new Notification(RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
                        }
                    }
                } catch (IOException ex) {
                    Notifications.Bus.notify(
                            new Notification(RAPAIO_GROUP_ID_INFO, "Error on main server socket.", ex.getMessage(), NotificationType.ERROR));

                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));

                    Notifications.Bus.notify(
                            new Notification(RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
                }
            }

            private void doDraw(CommandBytes cb) throws IOException, ClassNotFoundException {
                InputStream in = new ByteArrayInputStream(cb.getBytes());
                final Figure figure = (Figure) new ObjectInputStream(in).readObject();
                if (printer != null)
                    printer.drawImage(figure);
            }

            private CommandBytes doConfig(CommandBytes cb) throws IOException {
                if (printer != null) {
                    cb.setGraphicalWidth(printer.getWidth());
                    cb.setGraphicalHeight(printer.getHeight());
                }
                return cb;
            }
        });
        listenerThread.start();
    }

    @SuppressWarnings("deprecation")
    public void shutdown() throws IOException, InterruptedException {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Throwable ex) {
            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Error at shutdown", ex.getMessage(), NotificationType.ERROR));

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
        }
        if (listenerThread != null) {
            listenerThread.stop();
        }
    }
}

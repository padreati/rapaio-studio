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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import rapaio.printer.server.ClassMarshaller;
import rapaio.printer.server.CommandBytes;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
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
        }
    }

    public void disposeComponent() {
        try {
            getInstance().shutdown();
        } catch (Exception ex) {
            Notifications.Bus.notify(
                    new Notification(RAPAIO_GROUP_ID_INFO, "Error", ex.getMessage(), NotificationType.ERROR));
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
                                case CONFIG:
                                    cb = doConfig(cb);
                                    new ClassMarshaller().marshallConfig(s.getOutputStream(), cb);
                                    s.getOutputStream().flush();
                                    doDraw(new ClassMarshaller().unmarshall(s.getInputStream()));
                                    break;

                                case DRAW:
                                    doDraw(cb);
                                    break;
                            }

                        } catch (Exception ex) {
                            System.out.println("Error running remote command" + ex.getMessage());
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("Error running remote command" + ex.getMessage());
                }
            }

            private void doDraw(CommandBytes cb) throws IOException {
                InputStream in = new ByteArrayInputStream(cb.getBytes());
                final BufferedImage image = ImageIO.read(in);
                if (printer != null)
                    printer.drawImage(image);
            }

            private CommandBytes doConfig(CommandBytes cb) throws IOException {
                cb.setGraphicalWidth(printer.getWidth());
                cb.setGraphicalHeight(printer.getHeight());
                return cb;
            }
        });
        listenerThread.start();
    }

    public void shutdown() throws IOException, InterruptedException {
        try {
            serverSocket.close();
        } catch (Throwable ex) {
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread.join(0);
        }
    }
}

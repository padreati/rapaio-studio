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
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import rapaio.graphics.base.*;
import rapaio.printer.idea.*;

import javax.net.ServerSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    public ExtendedPrinter getPrinter() {
        return printer;
    }

    public void setExtendedPrinter(ExtendedPrinter printer) {
        this.printer = printer;
    }

    void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    ServerSocket getServerSocket() {
        return serverSocket;
    }

    @NotNull
    public String getComponentName() {
        return "rapaio.studio.RapaioStudioServer";
    }

    private Thread listenerThread;

    public void start() {
        listenerThread = new WorkingThread(this);
        listenerThread.start();
    }

    @SuppressWarnings("deprecation")
    public void shutdown() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ex) {
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


class WorkingThread extends Thread {

    private final RapaioStudioServer parent;

    public WorkingThread(RapaioStudioServer parent) {
        this.parent = parent;
    }

    public void run() {
        try {
            parent.setServerSocket(ServerSocketFactory.getDefault().createServerSocket(RapaioStudioServer.DEFAULT_PORT));
            while (true) {
                try {
                    Socket s = parent.getServerSocket().accept();
                    if (parent.getServerSocket().isClosed()) {
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
                            new Notification(RapaioStudioServer.RAPAIO_GROUP_ID_INFO, "Error after accept command.", ex.getMessage(), NotificationType.ERROR));

                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));

                    Notifications.Bus.notify(
                            new Notification(RapaioStudioServer.RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
                }
            }
        } catch (IOException ex) {
            Notifications.Bus.notify(
                    new Notification(RapaioStudioServer.RAPAIO_GROUP_ID_INFO, "Error on main server socket.", ex.getMessage(), NotificationType.ERROR));

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            Notifications.Bus.notify(
                    new Notification(RapaioStudioServer.RAPAIO_GROUP_ID_INFO, "Stack trace:", sw.toString(), NotificationType.ERROR));
        }
    }

    private void doDraw(CommandBytes cb) throws IOException, ClassNotFoundException {
        InputStream in = new ByteArrayInputStream(cb.getBytes());
        final Figure figure = (Figure) new ObjectInputStream(in).readObject();
        if (parent.getPrinter() != null)
            parent.getPrinter().drawImage(figure);
    }

    private CommandBytes doConfig(CommandBytes cb) throws IOException {
        if (parent.getPrinter() != null) {
            cb.setGraphicalWidth(parent.getPrinter().getWidth());
            cb.setGraphicalHeight(parent.getPrinter().getHeight());
        }
        return cb;
    }
}
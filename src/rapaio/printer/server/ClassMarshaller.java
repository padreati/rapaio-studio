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

package rapaio.printer.server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassMarshaller {

    public void marshallDraw(OutputStream out, BufferedImage image, int width, int height) throws IOException {
        byte[] bytes;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos2 = null;
        try {
            baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            bytes = baos.toByteArray();
            CommandBytes cb = CommandBytes.newDraw(bytes);
            oos2 = new ObjectOutputStream(out);
            oos2.writeObject(cb);
            oos2.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (baos != null) baos.close();
            if (oos2 != null) oos2.close();
        }
    }

    public void marshallConfig(OutputStream out, CommandBytes cb) throws IOException {
        if (cb == null) {
            cb = CommandBytes.newConfig();
        }
        ObjectOutputStream oos2 = new ObjectOutputStream(out);
        oos2.writeObject(cb);
        oos2.flush();
    }

    public void marshallConfig(OutputStream out) throws IOException {
        marshallConfig(out, CommandBytes.newConfig());
    }

    public CommandBytes unmarshall(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        return (CommandBytes) ois.readObject();
    }
}

package deng.palauncher.form;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamDrain extends Thread {
    private InputStream ins;

    public InputStreamDrain(InputStream ins) {
        this.ins = ins;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                int len = ins.read(buffer);
                if (len > 0) {
                    String log = new String(buffer,0,len);
                    System.out.print(log);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package deng.palauncher.form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class MainForm extends JFrame {
    public static int LOGO_FORM_WIDTH = 400;
    public static int LOGO_FORM_HEIGHT = 150;

    public static int BROWSER_FORM_WIDTH = 1024;
    public static int BROWSER_FORM_HEIGHT = 768;

    private static boolean PORT_CHECK_OPTION_DB = false;

    private static final int PORT_CHECK_OK = -1;
    private static final int EXP_SERVER_RUNNING = 0;
    private static final int EXP_DB_RUNNING = 1;
    private static final int EXP_VISA_PW_RUNNING = 2;
    private static final int EXP_VISA_LD_RUNNING = 3;

    private static int PORT_SERVER = 8080;
    private static int PORT_DB = 3306;
    private static int PORT_VISA_PW = 8000;
    private static int PORT_VISA_LD = 8001;

    private static String HOME_DIR = "";

    private static String PG_NAME_SERVER_PA = "$HOME\\jre\\bin\\java.exe -jar $HOME\\pastation_pa.jar";
    private static String PG_NAME_SERVER_FM = "$HOME\\jre\\bin\\java.exe -jar $HOME\\pastation_fm.jar";
    private static String PG_NAME_DB = "$HOME\\database\\bin\\mysqld --standalone";
    private static String PG_NAME_VISA_PW = "python $HOME\\VisaPW.py";
    private static String PG_NAME_VISA_LD = "python $HOME\\VisaLD.py";

    private static InputStreamDrain INS_SERVER = null;
    private static InputStreamDrain INS_VISA_PW = null;
    private static InputStreamDrain INS_VISA_LD = null;

    private static Process PROC_SERVER = null;
    private static Process PROC_DB = null;
    private static Process PROC_VISA_PW = null;
    private static Process PROC_VISA_LD = null;

    private JPanel contentPane;
    private JPanel logoPane;

    private static int PG_TYPE_PA = 0;
    private static int PG_TYPE_FM = 1;
    private static int pgType = PG_TYPE_PA;
    private static boolean runVisaPW = false;
    private static boolean runVisaLD = false;

    private static ComponentListener mainFromListener = new ComponentListener() {
        public void componentResized(ComponentEvent e) {

        }

        public void componentMoved(ComponentEvent e) {

        }

        public void componentShown(ComponentEvent e) {
            int checkResult = checkPorts();
            switch (checkResult) {
                case EXP_SERVER_RUNNING :
                    JOptionPane.showMessageDialog(null,"请终止后台运行的Java(java.exe)进程后再启动程序！");
                    break;
                case EXP_DB_RUNNING :
                    JOptionPane.showMessageDialog(null,"请终止后台运行的MySQL(mysqld.exe)进程后再启动程序！");
                    break;
                case EXP_VISA_PW_RUNNING :
                    JOptionPane.showMessageDialog(null,"请终止后台运行的Python(python.exe)进程后再启动程序！");
                    break;
                case EXP_VISA_LD_RUNNING :
                    JOptionPane.showMessageDialog(null,"请终止后台运行的Python(python.exe)进程后再启动程序！");
                    break;
            }
            checkResult = PORT_CHECK_OK;
            if (checkResult == PORT_CHECK_OK) {
                startProcesses();
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                ((JFrame)e.getComponent()).setVisible(false);
                showBrowserForm();
            } else {
                System.exit(0);
            }
        }

        public void componentHidden(ComponentEvent e) {
        }
    };

    private static int checkPorts() {
        // 检查服务器端口是否存在
        ServerSocket sock = null;
        try {
            sock = new ServerSocket(PORT_SERVER);
        } catch (Exception e) {
            return EXP_SERVER_RUNNING;
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sock = null;
            }
        }
        // 检查MySQL端口是否存在
        try {
            sock = new ServerSocket(PORT_DB);
        } catch (Exception e) {
            if (PORT_CHECK_OPTION_DB) {
                return EXP_DB_RUNNING;
            }
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sock = null;
            }
        }
        DatagramSocket dsock = null;
        if (runVisaPW) {
            try {
                dsock = new DatagramSocket(PORT_VISA_PW);
            } catch (Exception e) {
                return EXP_VISA_PW_RUNNING;
            } finally {
                if (dsock != null) {
                    dsock.close();
                    dsock = null;
                }
            }
        }
        if (runVisaLD) {
            try {
                dsock = new DatagramSocket(PORT_VISA_LD);
            } catch (Exception e) {
                return EXP_VISA_LD_RUNNING;
            } finally {
                if (dsock != null) {
                    dsock.close();
                    dsock = null;
                }
            }
        }
        return PORT_CHECK_OK;
    }

    public static void startProcesses() {
        try {
            PROC_DB = Runtime.getRuntime().exec(PG_NAME_DB.replace("$HOME", HOME_DIR));
            if (runVisaPW) {
                PROC_VISA_PW = Runtime.getRuntime().exec(PG_NAME_VISA_PW.replace("$HOME", HOME_DIR));
            }
            if (runVisaLD) {
                PROC_VISA_LD = Runtime.getRuntime().exec(PG_NAME_VISA_LD.replace("$HOME", HOME_DIR));
            }
            if (pgType == PG_TYPE_PA) {
                PROC_SERVER = Runtime.getRuntime().exec(PG_NAME_SERVER_PA.replace("$HOME", HOME_DIR));
            } else if (pgType == PG_TYPE_FM) {
                PROC_SERVER = Runtime.getRuntime().exec(PG_NAME_SERVER_FM.replace("$HOME", HOME_DIR));
            }

            if (PROC_SERVER != null) {
                INS_SERVER = new InputStreamDrain(PROC_SERVER.getInputStream());
                INS_SERVER.start();
            }
            if (PROC_VISA_PW != null) {
                INS_VISA_PW = new InputStreamDrain(PROC_VISA_PW.getInputStream());
                INS_VISA_PW.start();
            }
            if (PROC_VISA_LD != null) {
                INS_VISA_LD = new InputStreamDrain(PROC_VISA_LD.getInputStream());
                INS_VISA_LD.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopProcesses() {
        if (PROC_SERVER != null) {
            try {
                PROC_SERVER.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (PROC_DB != null) {
            try {
                PROC_DB.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (PROC_VISA_PW != null) {
            try {
                PROC_VISA_PW.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (PROC_VISA_LD != null) {
            try {
                PROC_VISA_LD.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showBrowserForm() {
        try {
            BrowserForm.run();
        } catch (Exception e) {

        }
    }

    public MainForm() {
        setContentPane(contentPane);

        setTitle("电动汽车无线充电综合测试系统");
//        setTitle("无线充电空间电磁场测试系统");

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setUndecorated(true);
        setLocationRelativeTo(null);

        /** 居中显示主窗口 **/
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(LOGO_FORM_WIDTH, LOGO_FORM_HEIGHT);
        setLocation((int) (dimension.getWidth() / 2 - LOGO_FORM_WIDTH / 2),
                (int) (dimension.getHeight() / 2 - LOGO_FORM_HEIGHT / 2));
        /************************/

        addComponentListener(mainFromListener);
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        MainForm mainForm = new MainForm();
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        HOME_DIR = args[0];
        if (args.length > 1) {
            String[] pgOptions = args[1].split(",");
            for (int i = 0; i < pgOptions.length; i++) {
                if (pgOptions[i].equals("PA")) {
                    pgType = PG_TYPE_PA;
                } else if (pgOptions[i].equals("FM")) {
                    pgType = PG_TYPE_FM;
                } else if (pgOptions[i].equals("PW")) {
                    runVisaPW = true;
                } else if (pgOptions[i].equals("LD")) {
                    runVisaLD = true;
                }
            }
            System.out.println("PG:" + String.valueOf(pgType) + ", PW:" + String.valueOf(runVisaPW) + ", LD:" + String.valueOf(runVisaLD));
        }
        mainForm.pack();
        mainForm.setVisible(true);
    }
}

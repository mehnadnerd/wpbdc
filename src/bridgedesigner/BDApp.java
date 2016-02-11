/*
 * The Bridge Designer (2nd Edition) application class.
 */

package bridgedesigner;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * The main class of the Bridge Designer (2nd Edition) application.
 */
public class BDApp extends SingleFrameApplication {

    public static final int WINDOWS_OS = 1;
    public static final int MAC_OS_X = 2;
    public static final int LINUX_OS = 3;
    public static final int UNKNOWN_OS = 4;
    
    private static int os = 0;
    private static Level loggerLevel = Level.OFF;
    private static String fileName = null;
    private static boolean legacyGraphics = false;
    // By default we'll try for an enhanced Mac interface as long as we're running on a Mac.
    private static boolean enhancedMacUI = (getOS() == MAC_OS_X);
    private static String resourcePath = "/bridgedesigner/resources/";

    // Attempt to get a glProfile for OpenGL rendering.

    public static int getOS() {
        if (os == 0) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.indexOf("windows") >= 0) {
                os = WINDOWS_OS;
            }
            else if (osName.indexOf("mac os x") >= 0) {
                os = MAC_OS_X;
            }
            else {
                os = UNKNOWN_OS;
            }
        }
        return os;
    }
    
    /**
     * At startup create and show the main frame of the application.
     */
    protected void startup() {
        // Our drawing is too slow to keep up with dyanmic window resizing, so
        // turn it off.  (This does nothing on some platforms.)
        // System.setProperty("sun.awt.noerasebackground", "true");
        Toolkit.getDefaultToolkit().setDynamicLayout(false);

        // Set up the Quaqua interface if main() decided we want it or if developer is forcing it.
        // The invokeLater() should not be necessary, but a bug in Java 7
        // requires it. Otherwise setVisible() calls on dialogs don't block
        // and other wierdness occurs.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // Emit test tables if we're in development mode.
                    // Too slow for every startup.
                    // EditableBridgeModel.printTestTables();
                    // DesignConditions.printSiteCostsTable();
                    // System.err.println("Test tables were placed in eg folder.");
            }
        });
    }

    /**
     * A vain attempt to smooth the window opening in maximized state.
     */
    @Override protected void configureWindow(java.awt.Window root) { }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of BDApp
     */

    /**
     * Return the resource map for the given class within this application.
     * 
     * @param c the class
     * @return resource map for the class
     */

    /**
     * Return the main frame for this application.
     * 
     * @return frame
     */

    /**
     * Return the file name given on the command line or null if none.
     * 
     * @return file name
     */
    public static String getFileName() {
        return fileName;
    }

    public static boolean isLegacyGraphics() {
        return legacyGraphics;
    }
    
    public static boolean isEnhancedMacUI() {
        return enhancedMacUI;
    }
    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-legacygraphics")) {
                legacyGraphics = true;
            }
            else if (args[i].equals("-noenhancedmacui")) {
                enhancedMacUI = false;
            }
            else if (fileName == null) {
                fileName = args[i];
            }
            else {
                System.err.printf("Invalid arguments in command.");
                return;
            }
        }
    }
    
    /**
     * Return an image retrieved from the application resource pool.
     * 
     * We fetch an icon because the image is embedded and it takes care of blocking until
     * the image read is complete.
     * 
     * @param name name of image to fetch
     * @return an image from the application resource pool
     */
    public Image getImageResource(String name) {
        return getIconResource(name).getImage();
    }

    /**
     * Return a buffered image taken from a graphics file in the application resource pool.
     * Argh... How many ways can the Java guys think of to represent a simple image file?
     *
     * @param name name of image to fetch
     * @return a buffered image from the application resource pool
     */
    public BufferedImage getBufferedImageResource(String name) {
        ImageIcon icon = getIconResource(name);
        Image image = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }

    /**
     * Return an icon retrieved from the application resource pool.
     * 
     * @param name name of icon to retrieve
     * @return an icon from the application resource pool
     */
    public ImageIcon getIconResource(String name) {
        // Null URLs can result from CASE mismatches in resource file name.
        URL url = null;
        // System.out.println("icon: " + name + " (" + url + ")");
        return new ImageIcon(url);
    }
    

 }

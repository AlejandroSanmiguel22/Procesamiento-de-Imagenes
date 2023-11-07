import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Plugin_Bordes implements PlugInFilter {
    ImagePlus imp;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G;
    }

    public void run(ImageProcessor ip) {
        ip.findEdges();
        
        int heigth = ip.getHeight();
        int width = ip.getWidth();
        
        byte[] pixels = (byte[]) ip.getPixels();
        
        for (int i = 0; i < heigth*width; i++) {
            pixels[i] = (byte) ( 255 - pixels[i]);
            
        }
        imp.updateAndDraw();
        
    }

}

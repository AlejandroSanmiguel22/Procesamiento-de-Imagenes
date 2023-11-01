import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Filter_BrilloM implements PlugInFilter {
    ImagePlus imp;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G;
    }

    public void run(ImageProcessor ip) {
        int pixel;
        int i;
        int x,y;
        
        int height = ip.getHeight();
        int width = ip.getWidth();
        
        byte[] pixels = (byte[]) ip.getPixels();
        int [][] pixelsM = new int[height][width];
        
        for (i = 0; i < height*width; i++) {
            pixels[i] = (byte) ( 255 - pixels[i]);
            
        }
        imp.updateAndDraw();
    }

}

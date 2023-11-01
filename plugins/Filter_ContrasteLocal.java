/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


 //@author aleja
 
//Luis Alejandro Sanmiguel Galeano 2220221058

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Filter_ContrasteLocal implements PlugInFilter {
    private int windowSize;
    private double k;

    public int setup(String arg, ImagePlus imp) {
        return DOES_8G; // Procesa imágenes de 8 bits en escala de grises
    }

    public void run(ImageProcessor ip) {
        if (!showDialog()) {
            return; // El usuario canceló el diálogo
        }

        int width = ip.getWidth();
        int height = ip.getHeight();

        // Copiamos la imagen de entrada
        ImageProcessor outputIp = ip.duplicate(); 

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Obtenemos la media local m(x, y) en la ventana definida
                double localMean = calculateLocalMean(ip, x, y, windowSize);

                // Obtenemos el valor del píxel original
                int originalValue = ip.getPixel(x, y);

                // Calculamos q(x, y) basado en el valor de k ingresado por el usuario
                double q = originalValue * k;

                // Aplicamos la transformación
                int transformedValue = (int) (localMean + q);

                // Nos aseguramos de que los valores estén en el rango [0, 255]
                transformedValue = Math.min(255, Math.max(0, transformedValue));

                // Establecermos el nuevo valor en la imagen de salida
                outputIp.putPixel(x, y, transformedValue);
            }
        }

        // Mostramos la imagen de salida
        new ImagePlus("Filtro Aplicado", outputIp).show();
    }

    private double calculateLocalMean(ImageProcessor ip, int centerX, int centerY, int windowSize) {
        
        //Inicializamos la sumatoria y count
        int sum = 0;
        int count = 0;

       for (int y = centerY - windowSize / 2; y <= centerY + windowSize / 2; y++) {
            for (int x = centerX - windowSize / 2; x <= centerX + windowSize / 2; x++) {
                // Verificar si el píxel está dentro de los límites de la imagen
                if (x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight()) {
                    // Obtener el valor del píxel en la posición (x, y)
                    int pixelValue = ip.getPixel(x, y);

                    // Sumar el valor del píxel a la suma total
                    sum += pixelValue;

                    // Incrementar el contador de píxeles incluidos en el cálculo
                    count++;
                }
            }
        }


        // Calcular la media local
        return (double) sum / count;
    }

    private boolean showDialog() {
        GenericDialog gd = new GenericDialog("Contraste Total");
        gd.addNumericField("Tamaño de la ventana:", windowSize, 0);
        gd.addNumericField("Valor de k:", k, 2);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return false;
        }

        windowSize = (int) gd.getNextNumber();
        k = gd.getNextNumber();
        return true;
    }
}


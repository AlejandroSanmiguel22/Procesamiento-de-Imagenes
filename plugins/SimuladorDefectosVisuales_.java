/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

//@author grupo Procesamiento de imagenes
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;

public class SimuladorDefectosVisuales_ implements PlugInFilter {

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor procesador) {
        GenericDialog dialog = new GenericDialog("Simulador de Defectos Visuales");
        String[] opciones = {"Miopía", "Hipermetropía", "Astigmatismo", "Cataratas", "Glaucoma", "Degeneración macular", "Daltonismo"};
        dialog.addChoice("Defecto visual:", opciones, opciones[0]);
        dialog.addSlider("Severidad:", 0, 100, 50);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        String defectoSeleccionado = dialog.getNextChoice();
        double severidad = dialog.getNextNumber();

        if ("Miopía".equals(defectoSeleccionado)) {
            procesador = simularMiopia(procesador, severidad);
        } else if ("Astigmatismo".equals(defectoSeleccionado)) {
            procesador = simularAstigmatismo(procesador, severidad);
        } else if ("Hipermetropía".equals(defectoSeleccionado)) {
            procesador = simularHipermetropia(procesador, severidad);
        }

        ImagePlus imagenActual = IJ.getImage();
        imagenActual.setProcessor(procesador);
        imagenActual.updateAndDraw();

        IJ.log("Defecto seleccionado: " + defectoSeleccionado);
        IJ.log("Severidad: " + severidad);
    }


    
    
    
    
    //Hipermetropía valor 12
    private ImageProcessor simularHipermetropia(ImageProcessor original, double severidad) {
        int width = original.getWidth();
        int height = original.getHeight();
        ImageProcessor copia = original.duplicate();

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double blurRadius = severidad * distance / Math.max(width, height);

                int sum = 0;
                int count = 0;

                for (int i = (int) -blurRadius; i <= blurRadius; i++) {
                    for (int j = (int) -blurRadius; j <= blurRadius; j++) {
                        int newX = x + i;
                        int newY = y + j;
                        if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                            sum += original.getPixel(newX, newY);
                            count++;
                        }
                    }
                }

                int blurredValue = sum / count;
                copia.putPixel(x, y, blurredValue);
            }
        }

        return copia;
    }

    //Astigmatismo valor 50
    private ImageProcessor simularAstigmatismo(ImageProcessor original, double severidad) {
        double factorEscala = 0.05;  // Ajusta este valor según tus necesidades
        double severidadAjustada = severidad * factorEscala;

        ImageProcessor copia = original.duplicate();
        copia.blurGaussian(severidadAjustada);
        return copia;
    }

    //Miopia Valor 12
    private ImageProcessor simularMiopia(ImageProcessor original, double severidad) {
        int width = original.getWidth();
        int height = original.getHeight();
        ImageProcessor copia = original.duplicate();

        boolean isColor = original instanceof ColorProcessor;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double distance = Math.sqrt((x - width / 2.0) * (x - width / 2.0) + (y - height / 2.0) * (y - height / 2.0));
                double blurRadius = 2 * severidad * distance / Math.max(width, height);

                if (isColor) {
                    int[] originalPixelValue = new int[3];
                    original.getPixel(x, y, originalPixelValue);
                    int[] blurredValue = getBlurredValueColor(original, x, y, blurRadius);
                    for (int i = 0; i < originalPixelValue.length; i++) {
                        originalPixelValue[i] = (int) (originalPixelValue[i] + (blurredValue[i] - originalPixelValue[i]));
                    }
                    copia.putPixel(x, y, originalPixelValue);
                } else {
                    int originalPixelValue = original.getPixel(x, y);
                    int blurredValue = getBlurredValueGray(original, x, y, blurRadius);
                    copia.putPixel(x, y, originalPixelValue + (blurredValue - originalPixelValue));
                }
            }
        }

        // Eliminar los márgenes negros
        int margin = 10; // Ajusta este valor según la severidad de los artefactos en tu imagen
        copia.setRoi(margin, margin, width - 2 * margin, height - 2 * margin);
        copia = copia.crop();

        return copia;
    }

    private int[] getBlurredValueColor(ImageProcessor img, int x, int y, double radius) {
        int count = 0;
        int[] sum = new int[3];

        for (int i = (int) -radius; i <= radius; i++) {
            for (int j = (int) -radius; j <= radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    int[] pixelValue = new int[3];
                    img.getPixel(x + i, y + j, pixelValue);
                    for (int k = 0; k < sum.length; k++) {
                        sum[k] += pixelValue[k];
                    }
                    count++;
                }
            }
        }

        for (int i = 0; i < sum.length; i++) {
            sum[i] /= count;
        }

        return sum;
    }

    private int getBlurredValueGray(ImageProcessor img, int x, int y, double radius) {
        int count = 0;
        int sum = 0;

        for (int i = (int) -radius; i <= radius; i++) {
            for (int j = (int) -radius; j <= radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    sum += img.getPixel(x + i, y + j);
                    count++;
                }
            }
        }

        return sum / count;
    }
}

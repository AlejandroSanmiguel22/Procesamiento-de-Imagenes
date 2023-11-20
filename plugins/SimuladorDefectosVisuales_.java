/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

//@author grupo Procesamiento de imagenes
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.Blitter;
import ij.process.ImageStatistics;
import ij.process.TypeConverter;
import ij.plugin.ContrastEnhancer;

public class SimuladorDefectosVisuales_ implements PlugInFilter {

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
public void run(ImageProcessor procesador) {
    GenericDialog dialog = new GenericDialog("Simulador de Defectos Visuales");
    String[] opciones = {"Miopía", "Hipermetropía", "Astigmatismo", "Cataratas", "Daltonismo"};
    dialog.addChoice("Defecto visual:", opciones, opciones[0]);
    dialog.addSlider("Severidad:", 0, 100, 50);

    dialog.showDialog();

    if (dialog.wasCanceled()) {
        return;
    }

    String defectoSeleccionado = dialog.getNextChoice();
    double severidad = dialog.getNextNumber();
    double radioMaximo = Math.min(procesador.getWidth(), procesador.getHeight()) / 2.0 * (severidad / 100.0);

    String tipoDaltonismo = "Deuteranopia"; // Valor predeterminado

    // Si el usuario selecciona Daltonismo, muestra otro diálogo para elegir el tipo
    if (defectoSeleccionado.equals("Daltonismo")) {
        GenericDialog dialogDaltonismo = new GenericDialog("Tipo de Daltonismo");
        String[] tiposDaltonismo = {"Deuteranopia", "Protanopia", "Tritanopia"};
        dialogDaltonismo.addChoice("Tipo de Daltonismo:", tiposDaltonismo, tiposDaltonismo[0]);
        dialogDaltonismo.showDialog();

        if (dialogDaltonismo.wasCanceled()) {
            return;
        }

        tipoDaltonismo = dialogDaltonismo.getNextChoice();
    }

    // Aplica el efecto basado en la selección del usuario
    switch (defectoSeleccionado) {
        case "Miopía":
            procesador = simularMiopia(procesador, severidad);
            break;
        case "Astigmatismo":
            procesador = simularAstigmatismo(procesador, severidad);
            break;
        case "Hipermetropía":
            procesador = simularHipermetropia(procesador, severidad, radioMaximo);
            break;
        case "Cataratas":
            procesador = simularCataratas(procesador);
            break;
        case "Daltonismo":
            procesador = simularDaltonismo(procesador, tipoDaltonismo);
            break;
        default:
            IJ.log("Defecto visual no reconocido.");
            return;
    }

    // Actualiza la imagen con el procesador modificado
    ImagePlus imagenActual = new ImagePlus("Simulación de " + defectoSeleccionado, procesador);
    imagenActual.show();

    IJ.log("Defecto seleccionado: " + defectoSeleccionado);
    IJ.log("Severidad: " + severidad);
}

    //****************************************************************************************//
    //Cataratas valor 50
    private ImageProcessor simularCataratas(ImageProcessor original) {
        // Crear una copia del procesador de imagen original para no modificar la imagen original
        ImageProcessor copia = original.duplicate();

        // Aplicar un desenfoque gaussiano para simular la visión borrosa
        copia.blurGaussian(2.0);

        // Reducir el contraste
        ContrastEnhancer enhancer = new ContrastEnhancer();
        enhancer.stretchHistogram(copia, 0.8); // Ajusta el valor según sea necesario para reducir el contraste

        // Ajustar el brillo si es necesario
        ImageStatistics stats = copia.getStatistics(); // Obtener estadísticas de la imagen
        copia.add(40); // Aumentar los valores de los píxeles para incrementar el brillo

        // Devolver la imagen procesada
        return copia;
    }
    //****************************************************************************************//
    
private ImageProcessor simularDaltonismo(ImageProcessor original, String tipoDaltonismo) {
    int width = original.getWidth();
    int height = original.getHeight();
    ImageProcessor copia = original.duplicate();

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int pixel = original.getPixel(x, y);
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            switch (tipoDaltonismo) {
                case "Deuteranopia": // Simulación de dificultad para percibir el verde
                    green = (red + blue) / 2; // Ajustar el verde basado en el rojo y el azul
                    break;
                case "Protanopia": // Simulación de dificultad para percibir el rojo
                    red = (green + blue) / 2; // Ajustar el rojo basado en el verde y el azul
                    break;
                case "Tritanopia": // Simulación de dificultad para percibir el azul
                    blue = (red + green) / 2; // Ajustar el azul basado en el rojo y el verde
                    break;
                // Puedes agregar más casos para otros tipos de daltonismo
            }

            int newColor = (red << 16) | (green << 8) | blue;
            copia.putPixel(x, y, newColor);
        }
    }

    return copia;
}

    //****************************************************************************************//
    //Hipermetropía valor 12
    private ImageProcessor simularHipermetropia(ImageProcessor original, double severidad, double radioMaximo) {
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

                // Aplicar desenfoque solo dentro de un radio máximo
                if (distance < radioMaximo) {
                    // El desenfoque aumenta con la distancia desde el centro hasta el radio máximo
                    double normalizedDistance = distance / radioMaximo;
                    double blurRadius = severidad * normalizedDistance;

                    int sumR = 0, sumG = 0, sumB = 0;
                    int count = 0;

                    for (int i = (int) -blurRadius; i <= blurRadius; i++) {
                        for (int j = (int) -blurRadius; j <= blurRadius; j++) {
                            int newX = x + i;
                            int newY = y + j;
                            if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                                int pixel = original.getPixel(newX, newY);
                                sumR += (pixel >> 16) & 0xff; // Rojo
                                sumG += (pixel >> 8) & 0xff;  // Verde
                                sumB += pixel & 0xff;         // Azul
                                count++;
                            }
                        }
                    }

                    if (count > 0) {
                        int meanR = sumR / count;
                        int meanG = sumG / count;
                        int meanB = sumB / count;
                        int meanColor = (meanR << 16) | (meanG << 8) | meanB;
                        copia.putPixel(x, y, meanColor);
                    }
                }
            }
        }

        return copia;
    }

//****************************************************************************************//
    //Astigmatismo valor 50
    private ImageProcessor simularAstigmatismo(ImageProcessor original, double severidad) {
        double factorEscala = 0.05;  // Ajusta este valor según tus necesidades
        double severidadAjustada = severidad * factorEscala;

        ImageProcessor copia = original.duplicate();
        copia.blurGaussian(severidadAjustada);
        return copia;
    }

    //****************************************************************************************//
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

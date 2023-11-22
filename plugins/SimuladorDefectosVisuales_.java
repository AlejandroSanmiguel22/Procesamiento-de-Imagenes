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
import ij.plugin.ContrastEnhancer;

public class SimuladorDefectosVisuales_ implements PlugInFilter {

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    @Override
// Método principal que se ejecuta en el plugin.
public void run(ImageProcessor procesador) {
    // Crear un diálogo para que el usuario seleccione el tipo de defecto visual y su severidad.
    GenericDialog dialog = new GenericDialog("Simulador de Defectos Visuales");
    String[] opciones = {"Miopía", "Hipermetropía", "Astigmatismo", "Cataratas", "Daltonismo"};
    dialog.addChoice("Defecto visual:", opciones, opciones[0]);
    dialog.addSlider("Severidad:", 0, 100, 50);

    // Mostrar el diálogo al usuario.
    dialog.showDialog();

    // Si el usuario cancela el diálogo, terminar la ejecución del método.
    if (dialog.wasCanceled()) {
        return;
    }

    // Obtener la elección del usuario y la severidad seleccionada.
    String defectoSeleccionado = dialog.getNextChoice();
    double severidad = dialog.getNextNumber();
    // Calcular el radio máximo para la hipermetropía basado en la severidad.
    double radioMaximo = Math.min(procesador.getWidth(), procesador.getHeight()) / 2.0 * (severidad / 100.0);

    // Valor predeterminado para el tipo de daltonismo.
    String tipoDaltonismo = "Deuteranopia";

    // Si el usuario selecciona Daltonismo, mostrar otro diálogo para elegir el tipo específico.
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

    // Aplicar el efecto visual seleccionado a la imagen.
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
            // Registrar un mensaje si se selecciona una opción no reconocida.
            IJ.log("Defecto visual no reconocido.");
            return;
    }

    // Mostrar la imagen procesada en una nueva ventana.
    ImagePlus imagenActual = new ImagePlus("Simulación de " + defectoSeleccionado, procesador);
    imagenActual.show();

    // Registrar información sobre la simulación en la consola de ImageJ.
    IJ.log("Defecto seleccionado: " + defectoSeleccionado);
    IJ.log("Severidad: " + severidad);
}


    //****************************************************************************************//
    //Cataratas valor 50
    private ImageProcessor simularCataratas(ImageProcessor original) {
      
        ImageProcessor copia = original.duplicate();

        // Aplicar un desenfoque gaussiano  con valor '2.0' que representa el radio del desenfoque.
        copia.blurGaussian(2.0);

        // Reducir el contraste de la imagen para simula la reducción de la claridad visual y la percepción de los detalles.
        ContrastEnhancer enhancer = new ContrastEnhancer();
      
        // El valor '0.8' representa el porcentaje de los píxeles que se utilizarán para el estiramiento del histograma.
        enhancer.stretchHistogram(copia, 0.8);

        // Ajustar el brillo de la imagen.
        copia.add(40); // Aumentar los valores de los píxeles para incrementar el brillo.
  
        return copia;
    }

    //****************************************************************************************//
//Daltonismo
    private ImageProcessor simularDaltonismo(ImageProcessor original, String tipoDaltonismo) {

        int width = original.getWidth();
        int height = original.getHeight();

        ImageProcessor copia = original.duplicate();

        // Recorrer todos los píxeles de la imagen.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Obtener el color del píxel actual.
                int pixel = original.getPixel(x, y);
                // Separar los componentes de color rojo, verde y azul.
                int red = (pixel >> 16) & 0xff; // Extraer el componente rojo
                int green = (pixel >> 8) & 0xff; // Extraer el componente verde
                int blue = pixel & 0xff;         // Extraer el componente azul

                // Aplicar ajustes de color basados en el tipo de daltonismo.
                switch (tipoDaltonismo) {
                    case "Deuteranopia": // Dificultad para percibir el verde
                        green = (red + blue) / 2; // Ajustar el verde basado en el rojo y el azul
                        break;
                    case "Protanopia": // Dificultad para percibir el rojo
                        red = (green + blue) / 2; // Ajustar el rojo basado en el verde y el azul
                        break;
                    case "Tritanopia": // Dificultad para percibir el azul
                        blue = (red + green) / 2; // Ajustar el azul basado en el rojo y el verde
                        break;
                    // Más casos pueden ser añadidos para otros tipos de daltonismo.
                }

                // Combinar los componentes de color ajustados para formar un nuevo color.
                int newColor = (red << 16) | (green << 8) | blue;
                // Actualizar el píxel en la copia de la imagen con el nuevo color.
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

        // Calcular el centro de la imagen.
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        // Recorrer todos los píxeles de la imagen.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calcular la distancia del píxel actual al centro de la imagen.
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // Aplicar desenfoque solo dentro de un radio máximo especificado.
                if (distance < radioMaximo) {
                    // El desenfoque aumenta con la distancia desde el centro hasta el radio máximo.
                    double normalizedDistance = distance / radioMaximo;
                    double blurRadius = severidad * normalizedDistance;

                    // Inicializar sumas de colores y contador.
                    int sumR = 0, sumG = 0, sumB = 0;
                    int count = 0;

                    // Calcular el promedio de color en un área alrededor del píxel actual basada en el radio de desenfoque.
                    for (int i = (int) -blurRadius; i <= blurRadius; i++) {
                        for (int j = (int) -blurRadius; j <= blurRadius; j++) {
                            int newX = x + i;
                            int newY = y + j;
                            // Verificar que las nuevas coordenadas estén dentro de los límites de la imagen.
                            if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                                int pixel = original.getPixel(newX, newY);
                                // Sumar los valores de color de cada píxel en el área.
                                sumR += (pixel >> 16) & 0xff; // Rojo
                                sumG += (pixel >> 8) & 0xff;  // Verde
                                sumB += pixel & 0xff;         // Azul
                                count++;
                            }
                        }
                    }

                    // Si hay píxeles en el área, calcular el color promedio.
                    if (count > 0) {
                        int meanR = sumR / count;
                        int meanG = sumG / count;
                        int meanB = sumB / count;
                        // Combinar los canales de color para formar un nuevo color.
                        int meanColor = (meanR << 16) | (meanG << 8) | meanB;
                        // Actualizar el píxel en la copia de la imagen con el color promedio.
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

        // Factor de escala para ajustar la severidad del efecto.
        double factorEscala = 0.05;
        // Ajustar la severidad con el factor de escala para obtener el efecto deseado.
        double severidadAjustada = severidad * factorEscala;

        // Crear una copia de la imagen original para aplicar modificaciones.
        ImageProcessor copia = original.duplicate();

        // Aplicar un desenfoque gaussiano a la copia de la imagen.
        copia.blurGaussian(severidadAjustada);

        // Devolver la imagen procesada.
        return copia;
    }

    //****************************************************************************************//
    //Miopia Valor 12
    private ImageProcessor simularMiopia(ImageProcessor original, double severidad) {

        // Obtener las dimensiones de la imagen original.
        int width = original.getWidth();
        int height = original.getHeight();
        // Crear una copia de la imagen original para modificarla.
        ImageProcessor copia = original.duplicate();

        // Determinar si la imagen original es en color.
        boolean isColor = original instanceof ColorProcessor;

        // Recorrer todos los píxeles de la imagen.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calcular la distancia del píxel al centro de la imagen.
                double distance = Math.sqrt((x - width / 2.0) * (x - width / 2.0) + (y - height / 2.0) * (y - height / 2.0));
                // Calcular el radio de desenfoque basado en la severidad de la miopía y la distancia al centro.
                double blurRadius = 2 * severidad * distance / Math.max(width, height);

                // Si la imagen es en color, procesar cada canal de color (RGB).
                if (isColor) {
                    // Array para almacenar los valores de los tres canales de color del píxel actual.
                    int[] originalPixelValue = new int[3];
                    // Obtener los valores de color del píxel actual.
                    original.getPixel(x, y, originalPixelValue);

                    // Obtener el valor de color desenfocado para el píxel actual.
                    int[] blurredValue = getBlurredValueColor(original, x, y, blurRadius);

                    // Mezclar los valores de color originales y desenfocados.
                    for (int i = 0; i < originalPixelValue.length; i++) {
                        // La mezcla se realiza promediando el valor original y el valor desenfocado.
                        originalPixelValue[i] = (int) (originalPixelValue[i] + (blurredValue[i] - originalPixelValue[i]));
                    }
                    // Actualizar el píxel en la copia de la imagen con los nuevos valores de color.
                    copia.putPixel(x, y, originalPixelValue);
                } else {
                    int originalPixelValue = original.getPixel(x, y);
                    int blurredValue = getBlurredValueGray(original, x, y, blurRadius);
                    copia.putPixel(x, y, originalPixelValue + (blurredValue - originalPixelValue));
                }

            }
        }

        // Eliminar los márgenes negros que pueden haber sido creados por el desenfoque.
        int margin = 10; // Ajusta este valor según la necesidad.
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

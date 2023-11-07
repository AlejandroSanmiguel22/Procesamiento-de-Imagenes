
//Luis Alejandro Sanmiguel Galeano 2220221058
// Importando bibliotecas necesarias
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

public class Plugin_RuidoSalYPimienta implements PlugInFilter {

    // Referencia a la imagen sobre la que se aplicará el filtro
    private ImagePlus referenciaImagen;
    // Porcentaje del ruido a aplicar (inicializado a 0 por defecto)
    private double porcentajeRuido = 0;

    // Método de configuración previa a la ejecución del filtro
    @Override
    public int setup(String arg, ImagePlus referenciaImagen) {
        // Cuadro de diálogo para que el usuario introduzca el porcentaje de ruido deseado
        GenericDialog dialogo = new GenericDialog("Configuración Ruido Sal y Pimienta");
        dialogo.addNumericField("Ruido (%)", porcentajeRuido, 0);
        dialogo.showDialog();

        // Si el usuario cancela, se termina la ejecución
        if (dialogo.wasCanceled()) 
            return DONE;

        // Actualizar el porcentaje de ruido con el valor introducido por el usuario
        porcentajeRuido = dialogo.getNextNumber();
        this.referenciaImagen = referenciaImagen;

        // Indicar que el plugin solo trabaja con imágenes de 8 bits (escala de grises)
        return DOES_8G;
    }

    // Método principal para aplicar el filtro a la imagen
    public void run(ImageProcessor procesadorImagen) {
        // Obtener los píxeles de la imagen
        byte[] pixelesImagen = (byte[]) procesadorImagen.getPixels();

        // Calcular el umbral basado en el porcentaje introducido
        double umbral = 1 - (porcentajeRuido / 100);

        // Aplicar ruido a cada píxel de la imagen
        for (int indice = 0; indice < pixelesImagen.length; indice++) {
            double valorAleatorio = Math.random() * 2 - 1; // valor aleatorio entre -1 y 1
            // Decidir si el píxel se convierte en sal, pimienta o se mantiene igual
            pixelesImagen[indice] = valorAleatorio < -umbral ? 0 : valorAleatorio > umbral ? (byte)255 : pixelesImagen[indice];
        }

        // Actualizar la imagen con los píxeles modificados
        referenciaImagen.updateAndDraw();
    }
}

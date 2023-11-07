/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Real_Convolver implements PlugInFilter {

    ImagePlus imp;
    int k; // Valor dado por el usuario
    float[][] matrix4 = new float[7][7]; // Matriz 4 de 7x7

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        
        // Diálogo para ingresar el valor k
        GenericDialog gd = new GenericDialog("Ajuste de Matriz");
        gd.addNumericField("Introduce valor K:", 0, 0); // 0 es el valor por defecto
        gd.showDialog();
        if (gd.wasCanceled()) {
            return DONE;
        }
        k = (int) gd.getNextNumber();

        // Matriz 1
        float[][] matrix1 = new float[7][7];
        matrix1[3][3] = k;

        // Matriz 2
        float[][] matrix2 = new float[7][7];
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                float xValue = i - 3; // Centrado en -3 a 3
                float yValue = j - 3; // Centrado en -3 a 3
                matrix2[i][j] = (float) Math.exp(-Math.sqrt(xValue*xValue + yValue*yValue) / 2);
            }
        }

        // Matriz 3
        float sumM2 = 0;
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                sumM2 += matrix2[i][j];
            }
        }
        
        float[][] matrix3 = new float[7][7];
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                matrix3[i][j] = matrix2[i][j] / sumM2;
            }
        }

        // Matriz 4
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                matrix4[i][j] = matrix1[i][j] - matrix3[i][j];
            }
        }

        return DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();

        // Duplicar el procesador de imagen para no alterar el original
        ImageProcessor result = ip.duplicate();

        for (int y = 3; y < height - 3; y++) {
            for (int x = 3; x < width - 3; x++) {
                float sum = 0;

                // Recorremos la matriz 4 y la aplicamos a los píxeles correspondientes
                for(int i = -3; i <= 3; i++) {
                    for(int j = -3; j <= 3; j++) {
                        sum += ip.getPixelValue(x + i, y + j) * matrix4[i + 3][j + 3];
                    }
                }
                
                result.putPixelValue(x, y, sum);
            }
        }

        // Mostrar resultado
        new ImagePlus("Imagen Procesada", result).show();
    }

    public static void main(String[] args) {
        new ImageJ();
        ImagePlus img = IJ.openImage("path_to_your_image");
        img.show();
        IJ.runPlugIn("Real_Convolver", "");
    }
}






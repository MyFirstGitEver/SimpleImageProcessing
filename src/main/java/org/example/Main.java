package org.example;


import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

class Transformation {
    private static void transpose(Vector[][] mat){
        for(int i=0;i<mat.length;i++){
            for(int j=i;j<mat[0].length;j++){
                Vector v = mat[i][j];
                mat[i][j] = mat[j][i];
                mat[j][i] = v;
            }
        }
    }

    public static void flipRight(Vector[][] mat){
        transpose(mat);

        int m = mat.length;
        int n = mat[0].length;

        for(int j=0;j<n / 2;j++){
            for(int i=0;i<m;i++){
                Vector temp = mat[i][j];
                mat[i][j] = mat[i][n  - j - 1];
                mat[i][n - j - 1] = temp;
            }
        }
    }

    public static void flipLeft(Vector[][] mat){
        transpose(mat);

        int m = mat.length;
        int n = mat[0].length;

        for(int i=0;i<m / 2;i++) {
            for(int j=0;j<n;j++) {
                Vector temp = mat[i][j];
                mat[i][j] = mat[m - i - 1][j];
                mat[m - i - 1][j] = temp;
            }
        }
    }

    public static Vector[][] convolve3(Vector[][] pixels, float[][] kernel){
        int m = pixels.length, n = pixels[0].length;

        Vector[][] newPixels = new Vector[m - 2][n - 2];

        for(int i=0;i<m - 2;i++){
            for(int j=0;j<n - 2;j++){
                float intensityDelta = Math.min(Math.abs(Math.round(convolve(pixels, i, j, kernel))), 255);

                newPixels[i][j] = new Vector(
                        intensityDelta,
                        intensityDelta,
                        intensityDelta);
            }
        }

        return newPixels;
    }

    public static Vector[][] convolve3Total(Vector[][] pixels, float[][] kernelX, float[][] kernelY){
        int m = pixels.length, n = pixels[0].length;

        Vector[][] newPixels = new Vector[m - 2][n - 2];

        for(int i=0;i<m - 2;i++){
            for(int j=0;j<n - 2;j++){
                float intensityX = convolve(pixels, i, j, kernelX);
                float intensityY = convolve(pixels, i, j, kernelY);

                float totalIntensity = Math.round(Math.sqrt((intensityX * intensityX) + (intensityY * intensityY)));

                totalIntensity = Math.min(totalIntensity, 255);

                newPixels[i][j] = new Vector(
                        totalIntensity,
                        totalIntensity,
                        totalIntensity);
            }
        }

        return newPixels;
    }

    private static float convolve(Vector[][] pixels, int i, int j, float[][] kernel){
        int n = kernel.length;

        float answer = 0;
        for(int x=i;x<i + n;x++){
            for(int y=j;y<j + n;y++){
                answer += pixels[x][y].x(0) * kernel[x - i][y - j];
            }
        }

        return answer;
    }
}

class Vector{
    private final float[] points;
    Vector(float... points){
        this.points = points;
    }

    Vector(int size){
        points = new float[size];
    }

    public float x(int i){
        return points[i];
    }

    public void setX(int pos, float value){
        points[pos] = value;
    }

    public int size(){
        return points.length;
    }

    public float distanceFrom(Vector x){
        if(size() != x.size()){
            return Float.NaN;
        }

        float total = 0;
        for(int i=0;i<x.size();i++){
            total += (x.x(i) - x(i)) * (x.x(i) - x(i));
        }

        return (float) Math.sqrt(total);
    }

    public void add(Vector v){
        for(int i=0;i<points.length;i++){
            points[i] += v.x(i);
        }
    }

    public Vector scaleBy(float x){
        for(int i=0;i<points.length;i++){
            points[i] *= x;
        }

        return this;
    }

    public int intRGB(){
        Color color = new Color(Math.round(points[0]), Math.round(points[1]), Math.round(points[2]));

        return color.getRGB();
    }

    public Vector iterativeMul(Vector v){
        if(points.length != v.size()){
            return null;
        }

        Vector ans = new Vector(v.size());

        for(int i=0;i<points.length;i++){
            ans.setX(i, v.x(i) * points[i]);
        }

        return ans;
    }
}

class ImageProcessing{
    private final int type;
    private final Vector[][] pixels;
    private static final Vector red = new Vector(1, 0, 0);
    private static final Vector green = new Vector(0, 1, 0);
    private static final Vector blue = new Vector(0, 0, 1);
    private final static float[][] simpleXOperator = {
            {1, 0, -1},
            {1, 0, -1},
            {1, 0, -1},
    };

    private final static float[][] simpleYOperator = {
            {1, 1, 1},
            {0, 0, 0},
            {-1, -1, -1},
    };
    public ImageProcessing(String path, int type) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));

        pixels = new Vector[image.getWidth()][image.getHeight()];

        for(int i=0;i<image.getWidth();i++){
            for(int j=0;j<image.getHeight();j++){
                Color color = new Color(image.getRGB(i, j));

                pixels[i][j] = new Vector(color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        this.type = type;
    }

    public ImageProcessing(BufferedImage image, int type){
        pixels = new Vector[image.getWidth()][image.getHeight()];

        for(int i=0;i<image.getWidth();i++){
            for(int j=0;j<image.getHeight();j++){
                Color color = new Color(image.getRGB(i, j));

                pixels[i][j] = new Vector(color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        this.type = type;
    }

    public ImageProcessing(Vector[][] pixels, int type){
        this.pixels = pixels;
        this.type = type;
    }

    public void save(String savePath) throws IOException {
        BufferedImage image = new BufferedImage(pixels.length, pixels[0].length, type);

        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[0].length;j++){
                image.setRGB(i, j, pixels[i][j].intRGB());
            }
        }

        ImageIO.write(image, "png", new File(savePath));
    }

    public ImageProcessing red(int type){
        return new ImageProcessing(eliminateChannels(red, type), type);
    }

    public ImageProcessing blue(int type){
        return new ImageProcessing(eliminateChannels(blue, type), type);
    }

    public ImageProcessing green(int type){
        return new ImageProcessing(eliminateChannels(green, type), type);
    }

    private Vector[][] eliminateChannels(Vector preservedColor, int type){
        Vector[][] image = new Vector[pixels.length][pixels[0].length];

        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[0].length;j++){
                image[i][j] = pixels[i][j].iterativeMul(preservedColor);

                if(type == BufferedImage.TYPE_BYTE_GRAY){
                    int max = (int) Math.max(image[i][j].x(0), image[i][j].x(1));
                    max = (int) Math.max(image[i][j].x(2), max);

                    image[i][j].setX(0, max);
                    image[i][j].setX(1, max);
                    image[i][j].setX(2, max);
                }
            }
        }

        return image;
    }

    public ImageProcessing flipRight(){
        Vector[][] newPixels = copyPixels();
        Transformation.flipLeft(newPixels);

        return new ImageProcessing(newPixels, type);
    }

    public ImageProcessing flipLeft(){
        Vector[][] newPixels = copyPixels();
        Transformation.flipRight(newPixels);

        return new ImageProcessing(newPixels, type);
    }

    public ImageProcessing crop(int left, int top, int right, int bottom){
        Vector[][] newPixels = new Vector[right - left + 1][bottom - top + 1];

        for(int x=left;x<=right;x++){
            for(int y=top;y<=bottom;y++){
                newPixels[x - left][y - top] = pixels[x][y];
            }
        }

        return new ImageProcessing(newPixels, type);
    }

    public ImageProcessing insert(int left, int top, int right, int bottom, ImageProcessing otherProcessor){
        ImageProcessing resized = otherProcessor.resize(right - left + 1, bottom - top + 1);

        for(int x=left;x<=right;x++){
            for(int y=top;y<=bottom;y++){
                pixels[x][y] = resized.pixels[x - left][y - top];
            }
        }

        return this;
    }

    public ImageProcessing resize(int width, int height) {
        BufferedImage image = new BufferedImage(pixels.length, pixels[0].length, type); // reconstruct the image

        for(int i=0;i<pixels.length;i++){
            for(int j=0;j<pixels[0].length;j++){
                image.setRGB(i, j, pixels[i][j].intRGB());
            }
        }

        BufferedImage resized = new BufferedImage(width, height, type);
        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();

        return new ImageProcessing(resized, type);
    }

    public ImageProcessing threshold(float threshold){
        if(type != BufferedImage.TYPE_BYTE_GRAY){
            return null;
        }

        Vector[][] newPixels = copyPixels();

        for(int i=0;i<newPixels.length;i++){
            for(int j=0;j<newPixels[0].length;j++){
                float val = 0;

                if(newPixels[i][j].x(0) > threshold){
                    val = 255;
                }

                newPixels[i][j].setX(0, val);
                newPixels[i][j].setX(1, val);
                newPixels[i][j].setX(2, val);
            }
        }

        return new ImageProcessing(newPixels, type);
    }

    public ImageProcessing verticalContour(){
        return new ImageProcessing(Transformation.convolve3(pixels, simpleXOperator), BufferedImage.TYPE_BYTE_GRAY);
    }

    public ImageProcessing horizontalContour(){
        return new ImageProcessing(Transformation.convolve3(pixels, simpleYOperator), BufferedImage.TYPE_BYTE_GRAY);
    }

    public ImageProcessing contour(){
        return new ImageProcessing(Transformation.convolve3Total(pixels,
                simpleXOperator, simpleYOperator), BufferedImage.TYPE_BYTE_GRAY);
    }

    private Vector[][] copyPixels() {
        Vector[][] newPixels = new Vector[pixels.length][pixels[0].length];

        for(int i=0;i<newPixels.length;i++){
            for(int j=0;j<newPixels[0].length;j++){
                newPixels[i][j] = pixels[i][j];
            }
        }

        return newPixels;
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
//        new ImageProcessing("cameraman.jpeg", BufferedImage.TYPE_BYTE_GRAY).threshold(80.0f).save("threshold2.png");

//        new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY).verticalContour()
//                .save("vertical contour.png");
//        new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY).horizontalContour()
//                .save("horizontal contour.png");
        //new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY).contour().save("contour.png");
//        new ImageProcessing("cameraman.jpeg", BufferedImage.TYPE_BYTE_GRAY).contour().save("contour2.png");

//        new ImageProcessing("zelda.png", BufferedImage.TYPE_BYTE_GRAY).contour().save("contour3.png");
    }

    private static void insert() throws IOException {
        ImageProcessing processor = new ImageProcessing("gray2.png", BufferedImage.TYPE_BYTE_GRAY);
        ImageProcessing lena = new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY);

        lena.insert(0, 0, 130, 130, processor).save("insert.png");
    }

    private static  void resize() throws IOException {
        ImageProcessing processor = new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY);
        processor.resize(100, 100).save("resized.png");
    }

    private static void crop() throws IOException {
        ImageProcessing processor = new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY);
        processor.crop(40, 20, 200, 200).save("crop.jpg");
    }

    private static void flip() throws IOException {
        ImageProcessing processor = new ImageProcessing("gray.jpg", BufferedImage.TYPE_BYTE_GRAY);
        processor.flipRight().save("right.jpg");
    }

    private static void grayScaleSplit() throws IOException {
        ImageProcessing processor = new ImageProcessing("test.png", BufferedImage.TYPE_INT_RGB);

        processor.red(BufferedImage.TYPE_BYTE_GRAY).save("gred.png");
        processor.blue(BufferedImage.TYPE_BYTE_GRAY).save("gblue.png");
        processor.green(BufferedImage.TYPE_BYTE_GRAY).save("ggreen.png");
    }

    private static void redGreenBlue() throws IOException {
        ImageProcessing processor = new ImageProcessing("test.png", BufferedImage.TYPE_INT_RGB);

        processor.red(BufferedImage.TYPE_INT_RGB).save("red.png");
        processor.blue(BufferedImage.TYPE_INT_RGB).save("blue.png");
        processor.green(BufferedImage.TYPE_INT_RGB).save("green.png");
    }
}
package com.rlgbs;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.IntList;

import java.util.ArrayList;

public class RLBGStippling extends PApplet {
    int n = 1000;
    ArrayList<Stipple> stipples;
    ArrayList<Stipple> borderStipples;

    StippleGenerator stippleGenerator;
    StippleGenerator whiteStippleGenerator;
    StippleGenerator blackStippleGenerator;

    StipplePainter stipplePainter;
    PImage reference, staticBackground, adjustImage, image;
    int scale = 1;
    int sc = 0;
    float th = 0;
    Options options;

    ArrayList<String> stippleCounts = new ArrayList<>();
    ArrayList<String> splitCounts = new ArrayList<>();
    ArrayList<String> mergeCounts = new ArrayList<>();

    private String sessionID;
    private String refName;

    public void settings() {
        Imp.setPApplet(this);
        refName = "dailybugle.jpg";
        reference = loadImage(refName); // Load the image into the program
        //reference.filter(PConstants.BLUR, 1);
        //reference = Imp.invert(reference);
        //reference = Imp.resize(reference, 2);
        size(reference.width * scale, reference.height * scale, P3D);
        IntList il = IntList.fromRange(1, 11);
        il.shuffle(this);
        il.resize(5);
        sessionID = il.join("");
    }

    public void setup() {
        //reference.filter(GRAY);
        options = new Options(4, 100, 1, true);
        options.setStippleSize(3, 3, 6);
        options.setHysteresis(0.6f, 0.02f);
        th = Tools.computeOtsuThreshold(reference) * 255;
        stippleGenerator = new StippleGenerator(this, reference, options, th);
        stipplePainter = new StipplePainter(this, stippleGenerator, scale);
        stipplePainter.clearBackground();
        image = reference;
    }

    public void draw() {
        image(image, 0, 0);
    }

    public void mousePressed() {
        Cell cell = stippleGenerator.wrv.getCell(mouseX / scale, mouseY / scale);
        println("Area: " + cell.area + " Avg Density: " + cell.avgDensity + " Or: " + cell.orientation + " Cv: " + cell.cv);
        println("Ecc: " + cell.eccentricity);
        boolean reversible = stippleGenerator.testReversibility(cell);
        println(reversible);
        stippleGenerator.getStipples().get(cell.index).c = color(255);
        //stipplePainter.paintCell(cell, color(255,0,0));
        println(get(mouseX, mouseY));
        image = stipplePainter.update();
    }

    public void keyPressed() {
        if (key == 'i') {
            stippleGenerator.adjustIterate(Imp.createBackground(reference, (int) th / 2));
            System.out.println(stippleGenerator.getStipples().size());
            System.out.println("Iteration " + stippleGenerator.status.iterations + " complete");
            System.out.println("Splits: " + stippleGenerator.status.splits + " Merges: " + stippleGenerator.status.merges);
            float smr = (float) stippleGenerator.status.splits / (stippleGenerator.status.merges + 1);
            System.out.println("Splits to merge ratio: " + smr);
            stipplePainter.paint();

            image = stipplePainter.getStippleImage();
        }

        if (key == '.') {
            float smr = 1;
            while (smr >= 1) {
                stippleGenerator.iterate();
                System.out.println(stippleGenerator.getStipples().size());
                System.out.println("Iteration " + stippleGenerator.status.iterations + " complete");
                System.out.println("Splits: " + stippleGenerator.status.splits + " Merges: " + stippleGenerator.status.merges);
                smr = (float) stippleGenerator.status.splits / (stippleGenerator.status.merges + 1);
                System.out.println("Splits to merge ratio: " + smr);
                stippleCounts.add(Integer.toString(stippleGenerator.getStipples().size()));
                splitCounts.add(Integer.toString(stippleGenerator.status.splits));
                mergeCounts.add(Integer.toString(stippleGenerator.status.merges));

                stipplePainter.paint();

                image = stipplePainter.getStippleImage();
                redraw();
            }
        }

        if (key == 'c') {
            stippleGenerator.connectReverseCells();
            image = stipplePainter.paint();
        }

        if (key == 'r') {
            //options.setStippleSize(4, 4, 8);
            stippleGenerator.restart(reference, options, 255 - (th / 2));
            image = reference;
        }

        if (key == 'b') {
            image = stipplePainter.getBackgroundImage();
        }

        if (key == 'x') {
            stippleGenerator.cleanEccentricCells();
            image = stipplePainter.paint();
        }

        if (key == 'o') {
            staticBackground = Imp.createBackground(reference, (int) th / 2);
            image = staticBackground;
        }

        if (key == 'p') {
            Imp.startProcess(Imp.prepareMat(stipplePainter.getBackgroundImage()));
            Imp.gaussianSmoothContours(1);
            Imp.approximateContours(2, true);
            Imp.chaikinSmoothContours(2, PConstants.PI / 6);
            PImage newBackground = Imp.drawContours(0.02, -1);
            adjustImage = newBackground;
            stipplePainter.changeBackground(newBackground);
            image = stipplePainter.getStippleImage();
            //PImage saveImage = createImage(image.width, image.height, ARGB);
            //saveImage.copy(image, 0, 0, image.width, image.height, 0, 0, saveImage.width, saveImage.height);
            //saveImage.save("background.jpg");
        }

        if (key == 'a') {
            Imp.startProcess(Imp.prepareMat(stipplePainter.getBackgroundImage()));
            PImage newBackground = Imp.drawContours(0.02, -1);
            Imp.updateContours();

            adjustImage = newBackground;
            stipplePainter.changeBackground(newBackground);
            image = stipplePainter.getStippleImage();
        }
        if (key == 's') {
            Imp.gaussianSmoothContours(5);
            PImage newBackground = Imp.drawContours(0.02, -1);
            adjustImage = newBackground;
            stipplePainter.changeBackground(newBackground);
            image = stipplePainter.getStippleImage();
        }
        if (key == 'd') {
            Imp.approximateContours(5, false);
            PImage newBackground = Imp.drawContours(0.02, -1);
            adjustImage = newBackground;
            stipplePainter.changeBackground(newBackground);
            image = stipplePainter.getStippleImage();
        }
        if (key == 'f') {
            Imp.chaikinSmoothContours(2, PConstants.PI / 2);
            PImage newBackground = Imp.drawContours(0.02, -1);
            adjustImage = newBackground;
            stipplePainter.changeBackground(newBackground);
            image = stipplePainter.getStippleImage();
        }

        if (key == 'k') {
            save("/Users/kerem/Desktop/Saved" + sessionID + sc
                    + "_" + stippleGenerator.getStipples().size() + "_" + stippleGenerator.status.iterations + ".jpg");
            sc++;
        }

        if (key == '1') {
            PImage whiteSource = Imp.createWhiteSource(reference, (int) th / 2);
            stippleGenerator.restart(whiteSource, options, 0);
            image = whiteSource;
        }

        if (key == '2') {
            PImage blackSource = Imp.createBlackSource(reference, (int) th / 2);
            stippleGenerator.restart(blackSource, options, 255);
            image = blackSource;
        }

        if (key == '3') {
            stippleGenerator.relax();
            stipplePainter.paint();
            image = stipplePainter.getStippleImage();
        }

        if (key == '4') {
            //Imp.chaikinCurves(1);
            //stipplePainter.changeBackground(Imp.drawContours(0.001));
            adjustImage = Imp.createTriMap(1);
            image = adjustImage;
        }

        if (key == '5') {
            PImage bg;
            if (adjustImage == null)
                bg = stipplePainter.getBackgroundImage();
            else
                bg = adjustImage;

            stippleGenerator.adjustRelax(bg);
            image = stipplePainter.update();
        }

        if (key == '6') {
            PImage bg;
            if (adjustImage == null)
                bg = stipplePainter.getBackgroundImage();
            else
                bg = adjustImage;

            while (!stippleGenerator.finished()) {
                stippleGenerator.adjustIterate(bg);
                System.out.println(stippleGenerator.getStipples().size());
                System.out.println("Iteration " + stippleGenerator.status.iterations + " complete");
                System.out.println("Splits: " + stippleGenerator.status.splits + " Merges: " + stippleGenerator.status.merges);
                image = stipplePainter.update();
                redraw();
            }

        }

        if (key == '7') {
            PImage source = Imp.getContourGradient(2);
            //options.setStippleSize(2, 2, 2);
            options.setStippleSize(
                    options.initialStippleDiameter / 2,
                    options.stippleSizeMin / 2,
                    options.stippleSizeMax / 2);
            stippleGenerator.restart(source, options, 255);
            //stipplePainter.changeBackground(staticBackground);
            image = source;
        }

        if (key == '8') {
            //stippleGenerator.cleanBorders(bg, 5);
            stippleGenerator.fixStippleColors(staticBackground);
            borderStipples = new ArrayList<>();
            borderStipples.addAll(stippleGenerator.getStipples());
            stipplePainter.changeBackground(staticBackground);
            image = stipplePainter.getStippleImage();
        }

        if (key == '9') {
            staticBackground = Imp.createBackground(reference, (int) th / 2);
            adjustImage = staticBackground;
            stipplePainter.changeBackground(staticBackground);
            image = stipplePainter.getStippleImage();
        }

        if (key == '0') {
            /*PImage bg = Imp.createBackground(reference, (int) (th / 2));
            stipplePainter.changeBackground(bg);
            image = stipplePainter.getStippleImage();*/
            //Imp.updateContours();
            Imp.updateContours();
            stipplePainter.paintStipplesOnContours(Imp.getPolygons());
            image = stipplePainter.getStippleImage();
//            stippleGenerator.addStipples(borderStipples);
//            stipplePainter.changeBackground(staticBackground);
//            image = stipplePainter.getStippleImage();
        }

        if (key == 'm') {
            //PImage gradient = adjustImage.get();
            //gradient.filter(BLUR, 5);

            //image = gradient;

            image = stippleGenerator.modifyDensityMatrix(adjustImage, 2);
        }

        if (key == 'v') {
            stipplePainter.saveSVG();
        }

        if (key == 'd') {
            saveStrings("/Users/kerem/Desktop/stipplecount.txt", stippleCounts.toArray(new String[0]));
            saveStrings("/Users/kerem/Desktop/splitcount.txt", splitCounts.toArray(new String[0]));
            saveStrings("/Users/kerem/Desktop/mergecount.txt", mergeCounts.toArray(new String[0]));

        }
    }

    public static void main(String[] args) {
        PApplet.main("com.rlgbs.RLBGStippling", args);
    }
}

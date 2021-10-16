/*
 * Image/J Plugins
 * Copyright (C) 2002-2021 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at https://github.com/ij-plugins/ijp-color/
 */

package ij_plugins.color.calibration;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.FloatProcessor;
import ij_plugins.color.calibration.chart.ColorCharts;
import ij_plugins.color.calibration.chart.GridColorChart;

import java.io.File;
import java.io.IOException;

/**
 * Example of batch color calibration - Java version
 */
public class BatchCalibrationDemoJ {
    public static void main(String[] args) throws Exception {
        File baseDir = new File("../test/data/batch_correction");
        File srcDir = new File(baseDir, "src");
        File dstDir = new File(baseDir, "dst");
        File chartImageFile = new File(srcDir, "im_1.jpg");
        File chartRoiFile = new File(srcDir, "im_1_chart.roi");


        System.out.println("Load chart image and ROI");
        ImagePlus chartImage = IJ.openImage(chartImageFile.getCanonicalPath());

        Roi chartRoi = new RoiDecoder(chartRoiFile.getCanonicalPath()).getRoi();

        System.out.println("Create calibration recipe.");
        CorrectionRecipe recipe = createCalibrationRecipe(chartImage, chartRoi);

        File[] imageFiles = srcDir.listFiles((dir, name) -> name.endsWith(".jpg"));
        if (imageFiles == null) {
            throw new Exception("Error creating list of files in: " + srcDir.getCanonicalPath());
        }

        for (File file : imageFiles) {
            System.out.println("Correcting " + file.getName());
            correct(recipe, file, dstDir);
        }

        System.out.println("Done.");
    }

    static CorrectionRecipe createCalibrationRecipe(ImagePlus chartImage, Roi chartRoi) {
        // Create a ColorGauge default calibration chart and align it to ROI from our chart image
        GridColorChart chart = ColorCharts.ImageScienceColorGaugeMatte().copyAlignedTo(chartRoi);

        String refColorSpaceName = "sRGB";
        String mappingMethodName = "Linear Cross-band";
        ColorCalibrator colorCalibrator = ColorCalibrator.apply(chart, refColorSpaceName, mappingMethodName);
        ColorCalibrator.CalibrationFit calibrationFit = colorCalibrator.computeCalibrationMapping(chartImage);
        int imageType = chartImage.getType();

        return CorrectionRecipe.apply(calibrationFit.corrector(), chart.colorConverter(), colorCalibrator.referenceColorSpace(), imageType);
    }

    static void correct(CorrectionRecipe recipe, File srcImageFile, File dstDir) throws IOException {
        ImagePlus imp = IJ.openImage(srcImageFile.getCanonicalPath());

        FloatProcessor[] correctedBands = recipe.corrector().map(imp);

        ImagePlus img = CalibrationUtils.convertToSRGB(correctedBands, recipe.referenceColorSpace(), recipe.colorConverter());

        File dstFile = new File(dstDir, srcImageFile.getName());
        dstFile.getParentFile().mkdirs();
        IJ.save(img, dstFile.getCanonicalPath());
    }
}

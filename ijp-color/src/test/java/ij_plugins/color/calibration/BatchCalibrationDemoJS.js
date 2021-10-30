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

/**
 * Example of batch color calibration.
 *
 * First a color correction recipe is computed using a reference color chart and ROI location of chart in the image.
 *
 * Next correction recipe is applied to all images in an input directory.
 *
 * Corrected Images are saved to the output directory.
 */

importPackage(Packages.ij_plugins.color.calibration)
importPackage(Packages.ij_plugins.color.calibration.chart)
importClass(Packages.java.io.File)
importClass(Packages.java.io.IOException)


IJ.log("Batch calibration - start...")

// Locations of images
baseDir = new File("../../test/data/batch_correction")
srcDir = new File(baseDir, "src");
dstDir = new File(baseDir, "dst");
chartImageFile = new File(srcDir, "im_1.jpg");
chartRoiFile = new File(srcDir, "im_1_chart.roi");


// Create color calibration recipe using an image of a color chart
IJ.log("Load chart image and ROI");
chartImage = IJ.openImage(chartImageFile.getCanonicalPath());
chartRoi = new RoiDecoder(chartRoiFile.getCanonicalPath()).getRoi();

IJ.log("Create calibration recipe.");
recipe = createCalibrationRecipe(chartImage, chartRoi);

// List files with extension ".jpg"
imageFiles = Java
    .from(srcDir.listFiles())
    .filter(function (val) {
        return val.getName().endsWith(".jpg")
    })
IJ.log("Got " + imageFiles.length + " images to convert...");


// Iterate trough images and correct
for (let i = 0; i < imageFiles.length; i++) {
    file = imageFiles[i]
    IJ.log("Correcting " + file.getName());
    correct(recipe, file, dstDir);
}


IJ.log("Batch calibration - done.")


/**
 * Create color calibration recipe
 * @param chartImage image of a chart (assumed to be ImageScience ColorGauge Matte) [ImagePlus]
 * @param chartRoi location of the chart [Roi]
 * @returns calibration recipe
 */
function createCalibrationRecipe(chartImage, chartRoi) {
    // Create a ColorGauge default calibration chart and align it to ROI from our chart image
    chart = ColorCharts.ImageScienceColorGaugeMatte().copyAlignedTo(chartRoi);

    refColorSpaceName = "sRGB";
    mappingMethodName = "Linear Cross-band";
    colorCalibrator = ColorCalibrator.apply(chart, refColorSpaceName, mappingMethodName);
    calibrationFit = colorCalibrator.computeCalibrationMapping(chartImage);
    imageType = chartImage.getType();

    return CorrectionRecipe.apply(
        calibrationFit.corrector(),
        chart.colorConverter(),
        colorCalibrator.referenceColorSpace(),
        imageType);
}


/**
 * Apply color calibration recipe
 * @param recipe recipe to apply [CorrectionRecipe]
 * @param srcImageFile file containing the image to correct [File]
 * @param dstDir directory where corrected image should be saved [File]
 */
function correct(recipe, srcImageFile, dstDir) {
    imp = IJ.openImage(srcImageFile.getCanonicalPath());

    correctedBands = recipe.corrector().map(imp);

    img = CalibrationUtils.convertToSRGB(correctedBands, recipe.referenceColorSpace(), recipe.colorConverter());

    dstFile = new File(dstDir, srcImageFile.getName());
    IJ.save(img, dstFile.getCanonicalPath());
}

/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.qrcode;

import com.google.common.collect.Lists;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class QRCodeEncoderDecoder {


    private static final Map<DecodeHintType, Object> HINTS;
    private static final Map<DecodeHintType, Object> HINTS_PURE;

    static {
        HINTS = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        HINTS.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
        HINTS_PURE = new EnumMap<DecodeHintType, Object>(HINTS);
        HINTS_PURE.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
    }

    private final Logger log = LoggerFactory.getLogger(QRCodeEncoderDecoder.class);

    private int width;
    private int height;

    public QRCodeEncoderDecoder(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BufferedImage encode(String data) {
        // get a byte matrix for the data
        BitMatrix matrix;
        com.google.zxing.Writer writer = new QRCodeWriter();
        try {
            matrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
        } catch (com.google.zxing.WriterException e) {
            // exit the method
            return null;
        }

        // generate an image from the byte matrix
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        // create buffered image to draw to
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // iterate through the matrix and draw the pixels to the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean imageValue = matrix.get(x, y);
                image.setRGB(x, y, (imageValue ? 0 : 0xFFFFFF));
            }
        }

        // //write the image to the output stream
        // ImageIO.write(image, "png", outputStream);

        return image;
    }

    public static String decode(BufferedImage image) {

        // convert the image to a binary bitmap source
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        // decode the barcode
        QRCodeReader reader = new QRCodeReader();

        try {
            @SuppressWarnings("rawtypes")
            Hashtable hints = new Hashtable();
            Result result = reader.decode(bitmap, hints);


            return result.getText();
        } catch (ReaderException e) {
            // the data is improperly formatted
        }

        return "";
    }

    public static String decode(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            return processImage(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
    }

    private static String processImage(BufferedImage image) throws IOException {

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        List<Result> results = Lists.newArrayListWithCapacity(1);

        try {

            Reader reader = new MultiFormatReader();

            try {
                // Look for multiple barcodes
                MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
                Result[] theResults = multiReader.decodeMultiple(bitmap, HINTS);
                if (theResults != null) {
                    results.addAll(Arrays.asList(theResults));
                }
            } catch (ReaderException re) {

            }

            if (results.isEmpty()) {
                try {
                    // Look for pure barcode
                    Result theResult = reader.decode(bitmap, HINTS_PURE);
                    if (theResult != null) {
                        results.add(theResult);
                    }
                } catch (ReaderException re) {

                }
            }

            if (results.isEmpty()) {
                try {
                    // Look for normal barcode in photo
                    Result theResult = reader.decode(bitmap, HINTS);
                    if (theResult != null) {
                        results.add(theResult);
                    }
                } catch (ReaderException re) {

                }
            }

            if (results.isEmpty()) {
                try {
                    // Try again with other binarizer
                    BinaryBitmap hybridBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    Result theResult = reader.decode(hybridBitmap, HINTS);
                    if (theResult != null) {
                        results.add(theResult);
                    }
                } catch (ReaderException re) {

                }
            }


        } catch (RuntimeException re) {
            re.printStackTrace();
        }
        if (results.size() > 0) {
            Result result = results.get(0);
            if (result != null && result.getText() != null) {
                return result.getText();
            }
        }
        return null;
    }


}

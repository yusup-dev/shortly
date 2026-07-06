package com.shortly.apiservice.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public QrImage generate(String shortUrl, String shortKey, int size, String format) {
        String normalizedFormat = (format == null || format.isBlank()) ? "png" : format.toLowerCase();

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(shortUrl, BarcodeFormat.QR_CODE, size, size, hints);

            if ("svg".equals(normalizedFormat)) {
                return new QrImage(toSvg(matrix).getBytes(), "image/svg+xml", "svg");
            }

            BufferedImage image = toBufferedImage(matrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return new QrImage(out.toByteArray(), "image/png", "png");

        } catch (WriterException | java.io.IOException e) {
            log.error("Failed to generate QR code for shortKey={}", shortKey, e);
            throw new ApplicationException(ExceptionType.INTERNAL_SERVER_ERROR, "Failed to generate QR code");
        }
    }

    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        return image;
    }

    private String toSvg(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(width).append(" ").append(height)
                .append("\" shape-rendering=\"crispEdges\">");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>");

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                            .append("\" width=\"1\" height=\"1\" fill=\"#000000\"/>");
                }
            }
        }
        svg.append("</svg>");
        return svg.toString();
    }
}

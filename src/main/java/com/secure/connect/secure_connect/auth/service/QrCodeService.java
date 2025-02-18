package com.secure.connect.secure_connect.auth.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.secure.connect.secure_connect.exception.QrCodeGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QrCodeService {

    public static String getQRCode(String data) {
        int width = 250;
        int height = 250;
        String imageFormat = "PNG";

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, outputStream);

            byte[] pngData = outputStream.toByteArray();

            log.info("QR Code gerado com sucesso para os dados informados.");

            return "data:image/png;base64," + Base64.encodeBase64String(pngData);
        } catch (WriterException | IOException e) {
            log.error("Não foi possível gerar o QR Code.", e);
            throw new QrCodeGenerationException("Não foi possível gerar o QR Code.", e);
        }
    }
}

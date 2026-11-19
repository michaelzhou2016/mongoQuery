package com.example.mongo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Service
public class CompressStringUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CompressStringUtil.class);

    /**
     * String 객체를 압축하여 String 으로 리턴한다.
     *
     * @param string
     * @return
     */
    public synchronized String compressString(String string) {
        return byteToString(compress(string));
    }

    /**
     * 압축된 스트링을 복귀시켜서 String 으로 리턴한다.
     *
     * @param compressed
     * @return
     */
    public synchronized String decompressString(String compressed) {
        String decompressedMsg = compressed;
        try {
            decompressedMsg = decompress(hexToByteArray(compressed));
        } catch (AssertionError | NumberFormatException assertionError) {//Exception이 난다면 compress 이전 데이터
        } catch (Exception e) {
            LOG.error("", e);
        }
        return decompressedMsg;
    }

    /**
     * 압축해제 된 파라미터가 들어간 Map을 만들어서 리턴한다.
     *
     * @param map, string
     * @return
     */
    public synchronized HMap decompressMap(HMap map, String decompressParam) {
        for (String key : map.keySet()) {
            if (key.equals(decompressParam)) {
                String decompressedMsg = decompressString(map.get(key).toString());
                map.put(decompressParam, decompressedMsg);
            }
        }
        return map;
    }

    private static String byteToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        try {
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
        } catch (Exception e) {
            LOG.error("", e);
            return null;
        }

        return sb.toString();
    }

    private static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.close();
        } catch (UnsupportedEncodingException e) {
            LOG.error("", e);
            return null;
        } catch (IOException e) {
            LOG.error("", e);
            return null;
        }
        return baos.toByteArray();
    }


    private static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("", e);
            throw new AssertionError(e);
        }
    }

    /**
     * 16진 문자열을 byte 배열로 변환한다.
     */
    private static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return new byte[]{};
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            bytes[(int) Math.floor(i / 2)] = value;
        }
        return bytes;
    }


}

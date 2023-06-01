package com.chinagoods.bigdata.functions.utils.img;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Phash 算法实现
 * @author luoweifu
 **/
public class PHash {
    public static final Logger logger = LoggerFactory.getLogger(PHash.class);

    public static final Integer DEFAULT_N = 180;

    public static final Integer DCT_N = 16;

    public PHash() {
    }

    /**
     * 获得图像特征值
     * @param imagePath 图像路径
     * @return 返回图像phash特征值
     */
    public static String getFeatureValue(String imagePath) {
        logger.debug("获取特征图片路径为: {}, 图片宽: {}， 图片高: {}", imagePath, DEFAULT_N, DEFAULT_N);
        // 缩小尺寸，简化色彩
        int[][] grayMatrix = PHash.getGrayPixel(imagePath, DEFAULT_N, DEFAULT_N);
        if (grayMatrix == null) {
            return null;
        }
        // 计算DCT
        dct(grayMatrix, DEFAULT_N);
        // 缩小DCT，计算平均值
        int[][] newMatrix = new int[DCT_N][DCT_N];
        double average = 0;
        for(int i = 0; i < DCT_N; i++){
            for(int j = 0; j < DCT_N; j++){
                newMatrix[i][j] = grayMatrix[i][j];
                average += grayMatrix[i][j];
            }
        }
        average /= (DCT_N * DCT_N);

        // 计算hash值
        StringBuilder sb = new StringBuilder();
        int pos4Sum = 0;
        for(int i = 0; i < DCT_N; i++) {
            for(int j = 0; j < DCT_N; j++){
                // 若当前位置为4的倍数，则将数值转换为16进制字符
                if (j > 0 && j % 4 == 0) {
                    sb.append(Integer.toHexString(pos4Sum));
                    pos4Sum = 0;
                }
                if(newMatrix[i][j] < average){
                    pos4Sum<<=1;
                } else{
                    pos4Sum = (pos4Sum << 1) + 1;
                }
            }
            sb.append(Integer.toHexString(pos4Sum));
            pos4Sum = 0;
        }
        return sb.toString();
    }

    /**
     * 得到灰度像素图矩阵
     * @param imagePath 图像路径
     * @param width 宽
     * @param height 高
     * @return 返回图像灰度化矩阵
     */
    public static int[][] getGrayPixel(String imagePath, int width, int height) {
        logger.debug("获取特征图片路径为: {}, 宽： {}， 高: {}", imagePath, width, height);
        BufferedImage bi;
        try {
            bi = resizeImage(imagePath, width, height, BufferedImage.TYPE_INT_RGB);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}图片缩放失败， 错误原因为: ", imagePath, e);
            return null;
        }
        int minx = bi.getMinX();
        int miny = bi.getMinY();
        int[][] matrix = new int[width - minx][height - miny];
        for (int i = minx; i < width; i++) {
            for (int j = miny; j < height; j++) {
                int pixel = bi.getRGB(i, j);
                int red = (pixel & 0xff0000) >> 16;
                int green = (pixel & 0xff00) >> 8;
                int blue = (pixel & 0xff);
                int gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                matrix[i][j] = gray;
            }
        }
        return matrix;
    }

    /**
     * 缩放图片大小，将图片缩放成指定格式和指定宽高图片
     * @param srcImgPath 原始图片路径
     * @param width 转换的宽
     * @param height 转换的高
     * @param imageType 图片类型
     * @return 图片缓存内容
     */
    public static BufferedImage resizeImage(String srcImgPath, int width, int height, int imageType)
            throws IOException {
        File srcFile = new File(srcImgPath);
        BufferedImage srcImg = ImageIO.read(srcFile);
        BufferedImage buffImg;
        buffImg = new BufferedImage(width, height, imageType);
        buffImg.getGraphics().drawImage(srcImg.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return buffImg;
    }

    /**
     * 用于计算pHash的相似度<br>
     * 相似度为1时，图片最相似
     * @param phashCode1 图像1 phash code值
     * @param phashCode2 图像2 phash code值
     * @return 两者相似度，若返回1，则说明图片一样
     */
    public static double calculateSimilarity(String phashCode1, String phashCode2) {
        int num = 0;
        for(int i = 0; i < DCT_N * DCT_N; i++){
            if(phashCode1.charAt(i) == phashCode2.charAt(i)){
                num++;
            }
        }
        return ((double)num) / (DCT_N * DCT_N * 1.0);
    }

    /**
     * 离散余弦变换
     * @author luoweifu
     * @param pix 原图像的数据矩阵
     * @param matrixN 原图像(n*n)的高或宽
     */
    public static void dct(int[][] pix, int matrixN) {
        double[][] iMatrix = new double[matrixN][matrixN];
        for (int i = 0; i < matrixN; i++) {
            for (int j = 0; j < matrixN; j++) {
                iMatrix[i][j] = pix[i][j];
            }
        }
        // 求系数矩阵
        double[][] quotient = coefficient(matrixN);
        // 转置系数矩阵
        double[][] quotientT = transposingMatrix(quotient, matrixN);

        double[][] temp;
        temp = matrixMultiply(quotient, iMatrix, matrixN);
        iMatrix = matrixMultiply(temp, quotientT, matrixN);

        for (int i = 0; i < matrixN; i++) {
            for (int j = 0; j < matrixN; j++) {
                pix[i][j] = (int) (iMatrix[i][j]);
            }
        }
    }

    /**
     * 求离散余弦变换的系数矩阵
     * @author luoweifu
     * @param matrixN n*n矩阵的大小
     * @return 系数矩阵
     */
    private static double[][] coefficient(int matrixN) {
        double[][] coeff = new double[matrixN][matrixN];
        double sqrt = 1.0 / Math.sqrt(matrixN);
        for (int i = 0; i < matrixN; i++) {
            coeff[0][i] = sqrt;
        }
        for (int i = 1; i < matrixN; i++) {
            for (int j = 0; j < matrixN; j++) {
                coeff[i][j] = Math.sqrt(2.0 / matrixN) * Math.cos(i * Math.PI * (j + 0.5) / (double) matrixN);
            }
        }
        return coeff;
    }

    /**
     * 矩阵转置
     * @author luoweifu
     * @param matrix 原矩阵
     * @param matrixN 矩阵(n*n)的高或宽
     * @return 转置后的矩阵
     */
    private static double[][] transposingMatrix(double[][] matrix, int matrixN) {
        double[][] nMatrix = new double[matrixN][matrixN];
        for (int i = 0; i < matrixN; i++) {
            for (int j = 0; j < matrixN; j++) {
                nMatrix[i][j] = matrix[j][i];
            }
        }
        return nMatrix;
    }

    /**
     * 矩阵相乘
     * @author luoweifu
     * @param matrixA 矩阵A
     * @param matrixB 矩阵B
     * @param matrixN 矩阵的大小n*n
     * @return 结果矩阵
     */
    private static double[][] matrixMultiply(double[][] matrixA, double[][] matrixB, int matrixN) {
        double[][] nMatrix = new double[matrixN][matrixN];
        int t;
        for (int i = 0; i < matrixN; i++) {
            for (int j = 0; j < matrixN; j++) {
                t = 0;
                for (int k = 0; k < matrixN; k++) {
                    t += matrixA[i][k] * matrixB[k][j];
                }
                nMatrix[i][j] = t;
            }
        }
        return nMatrix;
    }
}

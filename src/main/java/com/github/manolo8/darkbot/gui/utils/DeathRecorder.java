package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.utils.LogUtils;
import eu.darkbot.util.Timer;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;

import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeathRecorder {
    public static final int FPS = 4;

    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final int PICTURE_LENGTH = WIDTH * HEIGHT * 3;

    private static final int MAX_FRAMES = 100;

    private final byte[] bitmapBuffer = new byte[PICTURE_LENGTH];
    private final byte[] compressionBuffer = new byte[PICTURE_LENGTH];

    private final List<CompressedFrame> compressedFrames = new ArrayList<>(MAX_FRAMES);
    private final BufferedImage imageCache = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    private final Timer frameTimer = Timer.get(1000 / FPS);

    private final JFrame mainGui;
    private final LZ4Compressor compressor;
    private final LZ4FastDecompressor decompressor;

    private int currentFrame, validFrames;
    private boolean saving;

    public DeathRecorder(JFrame mainGui) {
        this.mainGui = mainGui;

        LZ4Factory factory = LZ4Factory.fastestInstance();
        this.compressor = factory.fastCompressor();
        this.decompressor = factory.fastDecompressor();
    }

    public void onTick() {
        if (frameTimer.tryActivate()) {
            saveFrame();
        }
    }

    public void onDeath() {
        synchronized (this) {
            if (saving) return;
            saving = true;
        }

        new Thread(() -> {
            try {
                saveVideo();
            } catch (IOException e) {
                e.printStackTrace();
            }

            validFrames = currentFrame = 0;
            saving = false;
        }).start();
    }

    private synchronized void saveFrame() {
        if (saving) return;
        Graphics2D g2 = (Graphics2D) imageCache.getGraphics();

        // cut native border from FlatLaf - only on Windows?
        double frameWidth = mainGui.getWidth() - 16;
        double frameHeight = mainGui.getHeight() - 8;

        g2.scale(WIDTH / frameWidth, HEIGHT / frameHeight);
        g2.translate(-8, 0);
        mainGui.print(g2);
        //g2.dispose();

        for (int offset = 0, h = 0; h < HEIGHT; h++) {
            for (int w = 0; w < WIDTH; w++) {
                int v = imageCache.getRGB(w, h);
                bitmapBuffer[offset++] = (byte) (((v >>> 16) & 0xff) - 128);
                bitmapBuffer[offset++] = (byte) (((v >>> 8) & 0xff) - 128);
                bitmapBuffer[offset++] = (byte) (((v) & 0xff) - 128);
            }
        }

        CompressedFrame compressedImage;
        if (compressedFrames.size() <= currentFrame) {
            compressedFrames.add(compressedImage = new CompressedFrame());
        } else {
            compressedImage = compressedFrames.get(currentFrame);
        }
        compressedImage.compress();

        if (++currentFrame >= MAX_FRAMES)
            currentFrame = 0;

        if (validFrames < MAX_FRAMES)
            validFrames++;
    }

    private void saveVideo() throws IOException {
        if (validFrames > 0) {
            long time = System.currentTimeMillis();

            File outputFile = new File("logs/" + LocalDateTime.now().format(LogUtils.FILENAME_DATE) + ".mov");
            SequenceEncoder sequenceEncoder = SequenceEncoder.createSequenceEncoder(outputFile, FPS);

            Picture picture = Picture.create(WIDTH, HEIGHT, ColorSpace.RGB);
            for (int i = 0; i < validFrames; i++) {
                int frame = (currentFrame + i) % validFrames;

                CompressedFrame compressedFrame = compressedFrames.get(frame);
                compressedFrame.decompressToPicture(picture);

                sequenceEncoder.encodeNativeFrame(picture);
            }
            sequenceEncoder.finish();
            System.out.println("Saved video in: " + (System.currentTimeMillis() - time) + "ms | " + validFrames);
        }
    }

    private class CompressedFrame {
        private static final int MAX_COMPRESSED_LENGTH = 128_000;

        private byte[] compressed;
        private int size;

        private void compress() {
            size = compressor.compress(bitmapBuffer, compressionBuffer);

            if (size > MAX_COMPRESSED_LENGTH) {
                size = 0;
                return;
            }

            if (compressed == null || compressed.length < size) {
                compressed = new byte[(int) Math.min(MAX_COMPRESSED_LENGTH, size * 1.1)];
            }

            System.arraycopy(compressionBuffer, 0, compressed, 0, size);
        }

        private void decompressToPicture(Picture picture) {
            if (size == 0) return; // keep old data?

            byte[] data = picture.getPlaneData(0);
            decompressor.decompress(compressed, data, PICTURE_LENGTH);
        }
    }
}

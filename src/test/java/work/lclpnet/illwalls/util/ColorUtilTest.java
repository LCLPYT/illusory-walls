package work.lclpnet.illwalls.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorUtilTest {

    @ParameterizedTest
    @MethodSource("hsvRgbSamples")
    void testHsvToRgb(HsvRgbSample sample) {
        int rgb = ColorUtil.hsvToRgb(sample.h(), sample.s(), sample.v());
        assertEquals(sample.rgb(), rgb);
    }

    @Test
    void testRgbPacked() {
        assertEquals(0xff0000, ColorUtil.getRgbPacked(255, 0, 0));
        assertEquals(0x00ff00, ColorUtil.getRgbPacked(0, 255, 0));
        assertEquals(0x0000ff, ColorUtil.getRgbPacked(0, 0, 255));
        assertEquals(0x000000, ColorUtil.getRgbPacked(0, 0, 0));
        assertEquals(0xffffff, ColorUtil.getRgbPacked(255, 255, 255));
        assertEquals(0xf07f02, ColorUtil.getRgbPacked(240, 127, 2));
    }

    @Test
    void testSetArgbPackedAlpha() {
        assertEquals(0xff000000, ColorUtil.setArgbPackedAlpha(0x000000, 255));
        assertEquals(0xffffffff, ColorUtil.setArgbPackedAlpha(0xffffff, 255));
        assertEquals(0x00ffffff, ColorUtil.setArgbPackedAlpha(0xffffff, 0));
        assertEquals(0x00000000, ColorUtil.setArgbPackedAlpha(0x000000, 0));
        assertEquals(0xf001f030, ColorUtil.setArgbPackedAlpha(0x01f030, 240));
    }

    private static Stream<HsvRgbSample> hsvRgbSamples() {
        return Stream.of(
                new HsvRgbSample(44, 0.99f, 0.99f, 0xfcba03),
                new HsvRgbSample(220, 0.68f, 0.64f, 0x3459a3),
                new HsvRgbSample(110, 0.37f, 0.52f, 0x5c8554),
                new HsvRgbSample(220, 0.00f, 1.00f, 0xffffff),
                new HsvRgbSample(57, 0.00f, 1.00f, 0xffffff),
                new HsvRgbSample(111, 0.00f, 0.00f, 0x000000),
                new HsvRgbSample(312, 0.78f, 0.00f, 0x000000)
        );
    }

    private record HsvRgbSample(float h, float s, float v, int rgb) {}
}
// Part of SourceAFIS: https://sourceafis.machinezoo.com
package com.machinezoo.sourceafis;

import static org.junit.Assert.*;
import java.io.*;
import org.apache.commons.io.*;
import org.junit.*;
import com.machinezoo.noexception.*;

public class FingerprintTemplateTest {
	private static FingerprintTemplate t = new FingerprintTemplate();
	public static FingerprintTemplate probe() {
		return new FingerprintTemplate().create(load("probe.png"));
	}
	public static FingerprintTemplate matching() {
		return new FingerprintTemplate().create(load("matching.png"));
	}
	public static FingerprintTemplate nonmatching() {
		return new FingerprintTemplate().create(load("nonmatching.png"));
	}
	public static FingerprintTemplate probeIso() {
		return new FingerprintTemplate().convert(load("iso-probe.dat"));
	}
	public static FingerprintTemplate matchingIso() {
		return new FingerprintTemplate().convert(load("iso-matching.dat"));
	}
	public static FingerprintTemplate nonmatchingIso() {
		return new FingerprintTemplate().convert(load("iso-nonmatching.dat"));
	}
	@Test public void constructor() {
		new FingerprintTemplate().create(load("probe.png"));
	}
	@Test public void decodeImage_png() {
		decodeImage_validate(TemplateBuilder.decodeImage(load("probe.png")));
	}
	@Test public void decodeImage_jpeg() {
		decodeImage_validate(TemplateBuilder.decodeImage(load("probe.jpeg")));
	}
	@Test public void decodeImage_bmp() {
		decodeImage_validate(TemplateBuilder.decodeImage(load("probe.bmp")));
	}
	@Test public void decodeImage_tiff() {
		decodeImage_validate(TemplateBuilder.decodeImage(load("probe.tiff")));
	}
	@Test public void decodeImage_wsq() {
		decodeImage_validate(TemplateBuilder.decodeImage(load("wsq-original.wsq")), TemplateBuilder.decodeImage(load("wsq-converted.png")));
	}
	private void decodeImage_validate(DoubleMap map) {
		decodeImage_validate(map, TemplateBuilder.decodeImage(load("probe.png")));
	}
	private void decodeImage_validate(DoubleMap map, DoubleMap reference) {
		assertEquals(reference.width, map.width);
		assertEquals(reference.height, map.height);
		double delta = 0, max = -1, min = 1;
		for (int x = 0; x < map.width; ++x) {
			for (int y = 0; y < map.height; ++y) {
				delta += Math.abs(map.get(x, y) - reference.get(x, y));
				max = Math.max(max, map.get(x, y));
				min = Math.min(min, map.get(x, y));
			}
		}
		assertTrue(max > 0.75);
		assertTrue(min < 0.1);
		assertTrue(delta / (map.width * map.height) < 0.01);
	}
	@Test public void json_roundTrip() {
		TemplateBuilder tb = new TemplateBuilder();
		tb.size = new Cell(800, 600);
		tb.minutiae = new Minutia[] {
			new Minutia(new Cell(100, 200), Math.PI, MinutiaType.BIFURCATION),
			new Minutia(new Cell(300, 400), 0.5 * Math.PI, MinutiaType.ENDING)
		};
		t.immutable = new ImmutableTemplate(tb);
		t = new FingerprintTemplate().deserialize(t.serialize());
		assertEquals(2, t.immutable.minutiae.length);
		Minutia a = t.immutable.minutiae[0];
		Minutia b = t.immutable.minutiae[1];
		assertEquals(new Cell(100, 200), a.position);
		assertEquals(Math.PI, a.direction, 0.0000001);
		assertEquals(MinutiaType.BIFURCATION, a.type);
		assertEquals(new Cell(300, 400), b.position);
		assertEquals(0.5 * Math.PI, b.direction, 0.0000001);
		assertEquals(MinutiaType.ENDING, b.type);
	}
	private static byte[] load(String name) {
		return Exceptions.sneak().get(() -> {
			try (InputStream input = FingerprintTemplateTest.class.getResourceAsStream("/com/machinezoo/sourceafis/" + name)) {
				return IOUtils.toByteArray(input);
			}
		});
	}
}

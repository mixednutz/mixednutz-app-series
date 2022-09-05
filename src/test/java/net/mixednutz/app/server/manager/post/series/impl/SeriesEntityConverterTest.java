package net.mixednutz.app.server.manager.post.series.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.mixednutz.app.server.entity.post.series.Series;

public class SeriesEntityConverterTest {
	
	@Test
	public void test() {
		SeriesEntityConverter converter = new SeriesEntityConverter();
		Series series = new Series();
		assertTrue(converter.canConvert(series.getClass()));
	}

}

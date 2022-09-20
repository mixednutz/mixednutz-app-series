package net.mixednutz.app.server.format;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

public class EpubHtmlFilterTest {
	
	private Resource sampleFile = new ClassPathResource("test_epub_chapter.html");
	
	@Test
	public void test_filter() {
		EpubHtmlFilter filter = new EpubHtmlFilter();
		
		String html;
		try (Reader reader = new InputStreamReader(sampleFile.getInputStream(), "UTF-8")) {
            html = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

		html = filter.filter(html);
		
		System.out.println(html);
	}
	
	
}

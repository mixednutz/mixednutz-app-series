package net.mixednutz.app.server.series;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.format.FormattingUtils;
import net.mixednutz.app.server.format.HtmlFilter;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date.Event;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

@Component
public class SeriesEpubView extends AbstractView {
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;

    @PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

	
	private InputStream getHtml(Chapter chapter) {
		Context ctx = new Context();
	    ctx.setVariable("chapter", chapter);
	    String processedHtml = templateEngine.process("series/chapter/plain", ctx);
	    return new ByteArrayInputStream(processedHtml.getBytes());
	}
	
	private Resource getResource(Chapter chapter, String href) throws IOException {
		return new Resource(getHtml(chapter), href);
	}
	

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Series series = (Series) model.get("series");
		FormattingUtils formatter = (FormattingUtils) model.get("formatter");

		response.setHeader("Content-Disposition", "attachment; filename=" + series.getTitleKey() + ".epub");

		// Create Book
		Book book = new Book();
		Metadata metadata = book.getMetadata();

		// Set Title
		metadata.addTitle(series.getTitle());
		metadata.addDescription(series.getDescription());
		metadata.addAuthor(new Author(series.getAuthor().getUsername()));
		metadata.addPublisher(accessor.getMessage("site.title"));
		metadata.addIdentifier(new Identifier(
				formatter.formatAbsoluteUrl(series.getUri()), Identifier.Scheme.URL));
		metadata.getRights().add(
				"Copyright "+series.getDatePublished().getYear()+" "+
				series.getAuthor().getUsername());
				
		// Set cover image
//	    book.setCoverImage(getResource("/book1/test_cover.png", "cover.png") );

		ZonedDateTime lastPublished = series.getDatePublished();
		for (Chapter chapter : series.getChapters()) {
			// Add Chapter
			
			if (chapter.getDatePublished()!=null) {
				
				if (chapter.getDatePublished().isAfter(lastPublished)) {
					lastPublished = chapter.getDatePublished();
				}
				
				//HTML Filter
				String filteredHtml = chapter.getBody();
				for (HtmlFilter htmlFilter: htmlFilters) {
					filteredHtml = htmlFilter.filter(filteredHtml);
				}
				chapter.setFilteredBody(filteredHtml);
				
				//Read in HTML
				book.addSection(chapter.getTitle(), 
						getResource(chapter, chapter.getTitleKey()+".html"));
			}
			
			
		}
		metadata.addDate(new nl.siegmann.epublib.domain.Date(
				Date.from(series.getDatePublished().toInstant()), Event.CREATION));
		metadata.addDate(new nl.siegmann.epublib.domain.Date(
				Date.from(lastPublished.toInstant()), Event.PUBLICATION));

		// Create EpubWriter
		EpubWriter epubWriter = new EpubWriter();

		// Write the Book as Epub
		epubWriter.write(book, response.getOutputStream());

	}

}

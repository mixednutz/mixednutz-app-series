package net.mixednutz.app.server.series;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import net.mixednutz.app.server.entity.ExternalVisibility;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.entity.post.series.Chapter;
import net.mixednutz.app.server.entity.post.series.Series;
import net.mixednutz.app.server.format.EpubHtmlFilter;
import net.mixednutz.app.server.format.FormattingUtils;
import net.mixednutz.app.server.format.HtmlFilter;
import net.mixednutz.app.server.io.domain.FileWrapper;
import net.mixednutz.app.server.io.manager.PhotoUploadManager;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.manager.post.VisibilityManager;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date.Event;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

@Component
public class SeriesEpubView extends AbstractView {
	
	private static final Logger LOG = LoggerFactory.getLogger(SeriesEpubView.class);
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;
	
	private List<HtmlFilter> epubFilters;
	
	@Autowired
	protected PhotoUploadManager photoUploadManager;
	
	@Autowired
	private VisibilityManager visibilityManager;

    @PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
        
        epubFilters = new ArrayList<>(htmlFilters);
		epubFilters.add(new EpubHtmlFilter());
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
	
	private Optional<FileSystemResource> getCoverResource(Series series) {
		FileWrapper file=null;
		if (series.getCoverFilename()!=null) {
			try {
				file = photoUploadManager.downloadFile(series.getAuthor(), 
						series.getCoverFilename(), Size.ORIGINAL);
			} catch (IOException e) {
				LOG.error("Error reading image", e);
				Optional.empty();
			}
		}
		if (file==null) {
			return Optional.empty();
		}		
		return Optional.of(new FileSystemResource(file.getFile()));
	}
	
	private String getFilename(Series series, List<Chapter> filteredChapters) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				
		//Get filename
		StringBuffer buffer = new StringBuffer();
		buffer.append(series.getTitleKey());
		
		
		//append external role name (if applicable)
		if (auth!=null && auth.getPrincipal() instanceof User) {
			User viewer = (User) auth.getPrincipal();
			
			//Only append the role name if this is a non-owner.
			if (!viewer.equals(series.getAuthor())) {
				List<String> roles = visibilityManager.getExternalListIds(viewer);
				if (!roles.isEmpty()) {
					String roleName = filteredChapters.stream()
						.map(Chapter::getVisibility)
						.filter(v->v.getVisibilityType().equals(VisibilityType.EXTERNAL_LIST))
						.flatMap(v->v.getExternalList().stream())
						.distinct()
						.filter(ev->roles.contains(ev.getProviderListId()))
						.map(ExternalVisibility::getName)
						.map(name->name.replace(' ', '-').toUpperCase())
						.collect(Collectors.joining("_"));
					buffer.append("_"+roleName);
				}		
			} 
		}
		
		//append latest chapter publish date
		filteredChapters.stream()
				.filter((c)-> c.getDatePublished()!=null)
				.max(Comparator.comparing(Chapter::getDatePublished))
				.map(Chapter::getDatePublished)
				.ifPresent(lastUpdateDate->buffer.append("_"+lastUpdateDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
		
		buffer.append(".epub");
		
		return buffer.toString();
	}
	

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Series series = (Series) model.get("series");
		@SuppressWarnings("unchecked")
		List<Chapter> filteredChapters = (List<Chapter>) model.get("filteredChapters");
		FormattingUtils formatter = (FormattingUtils) model.get("formatter");

		response.setHeader("Content-Disposition", "attachment; filename=" +getFilename(series, filteredChapters));

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
		Optional<FileSystemResource> coverImage = getCoverResource(series);
		coverImage.ifPresent(res->{
			Resource coverResource;
			try {
				coverResource = new Resource(res.getInputStream(), series.getCoverFilename());
				book.setCoverImage(coverResource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		ZonedDateTime lastPublished = series.getDatePublished();
		for (Chapter chapter : filteredChapters) {
			// Add Chapter
			
			if (chapter.getDatePublished()!=null) {
				
				if (chapter.getDatePublished().isAfter(lastPublished)) {
					lastPublished = chapter.getDatePublished();
				}
				
				//HTML Filter
				String filteredHtml = chapter.getBody();
				for (HtmlFilter htmlFilter: epubFilters) {
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

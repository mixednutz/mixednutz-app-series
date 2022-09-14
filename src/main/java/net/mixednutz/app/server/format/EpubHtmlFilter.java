package net.mixednutz.app.server.format;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EpubHtmlFilter implements HtmlFilter {

	@Override
	public String filter(String html) {
		Document doc = Jsoup.parse(html);
		
		//XML mode for self-closing tags
		doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		
		//remove unnecessary spans
		doc.select("span").stream()
			.filter(span->!span.hasAttr("style"))
			.forEach(span->span.unwrap());
		
		return doc.html();
	}

}

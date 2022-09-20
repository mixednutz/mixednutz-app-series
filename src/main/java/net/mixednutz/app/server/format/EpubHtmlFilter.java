package net.mixednutz.app.server.format;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EpubHtmlFilter implements HtmlFilter {

	@Override
	public String filter(String html) {
		Document doc = Jsoup.parseBodyFragment(html);
		
		//XML mode for self-closing tags
		doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		
		//remove unnecessary spans
		doc.select("span").stream()
			.filter(span->!span.hasAttr("style"))
			.forEach(span->span.unwrap());
		
		//Return inner html fragment
		return doc.select("body").html();
	}

}

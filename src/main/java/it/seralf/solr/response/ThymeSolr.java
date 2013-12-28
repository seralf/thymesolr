package it.seralf.solr.response;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocIterator;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/** Custom WriterResponse for Thymeleaf integration into Solr */
public class ThymeSolr implements QueryResponseWriter {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected static final String CONTENT_TYPE = "text/html";

	public ThymeSolr() {
		logger.debug("\n\n\n#####################\n\n\nTHYMELEAF CREATE INSTANCE...");
	}

	@Override
	public String getContentType(final SolrQueryRequest request, final SolrQueryResponse response) {
		logger.debug("THYMELEAF.getContentType");
		return CONTENT_TYPE;
	}

	@Override
	public void init(final NamedList namedList) {
		logger.debug("THYMELEAF PARAMETERS: {}", namedList.toString());
	}

	@Override
	public void write(final Writer writer, final SolrQueryRequest request, final SolrQueryResponse response) throws IOException {

		StringBuffer sb = new StringBuffer();
		String templateDir = new File(request.getCore().getDataDir() + "/../conf/thymeleaf").getCanonicalPath();
		String template = new File(templateDir, "home.html").getAbsolutePath();
		logger.debug("\n\n\n\n########### <p>DATA DIR: " + templateDir + "</p>\n\n\n\n");
		logger.debug("\n\n\n\n########### <p>VALUES: " + response.getValues() + "</p>\n\n\n\n");

		String now = new SimpleDateFormat("dd MMMM YYYY - HH:mm").format(new Date());
		ResultContext resultContext = (ResultContext) response.getValues().get("response");

		Context ctx = new Context();
		ctx.setVariable("today", now);

		ctx.setVariable("response", response);
		ctx.setVariable("resultContext", resultContext);
		ctx.setVariable("request", request);
		ctx.setVariable("core", request.getCore());
		ctx.setVariable("fields", response.getReturnFields().getLuceneFieldNames());
		ctx.setVariable("values", response.getValues());
		DocIterator dit = resultContext.docs.iterator();

		SolrDocumentList doclist = SolrPluginUtils.docListToSolrDocumentList(resultContext.docs, request.getSearcher(), response.getReturnFields().getLuceneFieldNames(), null);

		TemplateEngine templateEngine = new TemplateEngine();

		FileTemplateResolver resolver = new FileTemplateResolver();

		resolver.setCharacterEncoding("UTF-8");

		templateEngine.setTemplateResolver(resolver);
		templateEngine.initialize();
		templateEngine.process(template, ctx);

		String result = templateEngine.process(template, ctx);

		String text = new String(result.getBytes());
		logger.debug("text");
		sb.append(text);
		writer.write(sb.toString());

	}
}

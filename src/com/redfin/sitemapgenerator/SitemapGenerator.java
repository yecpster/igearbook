package com.redfin.sitemapgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.xml.sax.SAXException;

abstract class SitemapGenerator<U extends ISitemapUrl, THIS extends SitemapGenerator<U, THIS>> {
    /** 50000 URLs per sitemap maximum */
    public static final int MAX_URLS_PER_SITEMAP = 50000;

    private final String baseUrl;
    private final File baseDir;
    private final String fileNamePrefix;
    private final String fileNameSuffix;
    private final boolean allowMultipleSitemaps;
    private final ArrayList<U> urls = new ArrayList<U>();
    private final W3CDateFormat dateFormat;
    private final int maxUrls;
    private final boolean autoValidate;
    private final boolean gzip;
    private final ISitemapUrlRenderer<U> renderer;
    private int mapCount = 0;
    private boolean finished = false;

    private final ArrayList<File> outFiles = new ArrayList<File>();

    public SitemapGenerator(final AbstractSitemapGeneratorOptions<?> options, final ISitemapUrlRenderer<U> renderer) {
        baseDir = options.baseDir;
        baseUrl = options.baseUrl;
        fileNamePrefix = options.fileNamePrefix;
        W3CDateFormat dateFormat = options.dateFormat;
        if (dateFormat == null)
            dateFormat = new W3CDateFormat();
        this.dateFormat = dateFormat;
        allowMultipleSitemaps = options.allowMultipleSitemaps;
        maxUrls = options.maxUrls;
        autoValidate = options.autoValidate;
        gzip = options.gzip;
        this.renderer = renderer;
        fileNameSuffix = gzip ? ".xml.gz" : ".xml";
    }

    /**
     * Add one URL of the appropriate type to this sitemap. If we have reached the maximum number of URLs, we'll throw an exception if
     * {@link #allowMultipleSitemaps} is false, or else write out one sitemap immediately.
     * 
     * @param url
     *            the URL to add to this sitemap
     * @return this
     */
    public THIS addUrl(final U url) {
        if (finished)
            throw new RuntimeException("Sitemap already printed; you must create a new generator to make more sitemaps");
        UrlUtils.checkUrl(url.getUrl().toString(), baseUrl);
        if (urls.size() == maxUrls) {
            if (!allowMultipleSitemaps)
                throw new RuntimeException(
                        "More than "
                                + maxUrls
                                + " urls, but allowMultipleSitemaps is false.  Enable allowMultipleSitemaps to split the sitemap into multiple files with a sitemap index.");
            if (mapCount == 0)
                mapCount++;
            writeSiteMap();
            mapCount++;
            urls.clear();
        }
        urls.add(url);
        return getThis();
    }

    /**
     * Add multiple URLs of the appropriate type to this sitemap, one at a time. If we have reached the maximum number of URLs, we'll throw an
     * exception if {@link #allowMultipleSitemaps} is false, or write out one sitemap immediately.
     * 
     * @param urls
     *            the URLs to add to this sitemap
     * @return this
     */
    public THIS addUrls(final Iterable<? extends U> urls) {
        for (final U url : urls)
            addUrl(url);
        return getThis();
    }

    /**
     * Add multiple URLs of the appropriate type to this sitemap, one at a time. If we have reached the maximum number of URLs, we'll throw an
     * exception if {@link #allowMultipleSitemaps} is false, or write out one sitemap immediately.
     * 
     * @param urls
     *            the URLs to add to this sitemap
     * @return this
     */
    public THIS addUrls(@SuppressWarnings("unchecked") final U... urls) {
        for (final U url : urls)
            addUrl(url);
        return getThis();
    }

    /**
     * Add multiple URLs of the appropriate type to this sitemap, one at a time. If we have reached the maximum number of URLs, we'll throw an
     * exception if {@link #allowMultipleSitemaps} is false, or write out one sitemap immediately.
     * 
     * @param urls
     *            the URLs to add to this sitemap
     * @return this
     * @throws MalformedURLException
     */
    public THIS addUrls(final String... urls) throws MalformedURLException {
        for (final String url : urls)
            addUrl(url);
        return getThis();
    }

    /**
     * Add one URL of the appropriate type to this sitemap. If we have reached the maximum number of URLs, we'll throw an exception if
     * {@link #allowMultipleSitemaps} is false, or else write out one sitemap immediately.
     * 
     * @param url
     *            the URL to add to this sitemap
     * @return this
     * @throws MalformedURLException
     */
    public THIS addUrl(final String url) throws MalformedURLException {
        U sitemapUrl;
        try {
            sitemapUrl = renderer.getUrlClass().getConstructor(String.class).newInstance(url);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return addUrl(sitemapUrl);
    }

    /**
     * Add multiple URLs of the appropriate type to this sitemap, one at a time. If we have reached the maximum number of URLs, we'll throw an
     * exception if {@link #allowMultipleSitemaps} is false, or write out one sitemap immediately.
     * 
     * @param urls
     *            the URLs to add to this sitemap
     * @return this
     */
    public THIS addUrls(final URL... urls) {
        for (final URL url : urls)
            addUrl(url);
        return getThis();
    }

    /**
     * Add one URL of the appropriate type to this sitemap. If we have reached the maximum number of URLs, we'll throw an exception if
     * {@link #allowMultipleSitemaps} is false, or write out one sitemap immediately.
     * 
     * @param url
     *            the URL to add to this sitemap
     * @return this
     */
    public THIS addUrl(final URL url) {
        U sitemapUrl;
        try {
            sitemapUrl = renderer.getUrlClass().getConstructor(URL.class).newInstance(url);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return addUrl(sitemapUrl);
    }

    @SuppressWarnings("unchecked")
    THIS getThis() {
        return (THIS) this;
    }

    /**
     * Write out remaining URLs; this method can only be called once. This is necessary so we can keep an accurate count for
     * {@link #writeSitemapsWithIndex()}.
     * 
     * @return a list of files we wrote out to disk
     */
    public List<File> write() {
        if (finished)
            throw new RuntimeException("Sitemap already printed; you must create a new generator to make more sitemaps");
        if (urls.size() == 0 && mapCount == 0)
            throw new RuntimeException("No URLs added, sitemap would be empty; you must add some URLs with addUrls");
        writeSiteMap();
        finished = true;
        return outFiles;
    }

    /**
     * After you've called {@link #write()}, call this to generate a sitemap index of all sitemaps you generated.
     * 
     */
    public void writeSitemapsWithIndex() {
        if (!finished)
            throw new RuntimeException("Sitemaps not generated yet; call write() first");
        final File outFile = new File(baseDir, "sitemap_index.xml");
        SitemapIndexGenerator sig;
        try {
            sig = new SitemapIndexGenerator.Options(baseUrl, outFile).dateFormat(dateFormat).autoValidate(autoValidate).build();
        } catch (final MalformedURLException e) {
            throw new RuntimeException("bug", e);
        }
        sig.addUrls(fileNamePrefix, fileNameSuffix, mapCount).write();
    }

    private void writeSiteMap() {
        if (urls.size() == 0)
            return;
        String fileNamePrefix;
        if (mapCount > 0) {
            fileNamePrefix = this.fileNamePrefix + mapCount;
        } else {
            fileNamePrefix = this.fileNamePrefix;
        }
        final File outFile = new File(baseDir, fileNamePrefix + fileNameSuffix);
        outFiles.add(outFile);
        try {
            OutputStreamWriter out;
            if (gzip) {
                @SuppressWarnings("resource")
                final FileOutputStream fileStream = new FileOutputStream(outFile);
                final GZIPOutputStream gzipStream = new GZIPOutputStream(fileStream);
                out = new OutputStreamWriter(gzipStream);
            } else {
                out = new FileWriter(outFile);
            }

            writeSiteMap(out);
            if (autoValidate)
                SitemapValidator.validateWebSitemap(outFile);
        } catch (final IOException e) {
            throw new RuntimeException("Problem writing sitemap file " + outFile, e);
        } catch (final SAXException e) {
            throw new RuntimeException("Sitemap file failed to validate (bug?)", e);
        }
    }

    private void writeSiteMap(final OutputStreamWriter out) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ");

        if (renderer.getXmlNamespaces() != null) {
            out.write(renderer.getXmlNamespaces());
            out.write(' ');
        }
        out.write(">\n");
        for (final U url : urls) {
            renderer.render(url, out, dateFormat);
        }
        out.write("</urlset>");
        out.close();
    }

}

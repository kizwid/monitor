package kizwid.web.context;

import kizwid.web.util.Check;
import org.apache.commons.lang.StringUtils;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Extends Jetty's {@link org.mortbay.jetty.webapp.WebAppContext} to allow for the given web
 * app descriptor to be discovered from the classpath in preference
 * to the base behaviour which requires a valid file system address.
 *
 * User: kizwid
 * Date: 2012-02-22
 */
public class EnrichedWebAppContext extends WebAppContext{
    private final static Logger logger = LoggerFactory.getLogger(EnrichedWebAppContext.class);
    private static final String JAR_DISCRIMINATOR = ".jar!";
    private final static String DEFAULT_WEB_APP_LOCATION_SUFFIX = "WEB-INF/web.xml";

    /**
     * The location of the web app descriptor within the root of the web app, typically you'll leave this untouched and use the
     * {@link #DEFAULT_WEB_APP_LOCATION_SUFFIX} instead as webapps are, by convention, described by WEB-INF/web.xml
     */
    private String webAppLocationSuffix;

    /**
     * Extension point for {@link #setWar(String)}, we resolve the given descriptor name for a file system address (via
     * classpath lookup) and set the discovered address into the parent. We are trying to locate the webapp - which is expected to be a
     * directory containing {@link #getWebAppLocationSuffix()}, typically, WEB-INF/web.xml - from the classpath, once we have found it we
     * get its filesystem address and set that address into the parent class such that the parent continues to do its thing with an actual
     * filesystem address without the users of this class having to provide a filesystem address. This resource must reference a location
     * which contains a web application either in packaged, WAR, format or exploded, WEB-INF/web.xml, format. The address will be either a
     * file resource - such as target/test-classes/foo - or a jar resource - such as foo.jar!/foo/. Examples:
     *
     * <pre>
     * file:/path/to/target/folder/target/classes/
     * jar:file:/path/to/folder/with/server/jar/foo-xxxx-SNAPSHOT.jar!/foo/
     * </pre>
     */
    @Override
    public void setWar(String webAppRoot) {
        String webXmlName = appendTrailingSlash(webAppRoot) + getWebAppLocationSuffix();
        URL webXmlUrl = Thread.currentThread().getContextClassLoader().getResource(webXmlName);
        Check.that(
                webXmlUrl != null,
                "Could not create a web application context because the web descriptor resource: [%s] is not on the classpath",
                webXmlName);

        String derivedWebAppRoot = getWebAppRootFromWebXmlPath(webXmlUrl.getPath());
        String fullWebAppRootPath = "";
        if (descriptorIsFoundInJar(webXmlUrl)) {
            fullWebAppRootPath = String.format("jar:%s", derivedWebAppRoot);
        } else {
            fullWebAppRootPath = String.format("file:%s", derivedWebAppRoot);
        }
        // add a trailing slash, jetty is quite particular about all directory addresses having a trailing slash
        fullWebAppRootPath = appendTrailingSlash(fullWebAppRootPath);
        logger.info(
                "Locating webapp descriptor resource from address: {} using root: {} and web app location: {}", new String[] {
                fullWebAppRootPath,
                webAppRoot,
                getWebAppLocationSuffix() });
        super.setWar(fullWebAppRootPath);
    }

    public void setWebAppLocationSuffix(String webAppLocationSuffix) {
        this.webAppLocationSuffix = webAppLocationSuffix;
    }

    // we always address the resource via its parent folder, Jetty expects a folder address to have a trailing slash
    private String appendTrailingSlash(String address) {
        if (!address.endsWith("/")) {
            address = address + "/";
        }
        return address;
    }

    // tells us whether the given descriptor is found in a jar or not
    private boolean descriptorIsFoundInJar(URL fileUrl) {
        return fileUrl.getPath().contains(JAR_DISCRIMINATOR);
    }

    private String getWebAppLocationSuffix() {
        return StringUtils.isBlank(webAppLocationSuffix) ? DEFAULT_WEB_APP_LOCATION_SUFFIX : webAppLocationSuffix;
    }

    private String getWebAppRootFromWebXmlPath(String webXmlPath) {
        return webXmlPath.substring(0, webXmlPath.length()
                - getWebAppLocationSuffix().length());
    }
}

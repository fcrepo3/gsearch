package dk.defxws.fedoragsearch.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;

import fedora.common.http.WebClient;

public class URIResolverImpl implements URIResolver {
	
	private Config config;
    
    private final Logger logger = Logger.getLogger(URIResolverImpl.class);
    
    public void setConfig(Config config) {
		this.config = config;
    }

	public Source resolve(String href, String base) throws TransformerException {
		Source source = null;
		URL url;
		try {
//			url = new URL(new URL(base), href);
			url = new URL(href);
		} catch (MalformedURLException e) {
			throw new TransformerException("resolve new URL href="+href+" base="+base, e);
		}
		String reposName = config.getRepositoryNameFromUrl(url);
        if (logger.isDebugEnabled())
            logger.debug("resolve get href="+href+" base="+base+" url="+url.toString()+" reposName="+reposName);
//		System.setProperty("http.user", config.getFedoraUser(reposName));
//		System.setProperty("http.password", config.getFedoraPass(reposName));
		System.setProperty("javax.net.ssl.trustStore", config.getTrustStorePath(reposName));
		System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustStorePass(reposName));
		WebClient client = new WebClient();
		try {
	        if (logger.isDebugEnabled())
	            logger.debug("resolve get source=\n"+client.getResponseAsString(href, false, new UsernamePasswordCredentials(config.getFedoraUser(reposName), config.getFedoraPass(reposName))));
			source = new StreamSource(client.get(href, false, config.getFedoraUser(reposName), config.getFedoraPass(reposName)));
		} catch (IOException e) {
			throw new TransformerException("resolve get href="+href+" base="+base, e);
		}
		return source;
	}

}

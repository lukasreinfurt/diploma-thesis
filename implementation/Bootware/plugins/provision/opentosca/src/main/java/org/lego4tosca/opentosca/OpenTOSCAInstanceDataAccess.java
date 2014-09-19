package org.lego4tosca.opentosca;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OpenTOSCAInstanceDataAccess {

	//static Logger logger = Logger.getLogger(OpenTOSCAInstanceDataAccess.class.getName());
	private Logger logger = new Logger();

	// the endpoint (address) of the instance data REST API
	private String APIRootCSARs = null;
	private String APIRootVinothek = null;

	private String APIRootNodeInstances = null;
	private static final String QUERY_PARAM_NODEINSTACEID = "nodeInstanceID";
	private static final String QUERY_PARAM_SERVICEINSTACEID = "serviceInstanceID";
	private static final String QUERY_PARAM_NODETEMPLATEID = "nodeTemplateName";
	private static final String NS_XLINK = "http://www.w3.org/1999/xlink";
	private static final String NS_CONTAINERAPI_INSTANCEDATA = "http://opentosca.org/api/pp";
	private final QName ELEMENT_NAME_LINK = new QName(NS_CONTAINERAPI_INSTANCEDATA, "link");
	private final QName ATTRIBUTE_NAME_HREF = new QName(NS_XLINK, "href");
	private static final String PATH_SEG_PROPERTIES = "properties";

	/**
	 *
	 * @param host The host OpenTOSCA is running on, i.e. an IP address or host name. When null or empty, a default value of "localhost" will be used.
	 */
	public OpenTOSCAInstanceDataAccess(String host){
		if (host == null || host.isEmpty()) {
			host = "localhost";
		}
		APIRootCSARs = "http://"+host+":1337/containerapi/CSARs";
		APIRootVinothek = "http://"+host+":8080/vinothek";
		logger.debug("APIRootCSARs    = "+APIRootCSARs);
		logger.debug("APIRootVinothek = "+APIRootVinothek);

		APIRootNodeInstances = "http://"+host+":1337/containerapi/instancedata/nodeInstances";
	}

	/**
	 * To run the build plan of the specified CSAR
	 * @param csarName the name of the CSAR file whose build plan has to run
	 * @return
	 */
	public String provisionService(String csarName){

		String url = getVinothekRequestURLForBuildPlan(csarName);

		if(StringUtils.isEmpty(url)){
			logger.error("Cannot run the build plan of "+ csarName+", error with CSAR name");
			return null;
		}

		String callbackURI = doGet(url);
		logger.info("callback link: "+ callbackURI);
		String response = "";

		if(!StringUtils.isEmpty(callbackURI)){
			logger.info("Start Polling...");
			while((response=doGet(callbackURI)).equals("NO-CALLBACK-RECEIVED-YET")){
				logger.info("NO-CALLBACK-RECEIVED-YET");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		logger.info("Build plan Response of CSAR file '"+csarName+"' is: "+ response);
	    return response;
	}

	/**
	 * To run the termination plan of the specified CSAR
	 * @param csarName the name of the CSAR file whose termination plan has to run
	 * @param serviceInstanceID the id of the service that will be deprovisioned
	 * @return
	 */
	public String deprovisionService(String csarName, String serviceInstanceID){

		String url = getVinothekRequestURLForTerminationPlan(csarName, serviceInstanceID);

		if(StringUtils.isEmpty(url)){
			logger.error("Cannot run the termination plan of "+ csarName+", error with CSAR name");
			return null;
		}

		String callbackURI = doGet(url);
		logger.info("callback link: "+ callbackURI);
		String response = "";

		if(!StringUtils.isEmpty(callbackURI)){
			logger.info("Start Polling...");
			while((response=doGet(callbackURI)).equals("NO-CALLBACK-RECEIVED-YET")){
				logger.info("NO-CALLBACK-RECEIVED-YET");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		logger.info("Termination plan Response of CSAR file '"+csarName+"' is: "+ response);
	    return response;
	}

	private String getVinothekRequestURLForBuildPlan(String csarName){
		if(StringUtils.isEmpty(csarName)){
			logger.error("CSAR name should not be empty");
			return null;
		}
		String csarURL = APIRootCSARs.trim()
				+ (APIRootCSARs.endsWith("/") == true ? "" : "/")
				+ csarName
				+ (csarName.endsWith(".csar") == true ? "" : ".csar");
		String check = doGet(csarURL);
		if(StringUtils.isEmpty(check)){
			logger.error("Specified CSAR does not exist");
			return null;
		}
		String url = APIRootVinothek.trim()
				+ (APIRootVinothek.endsWith("/") == true ? "" : "/")
				+ "ApplicationInstantiation?applicationId=" + csarURL
				+ (csarURL.endsWith("/") == true ? "" : "/")
				+ "Content/SELFSERVICE-Metadata/&optionId=1";
		return url;
	}

	private String getVinothekRequestURLForTerminationPlan(String csarName, String serviceInstanceID){
		String url = getVinothekRequestURLForBuildPlan(csarName);
		url += "&terminate=true&serviceInstanceID="+serviceInstanceID;
		return url;
	}

	private String doGet(String url){
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));
		// We want XML! In some cases, the OpenTOSCA API may otherwise return e.g. binary data (e.g. when GETting a certain CSAR).
		method.setRequestHeader("Accept", "text/xml");
		String responseBody = "";
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Method failed: " + method.getStatusLine());
				return null;
			}
			// Read the response body.
			responseBody = new String(method.getResponseBody());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return responseBody;
	}

	private String doPost(String url){
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));
		String responseBody = "";
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Method failed: " + method.getStatusLine());
				return null;
			}
			// Read the response body.
			responseBody = new String(method.getResponseBody());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return responseBody;
	}

	/**
	 * Uploads and deploys a CSAR file to OpenTSOCA.
	 *
	 * @param urlToUpload
	 *            The URL of the CSAR file. If the file is located on the local
	 *            drive, the URL has to look like e.g.
	 *            <code>file:///C:/Users/Anon/Documents/Test.csar</code> or
	 *            <code>file:///home/Anon/Test.csar</code>
	 * @return null (always, really...)
	 */
	// TODO change response to something meaningful...
	public String uploadCSARDueURL(String urlToUpload) {

		logger.info("Try to upload and deploy CSAR from " + encodeURL(urlToUpload));

		String requestURL = APIRootCSARs.trim()
				+ (APIRootCSARs.endsWith("/") == true ? "" : "/")
				+ "?url=" + urlToUpload;
		logger.debug("Request URL is "+requestURL);

		String response = doPost(requestURL);
		logger.debug("Response body is "+response);

		return response;
	}

	private String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
	}

	public Document getProperties(URI nodeInstanceID) {
		Document doc;
		URI nodeInstanceURI = getNodeInstanceURI(nodeInstanceID);

		HttpClient httpClient = new HttpClient();
		// not set = default = 0 = no timeout
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(10000);
		GetMethod httpget = new GetMethod(buildURI(nodeInstanceURI.toString(),
				PATH_SEG_PROPERTIES));
		try {
			/**
			 * Notice: Assumption is that a node instance id is an accessible
			 * URL!
			 *
			 * TODO remove assumption, i.e. get list of node instances, search
			 * for link to instance with given ID, follow this link
			 */
			httpget.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			int status = httpClient.executeMethod(httpget);
			logger.trace("GET " + httpget.getURI() + " --> " + status);
			InputStream instream = httpget.getResponseBodyAsStream();
			doc = newDocumentFromInputStream(instream);
			return doc;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpget.releaseConnection();
		}
		return null;
	}

	public Document getProperties(URI serviceInstanceID, String nodeTemplateName) {

		URI nodeInstanceURI = null;
		Document doc;
		HttpClient httpClient = new HttpClient();
		// not set = default = 0 = no timeout
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(10000);
		GetMethod httpget = new GetMethod(APIRootNodeInstances);
		NameValuePair[] query = {new NameValuePair(QUERY_PARAM_SERVICEINSTACEID, serviceInstanceID.toString()),
								 new NameValuePair(QUERY_PARAM_NODETEMPLATEID, nodeTemplateName)};
		httpget.setQueryString(query);
		try {
			httpget.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));
			int status = httpClient.executeMethod(httpget);
			logger.trace("GET " + httpget.getURI() + " --> " + status);
//			System.out.println("GET " + httpget.getURI() + " --> " + status);
			InputStream instream = httpget.getResponseBodyAsStream();
			doc = newDocumentFromInputStream(instream);
			// retrieve 'href' attribute from first link element
			NodeList nl = doc.getElementsByTagNameNS(ELEMENT_NAME_LINK.getNamespaceURI(), ELEMENT_NAME_LINK.getLocalPart());
			if (nl != null && nl.getLength() > 0) {
				Element firstLink = (Element) nl.item(0);
				String attrValue = firstLink.getAttributeNS(ATTRIBUTE_NAME_HREF.getNamespaceURI(), ATTRIBUTE_NAME_HREF.getLocalPart());
				nodeInstanceURI = new URI(attrValue);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpget.releaseConnection();
		}
		 if (nodeInstanceURI != null) {
				//HttpClient httpClient = new HttpClient();
				// not set = default = 0 = no timeout
				httpClient.getHttpConnectionManager().getParams()
						.setConnectionTimeout(10000);
				httpget = new GetMethod(buildURI(nodeInstanceURI.toString(),
						PATH_SEG_PROPERTIES));
				try {
					httpget.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
							new DefaultHttpMethodRetryHandler(3, false));

					int status = httpClient.executeMethod(httpget);
					logger.trace("GET " + httpget.getURI() + " --> " + status);
//					System.out.println("GET " + httpget.getURI() + " --> " + status);
					InputStream instream = httpget.getResponseBodyAsStream();
					doc = newDocumentFromInputStream(instream);
					return doc;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					httpget.releaseConnection();
				}
				return null;
		 }
		 return null;
	}

	private URI getNodeInstanceURI(URI nodeInstanceID) {
		Document doc;
		HttpClient httpClient = new HttpClient();
		// not set = default = 0 = no timeout
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(10000);
		GetMethod httpget = new GetMethod(APIRootNodeInstances);
		NameValuePair[] query = {new NameValuePair(QUERY_PARAM_NODEINSTACEID, nodeInstanceID.toString())};
		httpget.setQueryString(query);
		try {
			/**
			 * Notice: Assumption is that a node instance id is an accessible
			 * URL!
			 *
			 * TODO remove assumption, i.e. get list of node instances, search
			 * for link to instance with given ID, follow this link
			 */
			httpget.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));
			int status = httpClient.executeMethod(httpget);
			logger.trace("GET " + httpget.getURI() + " --> " + status);
//			System.out.println("GET " + httpget.getURI() + " --> " + status);
			InputStream instream = httpget.getResponseBodyAsStream();
			doc = newDocumentFromInputStream(instream);
			// retrieve 'href' attribute from first link element
			NodeList nl = doc.getElementsByTagNameNS(ELEMENT_NAME_LINK.getNamespaceURI(), ELEMENT_NAME_LINK.getLocalPart());
			if (nl != null && nl.getLength() > 0) {
				Element firstLink = (Element) nl.item(0);
				String attrValue = firstLink.getAttributeNS(ATTRIBUTE_NAME_HREF.getNamespaceURI(), ATTRIBUTE_NAME_HREF.getLocalPart());
				return new URI(attrValue);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpget.releaseConnection();
		}
		return null;
	}

	private static String buildURI(String root, String... pathSegments) {
		String result = root;
		for (String seg : pathSegments) {
			result = addPathSegment(result, seg);
		}
		return result;
	}

	private static String addPathSegment(String root, String segment) {
		return removeOuterSlashes(root) + "/" + removeOuterSlashes(segment);
	}

	private static String removeOuterSlashes(String s) {
		if (s != null) {
			// remove slash at beginning if present
			if (s.startsWith("/")) {
				s = s.substring(1);
			}
			// remove slash at end if present
			if (s.endsWith("/")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	private static Document newDocumentFromInputStream(InputStream in) {
		// prepare everything
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document ret = null;
		try {
			// TODO add parameters to control namespace processing, whitespace
			// processing, ...
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		// parse
		try {
			ret = builder.parse(in);
			ret.getDocumentElement().normalize();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private class Logger {

		public void info(String string) {
			System.out.println(string);
		}

		public void error(String string) {
			System.out.println(string);
		}

		public void debug(String string) {
			System.out.println(string);
		}

		public void trace(String string) {
			System.out.println(string);
		}
	}

}

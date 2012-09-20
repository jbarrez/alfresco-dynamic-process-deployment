import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;


public class Cleanup {
	
	public static void main(String[] args) {
		System.out.println("Creating CMIS session");
		removeWorkflows();
		
		// Remove form config
		System.out.println("Removing form config");
		removeFormConfigs();
		
		System.out.println("done");
	}


	private static void removeWorkflows() {
		SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");
		parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/service/api/cmis");
		 parameter.put(SessionParameter.REPOSITORY_ID, findRepositoryId());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		Session session = sessionFactory.createSession(parameter);
		
		// Removing workflow
		System.out.println("Removing workflow");
		Folder workflowDefinitionFolder = (Folder) session.getObjectByPath("/Data Dictionary/Workflow Definitions");
		
		ItemIterable<CmisObject> children = workflowDefinitionFolder.getChildren();
		Iterator<CmisObject> iterator = children.iterator();
		while (iterator.hasNext()) {
			CmisObject cmisObject = iterator.next();
			if (cmisObject instanceof Document) {
				String name = cmisObject.getName();
				((Document)cmisObject).deleteAllVersions();
				System.out.println("Removed " + name);
			}
		}
		
		// Removing task model
		System.out.println("Removing task models");
		Folder modelFolder = (Folder) session.getObjectByPath("/Data Dictionary/Models");
		
		children = modelFolder.getChildren();
		iterator = children.iterator();
		while (iterator.hasNext()) {
			CmisObject cmisObject = iterator.next();
			if (cmisObject instanceof Document) {
				String name = cmisObject.getName();
				((Document)cmisObject).deleteAllVersions();
				System.out.println("Removed " + name);
			}
		}
	}
	
	private static void removeFormConfigs() {

		// First, find all the deployed kickstart forms
		GetMethod getDeploymentsMethod = new GetMethod("http://localhost:8081/share/page/modules/deploy");
		String response = executeHttpRequest(getDeploymentsMethod);
		
		HashSet<String> formIds = new HashSet<String>();
		Pattern pattern = Pattern.compile("\"id\":\"kickstart.*?\"");
		Matcher matcher = pattern.matcher(response);
		while (matcher.find()) {
			formIds.add(matcher.group().replace("\"", "").replace("id:", ""));
		}
		
		// Remove all the forms
		for (String formId : formIds) {
			System.out.println("Removing kickstart form '" + formId + "'");
			GetMethod deleteFormConfigMethod = new GetMethod("http://localhost:8081/share/page/modules/module/delete?moduleId=" + URLEncoder.encode(formId));
			executeHttpRequest(deleteFormConfigMethod); 
		}
	}
	
	private static String executeHttpRequest(HttpMethod method) {
		try {
			HttpState httpState = new HttpState();
			httpState.setCredentials(new AuthScope(null, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
			
			HttpClient httpClient = new HttpClient();
			int result = httpClient.executeMethod(null, method, httpState);

			// Display status code
			System.out.println("Response status code: " + result);

			// Display response
//			System.out.println("Response body: ");
//			System.out.println(method.getResponseBodyAsString());
			return method.getResponseBodyAsString();
		} catch (Throwable t) {
			System.err.println("Error: " + t.getMessage());
			t.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return null;
	}

	private static String findRepositoryId() {
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");
		parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/service/api/cmis");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		
		SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
		List<Repository> repositories = sessionFactory.getRepositories(parameter);
		return repositories.get(0).getId();
	}

}

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;


public class Cleanup {
	
	public static void main(String[] args) {
		
		System.out.println("Creating CMIS session");
		
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
		
		// Remove form config
		System.out.println("Removing form config");
		HttpState state = new HttpState();
		state.setCredentials(new AuthScope(null, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
		
		GetMethod getMethod = new GetMethod("http://localhost:8081/share/page/modules/module/delete?moduleId=" + URLEncoder.encode("test_form"));
		
		 try
	        {
	         HttpClient httpClient = new HttpClient();
	         int result = httpClient.executeMethod(null, getMethod, state);
	         
	           // Display status code
	            System.out.println("Response status code: " + result);
	            
	            // Display response
	            System.out.println("Response body: ");
	            System.out.println(getMethod.getResponseBodyAsString());
	        }
	        catch(Throwable t)
	        {
	          System.err.println("Error: " + t.getMessage());
	          t.printStackTrace();
	        }
	        finally
	        {
	            getMethod.releaseConnection();
	        }
		 
		 System.out.println("done");

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

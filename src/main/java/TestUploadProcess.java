import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;



public class TestUploadProcess {
	
	public static void main(String[] args) throws Exception {
		System.out.println("deploying process");
		deployProcess();
		System.out.println("Process deployed");
		
		System.out.println("deploying task model");
		deployTaskModel();
		System.out.println("Task model deployed");
		
		System.out.println("deploying form");
		deployForm();
		System.out.println("Form deployed");
		
		System.out.println("Done");
	}

	private static void deployProcess() throws FileNotFoundException {
		SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");
		parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/service/api/cmis");
		 parameter.put(SessionParameter.REPOSITORY_ID, findRepositoryId());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		
		Session session = sessionFactory.createSession(parameter);
		
		
		Folder workflowDefinitionFolder = (Folder) session.getObjectByPath("/Data Dictionary/Workflow Definitions");
		
		ItemIterable<CmisObject> children = workflowDefinitionFolder.getChildren();
		Iterator<CmisObject> iterator = children.iterator();
		while (iterator.hasNext()) {
			CmisObject cmisObject = iterator.next();
			for (Property<?> property : cmisObject.getProperties()) {
				System.out.println(property.getId() + " : " + property.getValueAsString());
			}
		}
		
		String processFileName = "test_process.bpmn20.xml";
		
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("cmis:name", processFileName);
		properties.put("cmis:objectTypeId", "D:bpm:workflowDefinition");
		properties.put("bpm:definitionDeployed", true);
		properties.put("bpm:engineId", "activiti");
		
	    ContentStream contentStream = new ContentStreamImpl(processFileName, null, "application/xml", TestUploadProcess.class.getResourceAsStream(processFileName));
		
		
		Document document = workflowDefinitionFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
		System.out.println(document.getName());
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
	
	
	private static void deployTaskModel() throws FileNotFoundException {
		SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");
		parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/service/api/cmis");
		 parameter.put(SessionParameter.REPOSITORY_ID, findRepositoryId());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		
		Session session = sessionFactory.createSession(parameter);
		Folder modelFolder = (Folder) session.getObjectByPath("/Data Dictionary/Models");
		
		ItemIterable<CmisObject> children = modelFolder.getChildren();
		Iterator<CmisObject> iterator = children.iterator();
		while (iterator.hasNext()) {
			CmisObject cmisObject = iterator.next();
			for (Property<?> property : cmisObject.getProperties()) {
				System.out.println(property.getId() + " : " + property.getValueAsString());
			}
		}
		
		String taskModelFile = "test_process_task_model.xml";
		
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("cmis:name", taskModelFile);
		properties.put("cmis:objectTypeId", "D:cm:dictionaryModel");
		properties.put("cm:modelActive", true);
		
		ContentStream contentStream = new ContentStreamImpl(taskModelFile, null, "application/xml", TestUploadProcess.class.getResourceAsStream(taskModelFile));
		
		
		Document document = modelFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
		System.out.println(document.getName());
	}
	
	private static void deployForm() throws Exception {
		HttpState state = new HttpState();
		state.setCredentials(new AuthScope(null, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
		
		String formConfigFileName = "test_process_form_config.xml";
		
		PostMethod postMethod = new PostMethod("http://localhost:8081/share/page/modules/module");
		
//		HttpState state = new HttpState();
//		state.addCookies(getAuthenticationCookies("admin", "admin"));
		
//		String ticket = getAuthenticationTicket("admin", "admin");
//		HttpState state = applyTicketToMethod(postMethod, ticket);
		
		 try
	        {
	            
			 String formConfig = FileUtils.readFileToString(new File(TestUploadProcess.class.getResource(formConfigFileName).toURI()));
			 postMethod.setRequestEntity(new StringRequestEntity(formConfig, "application/xml", "UTF-8"));
			 
//			 postMethod.setRequestHeader("Content-type", "text/xml");
	         HttpClient httpClient = new HttpClient();
	         int result = httpClient.executeMethod(null, postMethod, state);
	         
	           // Display status code
	          System.out.println("Response status code: " + result);
	            
	          if (result != 200)
	          {
	            System.out.println("Response body: ");
	            System.out.println(postMethod.getResponseBodyAsString());	        	  
	          }
	            
	        }
	        catch(Throwable t)
	        {
	          System.err.println("Error: " + t.getMessage());
	          t.printStackTrace();
	        }
	        finally
	        {
	            postMethod.releaseConnection();
	        }
	}
	
//	private static String getAuthenticationTicket(String userName, String password) throws Exception { 
//		PostMethod loginMethod = null;
//		try
//		{
////			loginMethod = new PostMethod("http://localhost:8080/alfresco/service/api/login");
//			loginMethod = new PostMethod("http://localhost:8081/share/page/dologin");
//			loginMethod.setRequestHeader("Accept", "application/json");
//
//			// Populate resuest body
//			JSONObject requestBody = new JSONObject();
//			requestBody.put("username", userName);
//			requestBody.put("password", password);
//			
//			try {
//				loginMethod.setRequestEntity(new StringRequestEntity(requestBody.toJSONString(), "application/json", "UTF-8"));
//			} catch (UnsupportedEncodingException error) {
//				throw new RuntimeException("All hell broke loose, a JVM that doesn't have UTF-8 encoding...");
//			}
//		 
//			HttpClient client = new HttpClient();
//		 
//			// Since no authentication info is available yet, no need to use a
//			// custom HostConfiguration for the login-call
//			client.executeMethod(loginMethod);
//
//			if(loginMethod.getStatusCode() == HttpStatus.SC_OK)
//			{
//				// Extract the ticket
//				JSONObject data = JSONUtil.getDataFromResponse(loginMethod);
//				if(data == null)
//				{
//					throw new RuntimeException("Failed to login to Alfresco with user " + userName + " (No JSON-data found in response)");
//				}
//
//				// Extract the actual ticket
//				String ticket = JSONUtil.getString(data, "ticket", null);
//				if(ticket == null)
//				{
//					throw new RuntimeException("Failed to login to Alfresco with user " + userName + "(No ticket found in JSON-response)");
//				}
//				return ticket;
//			}
//			else
//			{
//				// Unable to login
//				throw new RuntimeException("Failed to login to Alfresco with user " + userName + " (" + loginMethod.getStatusCode() + loginMethod.getStatusLine().getReasonPhrase() + ")");
//			}
//		} catch (IOException ioe) {
//			// Something went wrong when sending request
//			throw new RuntimeException("Failed to login to Alfresco with user " + userName, ioe);
//		}
//		finally
//		{
//			if(loginMethod != null)
//			{
//				try
//				{
//					loginMethod.releaseConnection();
//				}
//				catch(Throwable t)
//				{
//					// Ignore this to prevent swallowing potential original exception
//				}
//			}
//		}
//	}
	
	private static Cookie[] getAuthenticationCookies(String userName, String password) throws Exception { 
		PostMethod loginMethod = null;
		try
		{
			loginMethod = new PostMethod("http://localhost:8081/share/page/dologin");
			
			try {
				
				loginMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
				loginMethod.setRequestEntity(new StringRequestEntity("username=admin&password=admin", "text/plain", "UTF-8"));
			} catch (UnsupportedEncodingException error) {
				throw new RuntimeException("All hell broke loose, a JVM that doesn't have UTF-8 encoding...");
			}
		 
			HttpState state = new HttpState();
			HttpClient client = new HttpClient();
		 
			// Since no authentication info is available yet, no need to use a
			// custom HostConfiguration for the login-call
			client.executeMethod(null, loginMethod, state);

			Cookie[] cookies = state.getCookies();
			System.out.println("Found " + cookies.length + " cookies");
			
			System.out.println("Response body: " + loginMethod.getResponseBodyAsString());
			
			return cookies;
			
		} catch (IOException ioe) {
			// Something went wrong when sending request
			throw new RuntimeException("Failed to login to Alfresco with user " + userName, ioe);
		}
		finally
		{
			if(loginMethod != null)
			{
				try
				{
					loginMethod.releaseConnection();
				}
				catch(Throwable t)
				{
					// Ignore this to prevent swallowing potential original exception
				}
			}
		}
	}

	private static HttpState applyTicketToMethod(HttpMethod method, String ticket) throws URIException
	{
		// POST and PUT methods don't support Query-params, use Basic Authentication to pass
		// in the ticket (ROLE_TICKET) for all methods.
		HttpState state = new HttpState();
		state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("ROLE_TICKET", ticket));
		return state;
	}


}

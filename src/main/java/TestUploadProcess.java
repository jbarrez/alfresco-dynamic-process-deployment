import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;



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
		
		
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("cmis:name", "test_upload.bpmn20.xml");
		properties.put("cmis:objectTypeId", "D:bpm:workflowDefinition");
		properties.put("bpm:definitionDeployed", true);
		properties.put("bpm:engineId", "activiti");
		
		FileInputStream fis = new FileInputStream("kickstart-tryout.bpmn20.xml");
		
		 ContentStream contentStream = new ContentStreamImpl("test_upload.bpmn20.xml", null,
		            "application/xml",fis);
		
		
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
		
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("cmis:name", "task_model.xml");
		properties.put("cmis:objectTypeId", "D:cm:dictionaryModel");
		properties.put("cm:modelActive", true);
		
		FileInputStream fis = new FileInputStream("task-model.xml");
		
		 ContentStream contentStream = new ContentStreamImpl("task-model.xml", null,
		            "application/xml",fis);
		
		
		Document document = modelFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
		System.out.println(document.getName());
	}
	
	private static void deployForm() {
		HttpState state = new HttpState();
		state.setCredentials(new AuthScope(null, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
		
		PostMethod postMethod = new PostMethod("http://localhost:8081/share/page/modules/module");
		
		 try
	        {
	            
			 String formConfig = FileUtils.readFileToString(new File("test-form-config.xml"));
			 postMethod.setRequestEntity(new StringRequestEntity(formConfig, "application/xml", "UTF-8"));
			 
			 
//			 postMethod.setRequestHeader("Content-type", "text/xml");
	         HttpClient httpClient = new HttpClient();
	         int result = httpClient.executeMethod(null, postMethod, state);
	         
	           // Display status code
	            System.out.println("Response status code: " + result);
	            
	            // Display response
	            System.out.println("Response body: ");
	            System.out.println(postMethod.getResponseBodyAsString());
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

}

package AODocsDataToBigQueryConnector.AODocsDataToBigQueryConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DataManager {
	static Logger logger = Logger.getLogger(DataManager.class);
	public static void main(String[] args) throws IOException, InterruptedException {
        
		//gets the configuration properties
		Properties _properties = new Properties();
		InputStream in = DataManager.class.getClassLoader().getResourceAsStream("config.properties");
		_properties.load(in);
		in.close();
		
				
		logger.info(_properties.getProperty("Libray")+" started - "+new Date());
		String workingDirectory = System.getProperty("user.dir")+_properties.getProperty("WorkingDirectory");
		workingDirectory= "C:/AskLegalJSONResponses";
        Path path = Paths.get(workingDirectory);     
        if(!Files.exists(path)) {
        	if(new File(workingDirectory).mkdir())
        		//System.out.println("Folder for JSON response created");
        	 logger.info("Folder for JSON response created at "+workingDirectory);  
        	else
        		//System.out.println("Couldn't create the folder");
        		logger.error("Couldn't create the folder at "+workingDirectory);
        }
        else
        	//System.out.println("Folder already existing");
        	logger.info("Folder already existing at "+workingDirectory); 
 
        String objectName =  "";
        String fileName = "";

        AODocsConnector aodocs = new AODocsConnector();
        
        String projectId= _properties.getProperty("ProjectId");
        String bucketName= _properties.getProperty("BucketName");
        String bqTableName = _properties.getProperty("BqAuditLogTableName");
        String bqDatasetName = _properties.getProperty("BqDatasetName");
        boolean deleteTable = false;
        boolean AuditLog= Boolean.parseBoolean(_properties.getProperty("AuditLog"));
        if(AuditLog) {
        	logger.info("About to start fetching Auditlog");
        objectName =  new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.json'").format(new Date());
        objectName = "AL_DOC_STATE_CHANGED_"+objectName;
        fileName = workingDirectory+"/"+objectName;
        aodocs.GETAuditLog(fileName);
        logger.info("Auditlog fetched");
        logger.info("Starts uploading Auditlog into BigQuery");
        new CloudStorage().uploadObject(projectId,bucketName,objectName,fileName,bqTableName,bqDatasetName,deleteTable);
       
        Thread.sleep(100);
        }

        logger.info("About to start Reviews");
        objectName =  new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.json'").format(new Date());
        objectName = "AL_REVIEWS_"+objectName;
        fileName = workingDirectory+"/"+objectName;
        aodocs.GetReviews(fileName);
        deleteTable = true;
        bqTableName = _properties.getProperty("BqReviewsTableName");
        new CloudStorage().uploadObject(projectId,bucketName,objectName,fileName,bqTableName,bqDatasetName,deleteTable);

    }
}

package AODocsDataToBigQueryConnector.AODocsDataToBigQueryConnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.google.cloud.bigquery.*;

import com.google.cloud.storage.*;

public class CloudStorage {
	static Logger logger = Logger.getLogger(CloudStorage.class);
	public void uploadObject(
			 String projectId, String bucketName, String objectName, String filePath, String bqTableName,String bqDatasetName,Boolean deleteTable) throws IOException, InterruptedException {
        //System.out.println("Project Id=>"+projectId);
        //System.out.println("Bucket name=>"+bucketName);
        //System.out.println("Object name=>"+objectName);
		try
		{
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        System.out.println("Storage info=>"+storage);
        System.out.println("Blob Info=>"+blobInfo);
        System.out.println("File path Info=>"+ Paths.get(filePath));
        Blob blob = storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
        logger.info("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);  
        //System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
        Thread.sleep(100);
        String uri = "gs://"+bucketName+"/"+objectName;
        LoadJSONFromGCS(uri,bqTableName,bqDatasetName,deleteTable);
		}
		catch(Exception e) {
			logger.error("Error while uploading into bucket"+e.toString());
		}
    }
    private void LoadJSONFromGCS(String sourceUri,String tableName,String datasetName,Boolean deleteTable){
        //System.out.println("Started inserting in to BigQuery Table");
    	logger.info("Started inserting in to BigQuery Table");
        //String datasetName = "AskLegalData";// "VerifierData";;
        //String tableName = "asklegal_S4MU8KHxZyuMPz6JPl_auditlog_document_state_changed";
        // "asklegal_S4MU8KHxZyuMPz6JPl_reviews_info";

        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.

            boolean deleteStatus=false;
            if(deleteTable)
            {
                deleteStatus = deleteTable(datasetName,tableName);
                if(!deleteStatus)
                {
                    //System.out.println("Table not deleted and hence couldn't update table data");
                	logger.info("Table not deleted and hence couldn't update table data");
                    return;
                }
            }
            BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

            TableId tableId = TableId.of(datasetName, tableName);
            LoadJobConfiguration loadConfig =
                    LoadJobConfiguration.builder(tableId, sourceUri)
                            .setFormatOptions(FormatOptions.json())
                            .setAutodetect(true)
                            .build();

            // Load data from a GCS JSON file into the table
            Job job = bigquery.create(JobInfo.of(loadConfig));
            // Blocks until this load table job completes its execution, either failing or succeeding.
            job = job.waitFor();
            if (job.isDone()) {
            	logger.info("Json Autodetect from GCS successfully loaded in a table");
                //System.out.println("Json Autodetect from GCS successfully loaded in a table");
            } else {
            	logger.error("BigQuery was unable to load into the table due to an error:"+ job.getStatus().getError());
                System.out.println(
                        "BigQuery was unable to load into the table due to an error:"
                                + job.getStatus().getError());
            }

        } catch (InterruptedException e) {//| InterruptedException
            //System.out.println("Column not added during load append \n" + e.toString());
        	logger.error("Column not added during load append \n" + e.toString());
        }
    }
    private boolean deleteTable(String datasetName, String tableName) {
        boolean result=false;
        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
            result = bigquery.delete(TableId.of(datasetName, tableName));
            if (result) {
                //System.out.println("Table deleted successfully");
            	logger.info("Table deleted successfully");
            } else {
            	logger.info("Table was not found");
                //System.out.println("Table was not found");
            }
        } catch (BigQueryException e) {
        	logger.error("Table was not deleted. \n" + e.toString());
            //System.out.println("Table was not deleted. \n" + e.toString());
        }
        return result;
    }
}

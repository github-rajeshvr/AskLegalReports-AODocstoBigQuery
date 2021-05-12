package AODocsDataToBigQueryConnector.AODocsDataToBigQueryConnector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

public class AODocsConnector {
	 int counter = 0;
	    public void GETAuditLog(String fileName) throws IOException {

	        String LibraryId = "S4MU8KHxZyuMPz6JPl";
	        String SecurityCode = "S5INt505uH4ZSnmEy3";
	        int PageSize= 1000;
	        String PageToken = "";
	        String NextToken = "";
	        long Max_Date_Milli_Secs=0;
	        long Min_Date_Milli_Secs=0;

	        Calendar date = new GregorianCalendar();
	        // reset hour, minutes, seconds and millis
	        date.set(Calendar.HOUR_OF_DAY, 22);
	        date.set(Calendar.MINUTE, 0);
	        date.set(Calendar.SECOND, 0);
	        date.set(Calendar.MILLISECOND, 0);
	        System.out.println("Today is =>"+date.getTime());
	        Max_Date_Milli_Secs = date.getTimeInMillis();
	        System.out.println("Max Date in Milli Secs=>"+Max_Date_Milli_Secs);
	        // prev day
	        date.add(Calendar.DAY_OF_MONTH, -1);
	        System.out.println("Previous Day=>"+date.getTime());
	        //Min_Date_Milli_Secs =  date.getTimeInMillis();
	        Max_Date_Milli_Secs  =  date.getTimeInMillis();
	        System.out.println("Min Date in Milli Secs=>"+Min_Date_Milli_Secs);
	        do
	        {
	        PageToken=NextToken;
	        URL urlForGetRequest = new URL("https://ao-docs.appspot.com/_ah/api/domain/v1/auditLog?libraryId="+LibraryId+"&securityCode="+SecurityCode+"&pageSize="+PageSize+"&eventType=DOCUMENT_STATE_CHANGED&pageToken="+PageToken+"&maxDate="+Max_Date_Milli_Secs);//+"&minDate="+Min_Date_Milli_Secs
	        //URL urlForGetRequest = new URL("https://ao-docs.appspot.com/_ah/api/domain/v1/auditLog?libraryId="+LibraryId+"&securityCode="+SecurityCode+"&pageSize="+PageSize+"&pageToken="+PageToken+"&maxDate="+Max_Date_Milli_Secs+"&minDate="+Min_Date_Milli_Secs);
	        System.out.println("URL to AODocs API=>"+urlForGetRequest.toString());

	        String readLine = null;

	        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();

	        connection.setRequestMethod("GET");

	        //connection.setRequestProperty("userId", "a1bcdef"); // set userId its a sample here

	        int responseCode = connection.getResponseCode();

	        System.out.println("Response Code => "+responseCode);

	        System.out.println("Response Message => "+connection.getResponseMessage());

	        if (responseCode == HttpURLConnection.HTTP_OK) {

	            BufferedReader in = new BufferedReader(

	                    new InputStreamReader(connection.getInputStream()));

	            StringBuffer response = new StringBuffer();

	            while ((readLine = in.readLine()) != null) {

	                response.append(readLine);

	            } in .close();

	            // print result

	            //System.out.println("JSON String Result " + response.toString());

	            JSONObject jsonObj = new JSONObject(response.toString());

	            NextToken = jsonObj.get("pageToken").toString();

	           if(!jsonObj.isNull("auditLogList"))
	            {
	                ParseAuditlogJSON( fileName,jsonObj);
	            }
	           else {
	               System.out.println("All docs retrieved");
	               break;
	           }

	        } else {

	            System.out.println("GET NOT WORKED");

	        }
	        }while(!PageToken.equals(NextToken));
	        System.out.println("Moving to Cloud Storage Operation");
	    }
	    private void ParseAuditlogJSON(String _filePath, JSONObject _jsonObject) throws IOException {
	        System.out.println("Next Page Token =>"+ _jsonObject.get("pageToken"));

	        JSONArray auditLogsArray = _jsonObject.getJSONArray("auditLogList");
	        counter = counter+auditLogsArray.length();
	        System.out.println("Total Count=>"+counter);

	        Date today = new Date();
	        BufferedWriter out = new BufferedWriter(
	                new FileWriter(_filePath, true));

	        JSONObject response = new JSONObject();
	        JSONObject auditLog = new JSONObject();
	        for (int i=0; i < auditLogsArray.length(); i++) {

	            auditLog = auditLogsArray.getJSONObject(i);

	            response.put("objectId",auditLog.get("objectId"));
	            response.put("timestamp",auditLog.get("timestamp"));
	            response.put("eventType",auditLog.get("eventType"));
	            response.put("user",auditLog.get("user"));

	            response.put("details",auditLog.getJSONObject("details"));

	            response.put("stateChangeDate",auditLog.getJSONObject("diff").getJSONObject("stateChangeDate"));

	            out.write(response.toString());
	            out.write("\r\n");
	        }

	        out.close();
	    }

	    public void GetReviews(String _filePath) throws IOException {
	        String LibraryId = "S4MU8KHxZyuMPz6JPl";
	        String SecurityCode = "S5INt505uH4ZSnmEy3";
	        String ClassId = "S4MUO5L4HRA4pyvlPF";

	        int RequestCounter = 0;
	        int OffSetIndex = 0;
	        int MaxDataLimit = 100;
	        boolean HasNextSetData = true;
	        String DocumentId = "";
	        do {

	            URL requestURL = new URL("https://ao-docs.appspot.com/_ah/api/search/v1/libraries/" + LibraryId + "/search?classId=" + ClassId + "&securityCode=" + SecurityCode + "&limit=" + MaxDataLimit + "&offset=" + OffSetIndex);
	            //String readLine = null;
	            System.out.println("URL to get Reviews-=>"+requestURL);

	            byte[] postDataBytes = (LibraryId + "/search?classId=" + ClassId + "&securityCode=" + SecurityCode + "&limit=" + MaxDataLimit + "&offset=" + OffSetIndex).getBytes(StandardCharsets.UTF_8);//requestURL.toString().getBytes("UTF-8");
	            //System.out.println("Bytes array length=>"+postDataBytes.length);
	            HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
	            System.out.println("Connection Opened");
	            connection.setReadTimeout(150000);
	            connection.setConnectTimeout(150000);
	            connection.setRequestMethod("POST");
	            connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");// application/json;charset=utf-8

	            //connection.setDoInput(true);
	            connection.setDoOutput(true);

	            //connection.setRequestProperty("Accept", "application/json");

	            OutputStream out = connection.getOutputStream();
	            //System.out.println(postDataBytes);
	            out.write(postDataBytes);
	            out.flush();
	            System.out.println("Connecting");
	            connection.connect();
	            System.out.println("Connected");
	            System.out.println("Getting response code");
	            int responseCode=connection.getResponseCode();
	            //System.out.println("Checking for Resp code");
	            if (responseCode == HttpURLConnection.HTTP_OK) {

	                System.out.println("Response code is >"+responseCode);

	               //BufferedReader br = new BufferedReader(

	                        //new InputStreamReader(connection.getInputStream()));
	                Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
	                StringBuilder sb = new StringBuilder();
	                for (int c; (c = in.read()) >= 0;)
	                    sb.append((char)c);
	                //String response = br.readLine();
	                String response = sb.toString();
	                //br.close();
	                in.close();

	                //System.out.println("JSON String Result " + response.toString());

	                JSONObject jsonObj = new JSONObject(response.toString());
	                if (!jsonObj.isNull("documents"))
	                {
	                    //System.out.println("JSON Object =>"+jsonObj);
	                    //System.out.println("Response JSON having the documents array");
	                    JSONArray documents = jsonObj.getJSONArray("documents");
	                    if (documents.length() > 0) {
	                        System.out.println("Documents array length > 0");
	                        //parse the document results array
	                        ParseReviewsJSON(_filePath,jsonObj);

	                    } else {
	                        HasNextSetData = false;
	                        System.out.println("All docs retrieved");
	                        break;
	                    }
	                    }
	                    else
	                    {
	                        System.out.println("Response JSON not having the documents array");
	                        HasNextSetData = false;
	                    }
	            }
	            else {
	                System.out.println("GET NOT WORKED");
	                //System.out.println("RESPONSE CODE=> "+responseCode);
	                //System.out.println("RESPONSE Message=> "+connection.getResponseMessage());
	            }
	            RequestCounter++;
	            OffSetIndex = RequestCounter*MaxDataLimit;
	        }while (HasNextSetData);
	    }

	    private void ParseReviewsJSON(String _filePath,JSONObject _jsonData) throws IOException {

	        System.out.println("Started parsing the JSON");
	        //iterates over the documents
	        JSONArray documents = _jsonData.getJSONArray("documents");
	        JSONArray fields;
	        JSONArray categories;
	        JSONObject response = new JSONObject();
	        JSONObject document = new JSONObject();

	        //String fileName ="/Users/rxr406/testfiles/reviewsinfo.json";
	        BufferedWriter out = new BufferedWriter(
	                new FileWriter(_filePath, true));
	try
	{
	        for (int i=0; i < documents.length(); i++) {

	            document = documents.getJSONObject(i);
	            //System.out.println("First JSON Object=>"+document);

	            response.put("DocumentId",document.get("id"));
	            response.put("CreationDate",document.get("creationDate"));
	            response.put("AssignedDate",document.get("creationDate"));
	            response.put("deleted",document.get("deleted"));
	            response.put("State",document.get("state"));


	            fields = document.getJSONArray("fields");
	            categories = document.getJSONArray("categories");
	            for (int f=0;f<fields.length();f++)
	            {
	                JSONObject field = fields.getJSONObject(f);
	                if(field.get("fieldName").toString().equals("Request Owner"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("RequestOwner",field.getJSONArray("values").get(0));
	                    else
	                        response.put("RequestOwner","");
	                }
	                if(field.get("fieldName").toString().equals("AskLegal Request Number"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("AskLegalRequestNumber",field.getJSONArray("values").get(0));
	                    else
	                        response.put("AskLegalRequestNumber","");
	                }
	                if(field.get("fieldName").toString().equals("Estimated Annual Revenue ($)"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("EstimatedAnnualRevenue",field.getJSONArray("values").get(0));
	                    else
	                        response.put("EstimatedAnnualRevenue","");
	                }
	                if(field.get("fieldName").toString().equals("What do you need from Legal? / Provide Opportunity Details"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("WhatDoYouNeedFromLegalProvideOpportunityDetails",field.getJSONArray("values").get(0));
	                    else
	                        response.put("WhatDoYouNeedFromLegalProvideOpportunityDetails","");
	                }
	                if(field.get("fieldName").toString().equals("Business Entity Name"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("BusinessEntityName",field.getJSONArray("values").get(0));
	                    else
	                        response.put("BusinessEntityName","");
	                }
	                if(field.get("fieldName").toString().equals("Assignee"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("Assignee",field.getJSONArray("values").get(0));
	                    else
	                        response.put("Assignee","");
	                }
	                if(field.get("fieldName").toString().equals("Assistant"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("Assistant",field.getJSONArray("values").get(0));
	                    else
	                        response.put("Assistant","");
	                }
	                if(field.get("fieldName").toString().equals("What do you need from Legal?"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("WhatDoYouNeedFromLegal",field.getJSONArray("values").get(0));
	                    else
	                        response.put("WhatDoYouNeedFromLegal","");
	                }
	                if(field.get("fieldName").toString().equals("Third Party and/or Internal Stakeholders"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("ThirdPartyInternalStakeholders",field.getJSONArray("values").get(0));
	                    else
	                        response.put("ThirdPartyInternalStakeholders","");
	                }
	                if(field.get("fieldName").toString().equals("Describe the nature, type, and intended use of the content"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("DescribeNatureTypeUseOfContent",field.getJSONArray("values").get(0));
	                    else
	                        response.put("DescribeNatureTypeUseOfContent","");
	                }
	                if(field.get("fieldName").toString().equals("Provide Opportunity Details"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("ProvideOpportunityDetails",field.getJSONArray("values").get(0));
	                    else
	                        response.put("ProvideOpportunityDetails","");
	                }
	                if(field.get("fieldName").toString().equals("Project Name"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("ProjectName",field.getJSONArray("values").get(0));
	                    else
	                        response.put("ProjectName","");
	                }
	                if(field.get("fieldName").toString().equals("Project Name"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("ProjectName",field.getJSONArray("values").get(0));
	                    else
	                        response.put("ProjectName","");
	                }
	                if(field.get("fieldName").toString().equals("Matter Name"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("MatterName",field.getJSONArray("values").get(0));
	                    else
	                        response.put("MatterName","");
	                }
	                if(field.get("fieldName").toString().equals("Legal Review Complete Date"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("LegalReviewCompleteDate",field.getJSONArray("values").get(0));
	                    else
	                        response.put("LegalReviewCompleteDate","");
	                }
	                if(field.get("fieldName").toString().equals("Acknowledgement"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("Acknowledgement",field.getJSONArray("values").get(0));
	                    else
	                        response.put("Acknowledgement","");
	                }
	                if(field.get("fieldName").toString().equals("NEQ Number"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("NEQNumber",field.getJSONArray("values").get(0));
	                    else
	                        response.put("NEQNumber","");
	                }
	                if(field.get("fieldName").toString().equals("Estimated Annual Spend ($)"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("EstimatedAnnualSpend",field.getJSONArray("values").get(0));
	                    else
	                        response.put("EstimatedAnnualSpend","");
	                }
	                if(field.get("fieldName").toString().equals("Policy Type"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("PolicyType",field.getJSONArray("values").get(0));
	                    else
	                        response.put("PolicyType","");
	                }
	                if(field.get("fieldName").toString().equals("Policy & Standard Status"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("PolicyStandardStatus",field.getJSONArray("values").get(0));
	                    else
	                        response.put("PolicyStandardStatus","");
	                }
	                if(field.get("fieldName").toString().equals("Closed Date"))
	                {
	                    if(!field.isNull("values"))
	                        response.put("ClosedDate",field.getJSONArray("values").get(0));
	                    else
	                        response.put("ClosedDate","");
	                }

	            }
	            for(int c=0;c<categories.length();c++)
	            {
	                JSONObject category =categories.getJSONObject(c);
	                if(category.get("fieldName").toString().equals("Business Channel"))
	                {
	                   if(!category.isNull("values"))
	                   {
	                       JSONArray values = category.getJSONArray("values");
	                       JSONArray value = values.getJSONArray(0);
	                       JSONObject bcValue = value.getJSONObject(0);
	                       response.put("BusinessChannel",bcValue.get("name"));
	                       if(value.length()>1) {
	                           JSONObject stValue = value.getJSONObject(1);
	                           response.put("SalesTeam",stValue.get("name"));
	                       }
	                   }
	                   else {
	                       response.put("BusinessChannel", "");
	                       response.put("SalesTeam","");
	                   }
	                }
	                if(category.get("fieldName").toString().equals("Relevant Market/Service for this Opportunity"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("RelevantMarketServiceOpportunity",catValue.get("name"));
	                        if(value.length()>1) {
	                            JSONObject lvl1Value = value.getJSONObject(1);
	                            response.put("RelevantMarketServiceOpportunityLvl1",lvl1Value.get("name"));
	                        }
	                    }
	                    else{
	                        response.put("RelevantMarketServiceOpportunity","");
	                        response.put("RelevantMarketServiceOpportunityLvl1","");
	                    }

	                }
	                if(category.get("fieldName").toString().equals("Request Type"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("RequestType",catValue.get("name"));
	                    }
	                    else
	                        response.put("RequestType","");
	                }
	                if(category.get("fieldName").toString().equals("Account Type"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("AccountType",catValue.get("name"));
	                    }
	                    else
	                        response.put("AccountType","");
	                }
	                if(category.get("fieldName").toString().equals("Relevant BU/COE"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("RelevantBUCOE",catValue.get("name"));
	                    }
	                    else
	                        response.put("RelevantBUCOE","");
	                }
	                if(category.get("fieldName").toString().equals("Required for RFP"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("RequiredForRFP",catValue.get("name"));
	                    }
	                    else
	                        response.put("RequiredForRFP","");
	                }
	                if(category.get("fieldName").toString().equals("Current Agreement with Business Entity?"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("CurrentAgreementBusinessEntity",catValue.get("name"));
	                    }
	                    else
	                        response.put("CurrentAgreementBusinessEntity","");
	                }
	                if(category.get("fieldName").toString().equals("Type of Business Relationship"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("TypeOfBusinessRelationship",catValue.get("name"));
	                    }
	                    else
	                        response.put("TypeOfBusinessRelationship","");
	                }
	                //needs to capture
	                if(category.get("fieldName").toString().equals("Subject Area"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("SubjectAreaBusinessChannel",catValue.get("name"));
	                        if(value.length()>1){
	                            JSONObject saValue = value.getJSONObject(1);
	                            response.put("SubjectArea",saValue.get("name"));
	                        }
	                    }
	                    else{
	                        response.put("SubjectArea","");
	                        response.put("SubjectAreaBusinessChannel","");
	                    }

	                }
	                if(category.get("fieldName").toString().equals("Content Type"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("ContentType",catValue.get("name"));
	                    }
	                    else
	                        response.put("ContentType","");
	                }
	                if(category.get("fieldName").toString().equals("Distribution"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("Distribution",catValue.get("name"));
	                    }
	                    else
	                        response.put("Distribution","");
	                }
	                if(category.get("fieldName").toString().equals("Related to existing content?"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("RelatedToExistingContent",catValue.get("name"));
	                    }
	                    else
	                        response.put("RelatedToExistingContent","");
	                }
	                if(category.get("fieldName").toString().equals("Was the existing content approved by Legal?"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("ExistingContentApprovedByLegal",catValue.get("name"));
	                    }
	                    else
	                        response.put("ExistingContentApprovedByLegal","");
	                }
	                if(category.get("fieldName").toString().equals("Do we have permission to use 3rd party content?"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("PermissionTo3rdPartyContent",catValue.get("name"));
	                    }
	                    else
	                        response.put("PermissionTo3rdPartyContent","");
	                }
	                if(category.get("fieldName").toString().equals("VEN Product/Service"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("VENProductService",catValue.get("name"));
	                    }
	                    else
	                        response.put("VENProductService","");
	                }
	                if(category.get("fieldName").toString().equals("Will Vendor be On-Site?"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("VendorBeOnSite",catValue.get("name"));
	                    }
	                    else
	                        response.put("VendorBeOnSite","");
	                }
	                if(category.get("fieldName").toString().equals("Will Vendor have access to our information or systems?"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("VendorHaveAccessInformationSystems",catValue.get("name"));
	                    }
	                    else
	                        response.put("VendorHaveAccessInformationSystems","");
	                }
	                if(category.get("fieldName").toString().equals("Rush"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("Rush",catValue.get("name"));
	                    }
	                    else
	                        response.put("Rush","");
	                }
	                if(category.get("fieldName").toString().equals("Paper Type"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("PaperType",catValue.get("name"));
	                    }
	                    else
	                        response.put("PaperType","");
	                }
	                if(category.get("fieldName").toString().equals("Relevant Market/Service"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("RelevantMarketService",catValue.get("name"));
	                    }
	                    else
	                        response.put("RelevantMarketService","");
	                }
	                if(category.get("fieldName").toString().equals("Type of Account"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("TypeOfAccount",catValue.get("name"));
	                    }
	                    else
	                        response.put("TypeOfAccount","");
	                }
	                if(category.get("fieldName").toString().equals("Folder"))
	                {
	                    if(!category.isNull("values"))
	                    {
	                        JSONArray values = category.getJSONArray("values");
	                        JSONArray value = values.getJSONArray(0);
	                        JSONObject catValue = value.getJSONObject(0);
	                        response.put("Folder",catValue.get("name"));
	                    }
	                    else
	                        response.put("Folder","");
	                }
	            }
	            out.write(response.toString());
	            out.write("\r\n");
	        }

	    }catch (Exception e){
	    System.out.println("Error occurred while parsing the JSON. Error details=>"+e.getMessage());
	    }
	finally {
	    out.close();
	}
	    }
}

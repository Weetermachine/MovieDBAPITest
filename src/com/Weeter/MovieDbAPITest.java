package Weeter;

import junit.framework.Assert;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MovieDbAPITest {

    private static final String APITOKEN = "";
    private static final String BASEURL = "https://api.themoviedb.org/3/";
    private static final String TOKENPARAM = "?api_key=" + APITOKEN;

    // I originally tried this higher, however it appears that there is a limit to the number of open connections allowed per API token in a short period of time.
    private static final int LOAD_LIMIT = 30;


    private volatile boolean hasFailed = false;
    private volatile List<String> message = new ArrayList<>();

    /**
     * This is the basic API test with a valid API token, which is the only required field.
     * Due to this being date based, I can only really shallow test this at this point,
     * as providing no dates defaults it to the last 24 hours.
     */
    @Test
    public void basicAPIRequiredFields()
    {
        try {

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/1245/changes" + TOKENPARAM);

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return a successful response as expected. Status returned: " + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Map resultJson = new Gson().fromJson(result.toString(), Map.class);

            Assert.assertTrue("Response API did not contain the expected default 'changes' key.", resultJson.containsKey("changes"));

            List<Object> changes = (List<Object>) resultJson.get("changes");


            // Because this is a fluid response, I am wrapping this in a conditional because I am not sure when you will look at this, and cannot
            // guarantee any one result for this test. Normally, I would use another API to make changes to this person and look for the changes
            // but, seeing as this is not test data that I would be messing with, and that I do not appear to have appropriate permissions to add
            // and remove changes to a person, I will simply leave it to a very shallow test
            if(changes.size() != 0)
            {
                Map<String, String> keys = (Map<String, String>)changes.get(0);

                Assert.assertTrue("API result is missing the 'key' object in the response.", keys.containsKey("key"));
            }



        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in basicAPIRequiredFields. Err: " + ex.getMessage());
        }

    }

    @Test
    public void invalidPersonIDTest()
    {
        try {

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/not_valid/changes" + TOKENPARAM);

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return a success response as expected. Status returned: " + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Map resultJson = new Gson().fromJson(result.toString(), Map.class);

            Assert.assertTrue(resultJson.containsKey("changes"));

            // Verify it is an empty response
            Assert.assertEquals("An empty 'changes' tag was expected, however the result was: " + result.toString(), "{\"changes\":[]}", result.toString());



        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in invalidPersonIDTest. Err: " + ex.getMessage());
        }

    }

    @Test
    public void invalidAPIToken()
    {
        try {

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/3/changes" + "?api_key=ffffffffffffffffffffffffffffffff");

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return the expected error response. Status returned: " + response.getStatusLine().getStatusCode(), 401, response.getStatusLine().getStatusCode());

        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in invalidAPIToken. Err: " + ex.getMessage());
        }

    }

    @Test
    public void invalidShortAPIToken()
    {
        try {

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/3/changes" + "?api_key=e109253d2212");

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return the expected error response. Status returned: " + response.getStatusLine().getStatusCode(), 401, response.getStatusLine().getStatusCode());

        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in invalidShortAPIToken. Err: " + ex.getMessage());
        }

    }

    @Test
    public void invalidLongAPIToken()
    {
        try {

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/3/changes" + "?api_key=e109253d2212e109253d2212e109253d2212");

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return the expected error response. Status returned: " + response.getStatusLine().getStatusCode(), 401, response.getStatusLine().getStatusCode());

        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in invalidLongAPIToken. Err: " + ex.getMessage());
        }

    }

    @Test
    public void missingRequiredField()
    {
        try {

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/3/changes");

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return the expected error response. Status returned: " + response.getStatusLine().getStatusCode(), 401, response.getStatusLine().getStatusCode());

        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in missingRequiredField. Err: " + ex.getMessage());
        }

    }

    @Test
    public void basicAPILoadTest()
    {

        List<Thread> threads = new ArrayList<>();
        try {

            for(int i = 0; i < LOAD_LIMIT; i++) {

                final int thread_number = i+1;
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            int threadNumber = thread_number;
                            HttpClient client = HttpClientBuilder.create().build();

                            // Generate URL
                            HttpGet request = new HttpGet(BASEURL + "person/1245/changes" + TOKENPARAM);

                            HttpResponse response = client.execute(request);

                            if(response.getStatusLine().getStatusCode() != 200)
                            {
                                hasFailed = true;
                                message.add("The Movie DB API did not return a successful response as expected. Thread number: " + threadNumber + "\n");

                            }
                            else {


                                BufferedReader rd = new BufferedReader(
                                        new InputStreamReader(response.getEntity().getContent()));

                                StringBuffer result = new StringBuffer();
                                String line = "";
                                while ((line = rd.readLine()) != null) {
                                    result.append(line);
                                }

                                Map resultJson = new Gson().fromJson(result.toString(), Map.class);

                                if (!resultJson.containsKey("changes")) {
                                    hasFailed = true;
                                    message.add("Response API did not contain the expected default 'changes' key. Thread number: " + threadNumber + "\n");

                                }
                                else {

                                    List<Object> changes = (List<Object>) resultJson.get("changes");


                                    // Because this is a fluid response, I am wrapping this in a conditional because I am not sure when you will look at this, and cannot
                                    // guarantee any one result for this test. Normally, I would use another API to make changes to this person and look for the changes
                                    // but, seeing as this is not test data that I would be messing with, and that I do not appear to have appropriate permissions to add
                                    // and remove changes to a person, I will simply leave it to a very shallow test
                                    if (changes.size() != 0) {
                                        Map<String, String> keys = (Map<String, String>) changes.get(0);

                                        if(keys.containsKey("keys"))
                                        {
                                            hasFailed = true;
                                            message.add("API result is missing the 'key' object in the response. Thread number: " + threadNumber + "\n");

                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            hasFailed = true;
                            message.add("An exception was thrown in basicAPILoadTest. Err: " + ex.getMessage() + "\n");

                        }
                    }
                });

                threads.add(thread);
                thread.start();
            }


            for(Thread thread : threads)
            {
                thread.join();
            }

            Assert.assertFalse(message.toString(), hasFailed);


        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in basicAPILoadTest. Err: " + ex.getMessage());
        }

    }


    @Test
    public void emptyDatesTag()
    {
        try {

            String startDateToken = "&start_date=";
            String endDateToken = "&end_date=";

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/1245/changes" + TOKENPARAM + startDateToken + endDateToken);

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return a success response as expected. Status returned: " + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Map resultJson = new Gson().fromJson(result.toString(), Map.class);

            Assert.assertTrue(resultJson.containsKey("changes"));

        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in emptyDatesTag. Err: " + ex.getMessage());
        }

    }


    @Test
    public void differentLanguageTest()
    {
        try {

            String language = "&language=es-MX";

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/1245/changes" + TOKENPARAM + language);

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return a success response as expected. Status returned: " + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Map resultJson = new Gson().fromJson(result.toString(), Map.class);

            Assert.assertTrue(resultJson.containsKey("changes"));

            List<Object> changes = (List<Object>) resultJson.get("changes");


            // Because this is a fluid response, I am wrapping this in a conditional because I am not sure when you will look at this, and cannot
            // guarantee any one result for this test. Normally, I would use another API to make changes to this person and look for the changes
            // but, seeing as this is not test data that I would be messing with, and that I do not appear to have appropriate permissions to add
            // and remove changes to a person, I will simply leave it to a very shallow test
            if(changes.size() != 0)
            {
                Map<String, String> keys = (Map<String, String>)changes.get(0);

                Assert.assertTrue("API result is missing the 'key' object in the response.", keys.containsKey("key"));
            }



        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in differentLanguageTest. Err: " + ex.getMessage());
        }

    }


    @Test
    public void emptyLanguageTest()
    {
        try {

            String language = "&language=";

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/1245/changes" + TOKENPARAM + language);

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return a success response as expected. Status returned: " + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Map resultJson = new Gson().fromJson(result.toString(), Map.class);

            Assert.assertTrue(resultJson.containsKey("changes"));

            List<Object> changes = (List<Object>) resultJson.get("changes");


            // Because this is a fluid response, I am wrapping this in a conditional because I am not sure when you will look at this, and cannot
            // guarantee any one result for this test. Normally, I would use another API to make changes to this person and look for the changes
            // but, seeing as this is not test data that I would be messing with, and that I do not appear to have appropriate permissions to add
            // and remove changes to a person, I will simply leave it to a very shallow test
            if(changes.size() != 0)
            {
                Map<String, String> keys = (Map<String, String>)changes.get(0);

                Assert.assertTrue("API result is missing the 'key' object in the response.", keys.containsKey("key"));
            }



        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in emptyLanguageTest. Err: " + ex.getMessage());
        }

    }

    @Test
    public void invalidLanguageTest()
    {
        try {

            String language = "&language=XX";

            HttpClient client = HttpClientBuilder.create().build();

            // Generate URL
            HttpGet request = new HttpGet(BASEURL + "person/1245/changes" + TOKENPARAM + language);

            HttpResponse response = client.execute(request);

            Assert.assertEquals("The Movie DB API did not return a success response as expected. Status returned: " + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Map resultJson = new Gson().fromJson(result.toString(), Map.class);

            Assert.assertTrue(resultJson.containsKey("changes"));

            List<Object> changes = (List<Object>) resultJson.get("changes");


            // Because this is a fluid response, I am wrapping this in a conditional because I am not sure when you will look at this, and cannot
            // guarantee any one result for this test. Normally, I would use another API to make changes to this person and look for the changes
            // but, seeing as this is not test data that I would be messing with, and that I do not appear to have appropriate permissions to add
            // and remove changes to a person, I will simply leave it to a very shallow test
            if(changes.size() != 0)
            {
                Map<String, String> keys = (Map<String, String>)changes.get(0);

                Assert.assertTrue("API result is missing the 'key' object in the response.", keys.containsKey("key"));
            }



        }
        catch(Exception ex)
        {
            Assert.fail("An exception was thrown in invalidLanguageTest. Err: " + ex.getMessage());
        }

    }

}
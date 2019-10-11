package org.socialhistoryservices.delivery.record.controller;

import org.codehaus.jackson.JsonNode;
import org.socialhistoryservices.delivery.DeliveryTestCase;
import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.record.entity.Contact;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration unit test for the Record API.
 */
public class RecordControllerTest extends DeliveryTestCase {



    /**
     * Helper method to test if a record is equal to json.
     * @param rec Record to test.
     * @param node Json object to test.
     */
    protected void validateRecord(Record rec, JsonNode node) {
        assertEquals(rec.getPid(),
                     node.path("pid").getTextValue());
        assertEquals(rec.getTitle(),
                     node.path("title").getTextValue());
        assertEquals(rec.getRealRestrictionType().toString(),
                     node.path("restrictionType").getTextValue());

        checkStringIfNotNull("embargo", rec.getEmbargo() != null ?
                                        df.format(rec.getEmbargo()) : null,
                             node);

        checkStringIfNotNull("restriction", rec.getRealRestriction(), node);

        if (!node.path("contact").isMissingNode()) {
            // Contact should not be null or it will not be possible to be
            // equivalent.
            assertNotNull(rec.getContact());
            validateContact(rec.getContact(), node.path("contact"));
        }

        JsonNode n = node.path("holdings");
        if (!n.isMissingNode()) {
            assertNotNull(rec.getHoldings());
            assertEquals(rec.getHoldings().size(), n.size());
            for(int i = 0; i < n.size(); i++) {
                JsonNode sn = n.path(i).path("signature");
                assertFalse(sn.isMissingNode());
                boolean has = false;
                for (Holding h : rec.getHoldings()) {
                    if (h.getSignature().equals(sn.getTextValue())) {
                        has = true;
                        validateHolding(h, n.path(i));
                    }
                }
                assertTrue(has);
            }
        }
    }

    /**
     * Helper method to test if a given contact is equivalent to the provided
     * contact json data.
     * @param c The contact to compare the json with.
     * @param node The json root node of the contact json data.
     */
    protected void validateContact(Contact c, JsonNode node) {
        checkStringIfNotNull("firstname", c.getFirstname(), node);
        checkStringIfNotNull("lastname", c.getLastname(), node);
        checkStringIfNotNull("preposition", c.getPreposition(), node);
        checkStringIfNotNull("address", c.getAddress(), node);
        checkStringIfNotNull("zipcode", c.getZipcode(), node);
        checkStringIfNotNull("location", c.getLocation(), node);
        checkStringIfNotNull("country", c.getCountry(), node);
        checkStringIfNotNull("email", c.getEmail(), node);
        checkStringIfNotNull("phone", c.getPhone(), node);
        checkStringIfNotNull("fax", c.getFax(), node);
    }

  

    /**
     * Helper method to test if a given holding is equivalent to the provided
     * holding json data.
     * @param h The holding to compare the json with.
     * @param node The json root node of the holding json data.
     */
    protected void validateHolding(Holding h, JsonNode node) {
        assertEquals(h.getSignature(), node.path("signature").getTextValue());
        assertEquals(h.getUsageRestriction().toString(),
                     node.path("usageRestriction").getTextValue());
        assertEquals(h.getStatus().toString(), node.path("status").getTextValue());

        checkStringIfNotNull("direction", h.getDirection(), node);
        checkIntIfNotNull("floor", h.getFloor(), node);
        checkStringIfNotNull("cabinet", h.getCabinet(), node);
        checkStringIfNotNull("shelf", h.getShelf(), node);
    }


    /**
     * Helper method to test getting a single record.
     * @param rec Record to test.
     * @throws java.io.IOException Thrown when the servlet call fails.
     * @throws javax.servlet.ServletException Thrown when the servlet call  
     * fails.
     */
    protected void validateGetRecord(Record rec)
            throws IOException, ServletException {
        MockHttpServletResponse response = mockJSONRequest(
                "GET", "/record/"+rec.getPid(), null);
        assertEquals(200, response.getStatus());
        JsonNode root = parseJSON(response);

        assertEquals(root.size(), 1);
        validateRecord(rec, root.path(0));
    }

    /**
     * Helper method to test getting multiple records.
     * @param rec1 Record to test.
     * @param rec2 Record to test.
     * @throws java.io.IOException Thrown when the servlet call fails.
     * @throws javax.servlet.ServletException Thrown when the servlet call  
     * fails.
     */
    protected void validateGetRecord(Record rec1, Record rec2)
            throws IOException, ServletException {
        MockHttpServletResponse response = mockJSONRequest(
            "GET", "/record/"+rec1.getPid()+
                   properties.getProperty("prop_pidSeparator", ",")+rec2
                .getPid(), null);
        assertEquals(200, response.getStatus());
        JsonNode root = parseJSON(response);

        assertEquals(root.size(), 2);
        validateRecord(rec1, root.path(0));
        validateRecord(rec2, root.path(1));
    }


    @Test
    /**
     * Test getting sets of records.
     */
    public void testRecordsGet() throws IOException, ServletException {
        MockHttpServletResponse response;

        // Retrieve database objects
        Record rec1 = recordService.getRecordByPid("12345");
        Record rec2 = recordService.getRecordByPid("12345.1");
        Record rec3 = recordService.getRecordByPid("12345.2");
        Record rec4 = recordService.getRecordByPid("12345.3");
        Record rec5 = recordService.getRecordByPid("12345.4");

        // TODO: Validate 404 (this fails because the mockup never checks if the pid is valid and always creates a new record).
        // response = mockJSONRequest("GET", "/record/non-existing-pid", null);
        //assertEquals(404, response.getStatus());

        // Validate single gets
        validateGetRecord(rec1);
        validateGetRecord(rec2);
        validateGetRecord(rec3);
        validateGetRecord(rec4);
        validateGetRecord(rec5);

        // Validate multiple gets
        validateGetRecord(rec1, rec2);
        validateGetRecord(rec4, rec5);
    }

    @Test
    /**
     * Test deleting records.
     */
    public void testRecordsDelete() throws IOException, ServletException {
        MockHttpServletResponse response;

        Record recParent = recordService.getRecordByPid("12345");
        Record recChild = recordService.getRecordByPid("12345.4");

        // Make sure it exists now
        validateGetRecord(recChild);

        // Delete the record
        response = mockJSONRequest("DELETE", "/record/12345.4", null);
        assertEquals(200, response.getStatus());

        // Make sure we're returned the parent now
        response = mockJSONRequest(
                "GET", "/record/"+recChild.getPid(), null);
        assertEquals(200, response.getStatus());
        JsonNode root = parseJSON(response);

        assertEquals(root.size(), 1);
        validateRecord(recParent, root.path(0));
    }

    @Test
    /**
     * Test editing records.
     */
    public void testRecordsEdit() throws IOException, ServletException {
        MockHttpServletResponse response;

        Record record = recordService.getRecordByPid("12345");

        // Make sure it exists now.
        validateGetRecord(record);
        // Now locally edit without saving.
        Record checkRecord = new Record();
        checkRecord.mergeWith(record);
        checkRecord.setPid(record.getPid());
        checkRecord.setTitle(record.getTitle());
        checkRecord.setRestriction("Test Restriction");
        checkRecord.setRestrictionType(Record.RestrictionType.RESTRICTED);
        List<Holding> hs = new ArrayList<Holding>();
        Holding h1 = new Holding();
        h1.setSignature("microfilm");
        h1.setFloor(2);
        hs.add(h1);
        Holding h2 = new Holding();
        h2.setSignature("original");
        h2.setShelf("5");
        hs.add(h2);
        checkRecord.setHoldings(hs);

        // Edit the record
        response = mockJSONRequest("PUT", "/record/12345",
            "{\"restrictionType\": \"RESTRICTED\","+
            " \"restriction\": \"Test Restriction\","+
            " \"holdings\": [{\"signature\": \"microfilm\", " +
                    "\"floor\": 2}, "+
            " {\"signature\": \"original\", \"shelf\": \"5\"}]}");
        assertEquals(200, response.getStatus());



        // Check that it was edited
        validateGetRecord(checkRecord);
    }

     @Test
    /**
     * Test creating records.
     */
    public void testRecordsCreate() throws IOException, ServletException {
        MockHttpServletResponse response;

        Record r = new Record();
        r.setComments("Bla bla bla");
        String pid = "456";
        try {
            r.setExternalInfo(lookupService.getRecordMetaDataByPid(pid));
        } catch (NoSuchPidException e) {
            r.getExternalInfo().setMaterialType(ExternalRecordInfo.MaterialType.ARCHIVE);
            r.setTitle("Open Archive 6");
        }
        r.setPid(pid);
        r.setEmbargo(new Date());

        r.setRestriction("Do not use");
        r.setRestrictionType(Record.RestrictionType.RESTRICTED);
        List<Holding> hs = new ArrayList<Holding>();
        Holding h1 = new Holding();
        h1.setSignature("microfilm");
        h1.setFloor(2);
        h1.setRecord(r);
        hs.add(h1);
        r.setHoldings(hs);

        Contact c = new Contact();
        c.setFirstname("John");
        c.setLastname("Doe");
        r.setContact(c);

        // Create the record
        response = mockJSONRequest("PUT", "/record/" + r.getPid(),
            "{\"restrictionType\": \""+r.getRestrictionType()+"\","+
            " \"restriction\": \""+r.getRestriction()+"\","+
            " \"comments\": \""+r.getComments()+"\","+
             " \"embargo\": \""+df.format(r.getEmbargo())+"\","+
             " \"contact\": {\"firstname\": \""+c.getFirstname()+"\", " +
                    "\"lastname\" : \""+c.getLastname()+"\"},"+
            " \"holdings\": [{\"signature\": \""+h1.getSignature()+"\", " +
                    "\"floor\": "+h1.getFloor()+"}]}");
        assertEquals(200, response.getStatus());



        // Check that it was edited
        validateGetRecord(r);
    }
}

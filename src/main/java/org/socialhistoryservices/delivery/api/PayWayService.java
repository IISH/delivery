package org.socialhistoryservices.delivery.api;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the PayWay service.
 */
public class PayWayService {
    private static final Log logger = LogFactory.getLog(PayWayService.class);

    private String address;
    private String passPhraseIn;
    private String passPhraseOut;
    private String projectName;

    public PayWayService(String address, String passPhraseIn, String passPhraseOut, String projectName) {
        this.address = address;
        this.passPhraseIn = passPhraseIn;
        this.passPhraseOut = passPhraseOut;
        this.projectName = projectName;
    }

    /**
     * Creates a simple PayWay message with the order id.
     *
     * @param orderId The order id.
     * @return A PayWay message.
     */
    public PayWayMessage getMessageForOrderId(Long orderId) {
        PayWayMessage message = new PayWayMessage();
        message.put("orderid", orderId);
        return message;
    }

    /**
     * Returns a link which directs the user to the payment page in PayWay.
     *
     * @param orderId The id of the order to be payed.
     * @return A link to the payment page in PayWay.
     */
    public String getPaymentPageRedirectLink(Long orderId) {
        PayWayMessage message = getMessageForOrderId(orderId);

        // Before creating the redirect link, add the project and place the SHA-1 signature
        addProject(message);
        signTransaction(message, true);

        return this.address + "/payment?" + URLEncodedUtils.format(getNameValuePairs(message), "UTF-8");
    }

    /**
     * Sends a message to PayWay.
     *
     * @param apiName The name of the PayWay API to send the message to.
     * @param message The PayWay message to send.
     * @return The responding PayWayMessage.
     * @throws InvalidPayWayMessageException In case the received PayWay message is invalid.
     */
    public PayWayMessage send(String apiName, PayWayMessage message) throws InvalidPayWayMessageException {
        PayWayMessage returnMessage = new PayWayMessage();

        // Before sending the message, add the project and place the SHA-1 signature
        addProject(message);
        signTransaction(message, true);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            List<NameValuePair> params = getNameValuePairs(message);
            HttpPost httpPost = new HttpPost(this.address + "/" + apiName);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            logger.debug(String.format("send(): Sending message to PayWay: %s", httpPost.getRequestLine()));

            returnMessage = httpClient.execute(httpPost, new PayWayResponseHandler());

            // Only return the PayWay message if it is valid
            if (!isValid(returnMessage)) {
                throw new InvalidPayWayMessageException(returnMessage);
            }
        }
        catch (IOException ioe) {
            logger.debug("send(): PayWay connection failed", ioe);
            throw new InvalidPayWayMessageException(returnMessage, ioe);
        }

        return returnMessage;
    }

    /**
     * Signs the transaction with a SHA-1 hash.
     *
     * @param message  The PayWay message.
     * @param incoming Whether this an incoming message for PayWay or not.
     */
    public void signTransaction(PayWayMessage message, boolean incoming) {
        // Make sure the signature is removed
        message.remove("shasign");

        // Obtain the pass phrase
        String passPhrase = incoming ? this.passPhraseIn : this.passPhraseOut;

        // Create the hash
        List<String> keyValues = new ArrayList<>();
        for (Map.Entry<String, Object> messageEntry : message.entrySet()) {
            keyValues.add(messageEntry.getKey() + "=" + messageEntry.getValue().toString());
        }
        String toBeHashed = StringUtils.collectionToDelimitedString(keyValues, passPhrase) + passPhrase;
        String hash = DigestUtils.sha1Hex(toBeHashed);

        // Sign the transaction
        message.put("shasign", hash);
    }

    /**
     * Make sure the message is valid by checking the SHA-1 hash.
     *
     * @param message The PayWay message.
     */
    public boolean isValid(PayWayMessage message) {
        Boolean success = message.getBoolean("success");
        if ((success == null) || success) { // Also allow if success is not present
            // Obtain the current signature
            String originalHash = message.remove("shasign").toString();

            // Sign the transaction
            signTransaction(message, false);

            // Compare the hashes
            String newHash = message.getString("shasign");
            return newHash.equalsIgnoreCase(originalHash);
        }

        return false;
    }

    /**
     * Adds the project to the message.
     *
     * @param message The PayWay message.
     */
    private void addProject(PayWayMessage message) {
        message.put("project", this.projectName);
    }

    /**
     * Transforms a PayWay message to a list of name/value pairs.
     *
     * @param message The PayWay message.
     * @return A list of name/value pairs.
     */
    private List<NameValuePair> getNameValuePairs(PayWayMessage message) {
        List<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, Object> messageEntry : message.entrySet()) {
            pairs.add(new BasicNameValuePair(messageEntry.getKey(), messageEntry.getValue().toString()));
        }
        return pairs;
    }

    /**
     * Transforms the response from PayWay to a PayWay message.
     */
    private static class PayWayResponseHandler implements ResponseHandler<PayWayMessage> {
        @Override
        public PayWayMessage handleResponse(HttpResponse httpResponse) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = httpResponse.getEntity().getContent();
            return mapper.readValue(inputStream, PayWayMessage.class);
        }
    }
}

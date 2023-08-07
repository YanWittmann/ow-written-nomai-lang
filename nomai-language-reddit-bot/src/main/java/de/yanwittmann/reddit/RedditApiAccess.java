package de.yanwittmann.reddit;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditApiAccess {

    private final static String REDDIT_BASE_API_URL = "https://www.reddit.com/api";
    private final static String REDDIT_OAUTH_BASE_API_URL = "https://oauth.reddit.com/api";

    private final static String ACCESS_TOKEN_URL = REDDIT_BASE_API_URL + "/v1/access_token";
    private final static String COMMENT_URL = REDDIT_OAUTH_BASE_API_URL + "/comment";

    private OkHttpClient client = new OkHttpClient();

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;

    private String accessToken;
    private long expiresAt;

    public RedditApiAccess(String clientId, String clientSecret, String username, String password) throws IOException {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
        this.getAccessToken();
    }

    public RedditApiAccess(String accessToken) {
        this.accessToken = accessToken;
        this.expiresAt = 0;
        this.clientId = null;
        this.clientSecret = null;
        this.username = null;
        this.password = null;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    protected String getAccessToken() throws IOException {
        if (accessToken == null || expiresAt < System.currentTimeMillis() / 1000) {
            final String auth = clientId + ":" + clientSecret;
            final String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            final String accessTokenRequestBody = "grant_type=password&username=" + username + "&password=" + password;

            final Request request = createRequest(ACCESS_TOKEN_URL, accessTokenRequestBody, "Basic " + encodedAuth, "POST");

            final JSONObject responseJson = sendRequestParseJson(request);
            accessToken = responseJson.getString("access_token");
            final int expiresIn = responseJson.getInt("expires_in");
            expiresAt = (System.currentTimeMillis() / 1000) + expiresIn;

            System.out.println("Authenticated with Reddit API. Access token expires in " + expiresIn + " seconds.");
            System.out.println("Access token: " + accessToken);
        }

        return accessToken;
    }

    public JSONObject postComment(String postId, String comment) throws IOException {
        final String commentRequestBody = "api_type=json&text=" + comment + "&thing_id=" + postId;

        final Request request = createRequest(COMMENT_URL, commentRequestBody, "Bearer " + getAccessToken(), "POST");

        return sendRequestParseJson(request);
    }

    public JSONObject commentWithImageAsset(String parentId, String beforeImageText, String afterImageText, String caption, String imagePath, String mimeType) throws IOException {
        String assetId = uploadImageAsset(imagePath, mimeType);
        System.out.println("Uploaded image asset with id " + assetId);

        JSONObject rtjson = new JSONObject();
        JSONArray document = new JSONArray();

        // '{"document":[{"e":"par","c":[]},{"e":"img","id":"hfhr4gk1mggb1","c":""}]}'

        document.put(new JSONObject().put("e", "par").put("c", new JSONArray().put(new JSONObject().put("e", "text").put("t", beforeImageText))));

        final JSONObject imgElement = new JSONObject().put("e", "img").put("id", assetId);
        if (caption != null && !caption.isEmpty()) {
            imgElement.put("c", caption);
        }
        document.put(imgElement);

        document.put(new JSONObject().put("e", "par").put("c", new JSONArray().put(new JSONObject().put("e", "text").put("t", afterImageText))));

        rtjson.put("document", document);
        System.out.println(rtjson);

        final String commentRequestBody = "api_type=json&thing_id=" + parentId + "&richtext_json=" + rtjson + "&validate_on_submit=true";

        final Request request = createRequest(COMMENT_URL, commentRequestBody, "Bearer " + getAccessToken(), "POST");

        return sendRequestParseJson(request);
    }

    public String extractKeyFromXml(String xml) {
        Pattern pattern = Pattern.compile("<Key>(.*?)</Key>");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("No <Key> found in the provided XML.");
        }
    }


    /**
     * What a nightmare this one was.<br>
     * <a href="https://github.com/not-an-aardvark/snoowrap/issues/280#issuecomment-879916824">Reference implementation</a>
     *
     * @param imagePath Path to the image to upload (local file)
     * @param mimeType  MIME type of the image
     * @return The key of the uploaded image asset that can be used to reference it in a comment
     * @throws IOException If the request fails
     */
    public String uploadImageAsset(String imagePath, String mimeType) throws IOException {
        final String UPLOAD_ASSET_URL = "https://oauth.reddit.com/api/media/asset.json?raw_json=1";

        // Step 1: Send a POST request to the api/media/asset.json endpoint with the filename and mimetype
        final RequestBody requestBody = new FormBody.Builder()
                .add("filepath", imagePath)
                .add("mimetype", mimeType)
                .build();

        final Request request = new Request.Builder()
                .url(UPLOAD_ASSET_URL)
                .header("Authorization", "Bearer " + getAccessToken())
                .post(requestBody)
                .build();

        final String uploadResponse = sendRequest(request);

        if (!uploadResponse.startsWith("{")) {
            final String key = extractKeyFromXml(uploadResponse);
            System.out.println("(1) Extracted key from XML: " + key);
            return key;
        }

        final JSONObject uploadResponseJson = new JSONObject(uploadResponse);

        // Step 2: Send a POST request to the upload URL with a FormData object that includes the form fields and the image file
        String uploadURL = "https:" + uploadResponseJson.getJSONObject("args").getString("action");
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        JSONArray fields = uploadResponseJson.getJSONObject("args").getJSONArray("fields");
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            multipartBodyBuilder.addFormDataPart(field.getString("name"), field.getString("value"));
        }

        multipartBodyBuilder.addFormDataPart("file", imagePath,
                RequestBody.create(Paths.get(imagePath).toFile(), MediaType.parse(mimeType)));

        Request uploadRequest = new Request.Builder()
                .url(uploadURL)
                .post(multipartBodyBuilder.build())
                .build();

        String responseUploadRequest = sendRequest(uploadRequest);

        if (!responseUploadRequest.startsWith("{")) {
            final String key = extractKeyFromXml(responseUploadRequest);
            System.out.println("(2) Extracted key from XML: " + key);
            return key;
        }

        return new JSONObject(responseUploadRequest).getJSONObject("asset").getString("asset_id");
    }

    public String findSubredditForPost(String postId) throws IOException {
        final String postInfoUrl = "https://oauth.reddit.com/api/info?id=" + postId;

        final Request request = createRequest(postInfoUrl, "", "Bearer " + getAccessToken(), "GET");

        final JSONObject responseJson = sendRequestParseJson(request);
        return responseJson.getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data").getString("subreddit");
    }

    private JSONObject sendRequestParseJson(Request request) throws IOException {
        System.out.println("Sending request to " + request.url());
        try (Response response = client.newCall(request).execute()) {
            boolean failed = false;
            if (!response.isSuccessful()) {
                failed = true;
            }

            final String responseString = response.body().string();
            final JSONObject jsonResponse;
            try {
                jsonResponse = new JSONObject(responseString);
            } catch (JSONException e) {
                throw new IOException("Failed to parse response: " + responseString, e);
            }

            // if errors: {"json": {"errors": [["NO_TEXT", "we need something here", "text"]]}}
            if (jsonResponse.has("json")) {
                final JSONObject json = jsonResponse.optJSONObject("json");
                if (json != null && json.has("errors")) {
                    final JSONArray errors = json.getJSONArray("errors");
                    if (!errors.isEmpty()) {
                        failed = true;
                    }
                }
            }

            if (failed) {
                throw new IOException(
                        "Request failed with code " + response.code() + "\n" +
                                "Request URL: " + request.url() + "\n" +
                                "Response: " + responseString + "\n"
                );
            }

            System.out.println("Response: " + jsonResponse);

            return jsonResponse;
        }
    }

    private String sendRequest(Request request) throws IOException {
        System.out.println("Sending request to " + request.url());
        try (Response response = client.newCall(request).execute()) {
            boolean failed = false;
            if (!response.isSuccessful()) {
                failed = true;
            }

            final String responseString = response.body().string();

            if (failed) {
                throw new IOException(
                        "Request failed with code " + response.code() + "\n" +
                                "Request URL: " + request.url() + "\n" +
                                "Response: " + responseString + "\n"
                );
            }

            System.out.println("Response: " + responseString);
            return responseString;
        }
    }

    private Request createRequest(String url, String requestBody, String authorization, String method) {
        System.out.println("Creating request to " + url + " with body " + requestBody);
        final RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/x-www-form-urlencoded"));

        final Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Authorization", authorization)
                .addHeader("User-Agent", "any-name");

        if ("POST".equals(method)) {
            builder.post(body);
        } else if ("GET".equals(method)) {
            builder.get();
        }

        return builder.build();
    }

    public String extractPostId(String str) {
        String postId;

        if (str.startsWith("t3_")) {
            return str;
        } else if (str.startsWith("http")) {
            String[] parts = str.split("/");
            postId = parts[parts.length - 2];
        } else {
            postId = str;
        }

        return "t3_" + postId;
    }
}

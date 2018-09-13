package com.library.essay.tinymce.spellchecker;

/*
 Copyright (c) 2008
 Rich Irwin <rirwin@seacliffedu.com>, Andrey Chorniy <andrey.chorniy@gmail.com>

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.
*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andrey Chorniy
 * Date: 03.01.2010
 * This is a base class for implementing TinyMCE JSON based request/response
 * Abstract classes define SpellcheckMethod required to implement spellchecking functionality/lifecycle
 *  
 * TODO: refactor to single servlet which will use particular implementation based on delegating SpellcheckMethod to spellchecker implementation
 * TODO: Delegate abstract SpellcheckMethod to interface (named as SpellcheckerAdapter for example) to move them away from servlet code
 * TODO: Servlet will only do specific servlet functions: Reading JSONRequest, delegate spellchecking functionality to SpellcheckerAdapter and write JSON-response
 * TODO: SpellcheckerAdapter creation will be done in the servlet.init() method
 */

public abstract class TinyMCESpellCheckerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String MAX_SUGGESTIONS_COUNT_PARAM = "maxSuggestionsCount";
    private static final String PRELOADED_LANGUAGES_PARAM = "preloadedLanguages";

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String GET_METHOD_RESPONSE_ERROR = "This servlet expects a JSON encoded body, POSTed to this URL";

    private static final String DEFAULT_PRELOADED_LANGUAGES = "en-us";

    private int maxSuggestionsCount = 25;


    @Override
    public void init() throws ServletException {
        super.init();
        preLoadSpellcheckers();
        readMaxSuggestionsCount();
    }

    private void preLoadSpellcheckers() throws ServletException {
        String preloaded = getServletConfig().getInitParameter(PRELOADED_LANGUAGES_PARAM);
        if (preloaded == null || preloaded.trim().length() == 0) {
            preloaded = DEFAULT_PRELOADED_LANGUAGES;
        }

        String[] preloadedLanguages = preloaded.split(";");
        for (String preloadedLanguage : preloadedLanguages) {
            try {
                preLoadLanguageChecker(preloadedLanguage);
            } catch (SpellCheckException e) {
                //wrong servlet configuration
                throw new ServletException(e);
            }
        }
    }

    protected abstract void preLoadLanguageChecker(String preloadedLanguage) throws SpellCheckException;

    /**
     * This method look for the already created SpellChecker object in the cache, if it is not present in the cache then
     * it try to load it and put newly created object in the cache. SpellChecker loading is quite expensive operation
     * to do it for every spell-checking request, so in-memory-caching here is almost a "MUST to have"
     *
     * @param lang the language code like "en" or "en-us"
     * @return instance of SpellChecker for particular implementation
     * @throws SpellCheckException if method failed to load the SpellChecker for lang (it happens if there is no
     *                             dictionaries for that language was found in the classpath
     */
    protected abstract Object getChecker(String lang) throws SpellCheckException;


    private void readMaxSuggestionsCount() throws ServletException {
        String suggestionsCountParam = getServletConfig().getInitParameter(MAX_SUGGESTIONS_COUNT_PARAM);
        if (suggestionsCountParam != null && suggestionsCountParam.trim().length() > 0) {
            try {
                maxSuggestionsCount = Integer.parseInt(suggestionsCountParam.trim());
            } catch (NumberFormatException ex) {
                //wrong servlet configuration, possibly a typo
                throw new ServletException(ex);
            }
        }
    }


    /**
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy() {
        super.destroy();
        //remove unused objects from memory
        try {
            releaseResources();
        } catch (Throwable ex) {
            logger.error("Failed to releaseResources()", ex);
        }
    }

    /**
     * This method will be called from {@link javax.servlet.Servlet#destroy()} to release resources
     */
    protected abstract void releaseResources();


    /**
     * GET method is not supported
     * @see HttpServlet#doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setResponeHeaders(response);
        PrintWriter pw = response.getWriter();
        pw.println(GET_METHOD_RESPONSE_ERROR);
    }

    /**
     * There are three types of requests are supported
     * h3. "checkWords"
     * Check the list of words passed as a second argument of the "params" array
     * First argument of the params array, define the "language"
     *
     * h4. Example request body (JSON)
     * <pre>
     * {
     *   "method" : "checkWords",
     *   "params" : [ "en-us", ["usa","cawboy", "apple"] ]
     * }
     * </pre>
     * h4. Example Response (JSON)
     * <pre>
     *     {"id":null,"result":["cawboy"],"error":null}
     * </pre>
     *
     * h3. "getWordSuggestions"
     * Get suggestion for single word, passed as the 2-nd element in the "params" array
     * h4. Example request body (JSON)
     * <pre>
     *{
     *  "method" : "getWordSuggestions",
     *  "params" : [ "en-us", "Guugli" ]
     *}
     * </pre>
     * h4. Example Response (JSON)
     * <pre>
     *     {"id":null,"result":["Google"],"error":null}
     * </pre>
     *
     * h3. "spellcheck"
     * This kind of request is used by TinyMCE 4.0
     * h4. Example request body (JSON)
     * <pre>
     *{
     *  "id" : "${request-id}",
     *  "method" : "spellcheck",
     *  "params" : { "lang": "en",
     *               "words": ["cawboy", "apple", "Guugli"]
     *             }
     *}
     * </pre>
     * h4. Example Response (JSON)
     * <pre>
     * {
     *    "id":"${request-id}",
     *    "result": {
     *      "cawboy" : ["cowboy"],
     *      "Guugli" : ["Google"]
     *    }
     * }
     * </pre>
     * @param request 
     * @param response 
     * @throws ServletException 
     * @throws IOException 
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setResponeHeaders(response);
        try {
            JSONObject jsonInput = readRequest(request);

            String methodName = jsonInput.optString("method");
            if (methodName == null || methodName.trim().equals("")) {
                throw new SpellCheckException("Wrong spellchecker-method-name:" + methodName);
            }


            JSONObject jsonOutput = new JSONObject();
            SpellcheckMethod method = SpellcheckMethod.valueOf(methodName.trim());

            switch (method) {
                case checkWords:
                    jsonOutput.put("result", checkWords(jsonInput.optJSONArray("params")));
                    break;
                case getSuggestions:
                    jsonOutput.put("result", getWordSuggestions(jsonInput.optJSONArray("params")));
                    break;
                case spellcheck:
                    //This is TinyMCe 4.0
                    jsonOutput.put("id", jsonInput.optString("id"));
                    jsonOutput.put("result", getAllWordSuggestions(jsonInput.optJSONObject("params")));
                    break;
                default:
                    throw new SpellCheckException("Spellchecker method not supported {" + methodName + "}");
            }

            PrintWriter pw = response.getWriter();
            pw.println(jsonOutput.toString());

        } catch (SpellCheckException se) {
            logger.warn("Failed to perform spellcheck operation", se);
            returnError(response, se.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to perform spellcheck operation (generic error)", e);
            returnError(response, e.getMessage());
        }

        response.getWriter().flush();
    }

    private JSONObject getAllWordSuggestions(JSONObject params) throws SpellCheckException, JSONException {
        JSONObject suggestions = new JSONObject();
        JSONArray checkedWords = params.optJSONArray("words");
        String lang = params.optString("lang");
        lang = ("".equals(lang)) ? DEFAULT_LANGUAGE : lang;
        List<String> misspelledWords = findMisspelledWords(new JsonArrayIterator(checkedWords), lang);
        for (String misspelledWord : misspelledWords) {
            List<String> suggestionsList = findSuggestions(misspelledWord, lang, maxSuggestionsCount);
            suggestions.put(misspelledWord, suggestionsList);
        }
        return suggestions;
    }

    private JSONObject readRequest(HttpServletRequest request) throws SpellCheckException {
        JSONObject jsonInput;
        try {
            jsonInput = new JSONObject(getRequestBody(request));
        } catch (JSONException e) {
            throw new SpellCheckException("Could not interpret JSON request body", e);
        } catch (IOException e) {
            throw new SpellCheckException("Error reading request body", e);
        }
        return jsonInput;
    }

    /**
     * @param response response
     */
    private void setResponeHeaders(HttpServletResponse response) {
        response.setContentType("text/plain; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", System.currentTimeMillis());
    }


    /**
     * @param request request
     * @return read the request content and return it as String object
     * @throws IOException
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = request.getReader().readLine();
        while (null != line) {
            sb.append(line);
            line = request.getReader().readLine();
        }
        return sb.toString();
    }

    /**
     * write error response as JSONObject to output
     * @param response response
     * @param message error message
     */
    private void returnError(HttpServletResponse response, String message) {
        response.setStatus(500);
        try {
            response.getWriter().print("{'id':null,'response':[],'error':'" + message + "'}");
        } catch (IOException e) {
            logger.info("Failed to send error response to client", e);
        }
    }

    private JSONArray checkWords(JSONArray params) throws SpellCheckException {
        JSONArray misspelledWords = new JSONArray();
        if (params != null) {
            JSONArray checkedWords = params.optJSONArray(1);
            String lang = params.optString(0);
            lang = ("".equals(lang)) ? DEFAULT_LANGUAGE : lang;

            List<String> misspelledWordsList = findMisspelledWords(new JsonArrayIterator(checkedWords), lang);


            for (String misspelledWord : misspelledWordsList) {
                misspelledWords.put(misspelledWord);
            }

        }
        return misspelledWords;
    }

    private JSONArray getWordSuggestions(JSONArray params) throws SpellCheckException {
        JSONArray suggestions = new JSONArray();
        if (params != null) {
            String lang = params.optString(0);
            lang = ("".equals(lang)) ? DEFAULT_LANGUAGE : lang;
            String word = params.optString(1);
            List<String> suggestionsList = findSuggestions(word, lang, maxSuggestionsCount);
            for (String suggestion : suggestionsList) {
                suggestions.put(suggestion);
            }
        }
        return suggestions;
    }


    protected abstract List<String> findMisspelledWords(Iterator<String> checkedWordsIterator,
                                                        String lang) throws SpellCheckException;


    protected abstract List<String> findSuggestions(String word, String lang,
                                                    int maxSuggestionsCount) throws SpellCheckException;

    private static class JsonArrayIterator implements Iterator<String> {
        private JSONArray array;
        private int index = 0;

        private JsonArrayIterator(JSONArray array) {
            this.array = array;
        }

        public boolean hasNext() {
            return index < array.length();
        }

        public String next() {
            return array.optString(index++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}

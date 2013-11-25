package com.ctrip.framework.cdashboard.io.servlet.data;

import com.ctrip.framework.cdashboard.common.io.CommandName;
import com.ctrip.framework.cdashboard.common.io.CommandProcessorProvider;
import com.ctrip.framework.cdashboard.io.servlet.ServletInputAdapter;
import com.ctrip.framework.cdashboard.io.servlet.ServletOutputAdapter;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Implement JSONP servlet for CDashboard io,
 * handle user input request via http get request
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:12 PM
 */
@WebServlet(urlPatterns = {"/jsonp/*"}, asyncSupported = true)
public class JSONPServlet extends HttpServlet {
    private static final String REQUEST_DATA = "reqdata";
    private static final String CALLBACK = "callback";

    /**
     * Handle http request
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        ServletInputAdapter inputAdapter;
        String httpMethod = httpRequest.getMethod();
        String url = httpRequest.getRequestURI();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String cmd = url.substring(url.lastIndexOf("/") + 1);
        if ("get".equalsIgnoreCase(httpMethod)) {
            byte[] json = buildJsonFromRequest(httpRequest);
            InputStream inputStream = new ByteArrayInputStream(json);
            inputAdapter = new ServletInputAdapter(inputStream, CommandName.value(StringUtils.lowerCase(cmd)));
        } else {
            throw new IOException("Not support this http method: " + httpMethod);
        }
        AsyncContext context = request.startAsync();
        ServletOutputAdapter outputAdapter = new ServletOutputAdapter(context);
        CommandProcessorProvider.getInstance().getCommandProcessor().processCommand(inputAdapter, outputAdapter);
    }

    /**
     * Build json byte data from http request
     *
     * @param httpRequest
     * @return
     */
    private byte[] buildJsonFromRequest(HttpServletRequest httpRequest) {
        String json = httpRequest.getParameter(REQUEST_DATA);
        if (StringUtils.isNotBlank(json)) {
            String callback = httpRequest.getParameter(CALLBACK);
            if (StringUtils.isNotBlank(callback)) {
                JSONObject jsonObj = new JSONObject(json);
                jsonObj.put(CALLBACK, callback);
                return jsonObj.toString().getBytes(Charset.forName("UTF-8"));
            }
            return json.getBytes(Charset.forName("UTF-8"));
        }
        return null;
    }


}

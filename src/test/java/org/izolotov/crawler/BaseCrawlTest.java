package org.izolotov.crawler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BaseCrawlTest {

    public static final int PORT = 8081;
    public static Server server;

    public static final String SUCCESS_CONTENT = "<h1>Hello world!</h1>";
    public static final String CONTENT_TYPE = "text/html;charset=utf-8";

    public static final String SUCCESS_1 = String.format("http://localhost:%d/success_1.html", PORT);
    public static final String SUCCESS_2 = String.format("http://localhost:%d/success_2.html", PORT);
    public static final String SUCCESS_3 = String.format("http://localhost:%d/success_3.html", PORT);
    public static final String SUCCESS_REMOTE_1 = String.format("http://remote:%d/success_1.html", PORT);
    public static final String REDIRECT_TO_SUCCESS_2 = String.format("http://localhost:%d/redirect_to_success_2.html", PORT);
    public static final String REDIRECT_TO_SUCCESS_3 = String.format("http://localhost:%d/redirect_to_success_3.html", PORT);
    public static final String REDIRECT_TO_REDIRECT_TO_SUCCESS_3 = String.format("http://localhost:%d/redirect_to_redirect.html", PORT);
    public static final String REDIRECT_TO_SUCCESS_REMOTE_1 = String.format("http://localhost:%d/redirect_to_remote_1.html", PORT);
    public static final String REDIRECT_2_TO_SUCCESS_REMOTE_1 = String.format("http://localhost:%d/redirect_2_to_remote_1.html", PORT);

    public static class ResponseHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            final String requestUrl = baseRequest.getRequestURL().toString();
            if (SUCCESS_1.equals(requestUrl) ||
                    SUCCESS_2.equals(requestUrl) ||
                    SUCCESS_3.equals(requestUrl)) {
                successResponse(baseRequest, response);
            } else if (REDIRECT_TO_SUCCESS_2.equals(requestUrl)) {
                redirectResponse(SUCCESS_2, baseRequest, response);
            } else if (REDIRECT_TO_SUCCESS_3.equals(requestUrl)) {
                redirectResponse(SUCCESS_3, baseRequest, response);
            } else if (REDIRECT_TO_REDIRECT_TO_SUCCESS_3.equals(requestUrl)) {
                redirectResponse(REDIRECT_TO_SUCCESS_3, baseRequest, response);
            } else if (REDIRECT_TO_SUCCESS_REMOTE_1.equals(requestUrl)) {
                redirectResponse(SUCCESS_REMOTE_1, baseRequest, response);
            } else if (REDIRECT_2_TO_SUCCESS_REMOTE_1.equals(requestUrl)) {
                redirectResponse(SUCCESS_REMOTE_1, baseRequest, response);
            }
        }

        private void successResponse(Request baseRequest, HttpServletResponse response) throws IOException {
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(SUCCESS_CONTENT);
            baseRequest.setHandled(true);
        }

        private void redirectResponse(String targetUrl, Request baseRequest, HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("location", targetUrl);
            baseRequest.setHandled(true);
        }
    }

    @BeforeClass
    public static void startServer() throws Exception {
        server = new Server(PORT);
        server.setHandler(new ResponseHandler());
        server.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }
}

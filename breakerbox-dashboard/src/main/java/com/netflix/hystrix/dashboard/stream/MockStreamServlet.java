package com.netflix.hystrix.dashboard.stream;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Simulate an event stream URL by retrieving pre-canned data instead of going to live servers.
 */
public class MockStreamServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(MockStreamServlet.class);

    public MockStreamServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String filename = request.getParameter("file");
        if (filename == null) {
            // default to using hystrix.stream
            filename = "hystrix.stream";
        } else {
            // strip any .. / characters to avoid security problems
            filename = filename.replaceAll("\\.\\.", "");
            filename = filename.replaceAll("/", "");
        }
        int delay = 500;
        String delayArg = request.getParameter("delay");
        if (delayArg != null) {
            delay = Integer.parseInt(delayArg);
        }

        int batch = 1;
        String batchArg = request.getParameter("batch");
        if (batchArg != null) {
            batch = Integer.parseInt(batchArg);
        }

        String data = getFileFromPackage(filename);
        String lines[] = data.split("\n");

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        int batchCount = 0;
        // loop forever unless the user closes the connection
        for (;;) {
            for (String s : lines) {
                s = s.trim();
                if (s.length() > 0) {
                    try {
                        response.getWriter().println(s);
                        response.getWriter().println(""); // a newline is needed after each line for the events to trigger
                        response.getWriter().flush();
                        batchCount++;
                    } catch (Exception e) {
                        logger.warn("Exception writing mock data to output.", e);
                        // most likely the user closed the connection
                        return;
                    }
                    if (batchCount == batch) {
                        // we insert the delay whenever we finish a batch
                        try {
                            // simulate the delays we get from the real feed
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        // reset
                        batchCount = 0;
                    }
                }
            }
        }
    }

    private String getFileFromPackage(String filename) {
        try {
            return Files.toString(new File(Resources.getResource("assets/" + filename).toURI()), Charsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not find file: " + filename, e);
        }
    }
}
package com.shoppingcart.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ResponseLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        BufferedResponseWrapper responseWrapper = new BufferedResponseWrapper(httpServletResponse);
        chain.doFilter(request, responseWrapper);
        byte[] responseData = responseWrapper.getData();
        String responseBody = new String(responseData, response.getCharacterEncoding());
        logger.info("RESPONSE BODY: {}", responseBody);
        ServletOutputStream out = httpServletResponse.getOutputStream();
        out.write(responseData);
        out.flush();
    }
}

class BufferedResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final ServletOutputStream servletOutputStream = new ServletOutputStream() {
        @Override public void write(int b) { output.write(b); }
        @Override public boolean isReady() { return true; }
        @Override public void setWriteListener(WriteListener listener) {}
    };
    private PrintWriter printWriter;

    public BufferedResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() {
        if (printWriter == null) {
            printWriter = new PrintWriter(output);
        }
        return printWriter;
    }

    public byte[] getData() throws IOException {
        if (printWriter != null) {
            printWriter.flush();
        }
        return output.toByteArray();
    }
}

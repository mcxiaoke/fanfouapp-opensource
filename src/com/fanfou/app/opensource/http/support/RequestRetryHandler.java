package com.fanfou.app.opensource.http.support;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

import com.fanfou.app.opensource.AppContext;

public class RequestRetryHandler implements HttpRequestRetryHandler {
    private static final int RETRY_SLEEP_TIME_MILLIS = 1500;
    private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
    private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

    static {
        // Retry if the server dropped connection on us
        RequestRetryHandler.exceptionWhitelist
                .add(NoHttpResponseException.class);
        // retry-this, since it may happens as part of a Wi-Fi to 3G failover
        RequestRetryHandler.exceptionWhitelist.add(UnknownHostException.class);
        // retry-this, since it may happens as part of a Wi-Fi to 3G failover
        RequestRetryHandler.exceptionWhitelist.add(SocketException.class);

        // never retry timeouts
        RequestRetryHandler.exceptionBlacklist
                .add(InterruptedIOException.class);
        // never retry SSL handshake failures
        RequestRetryHandler.exceptionBlacklist.add(SSLHandshakeException.class);
    }

    private final int maxRetries;

    public RequestRetryHandler(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public boolean retryRequest(final IOException exception,
            final int executionCount, final HttpContext context) {
        boolean retry;

        final Boolean b = (Boolean) context
                .getAttribute(ExecutionContext.HTTP_REQ_SENT);
        final boolean sent = ((b != null) && b.booleanValue());

        if (executionCount > this.maxRetries) {
            // Do not retry if over max retry count
            retry = false;
        } else if (RequestRetryHandler.exceptionBlacklist.contains(exception
                .getClass())) {
            // immediately cancel retry if the error is blacklisted
            retry = false;
        } else if (RequestRetryHandler.exceptionWhitelist.contains(exception
                .getClass())) {
            // immediately retry if error is whitelisted
            retry = true;
        } else if (!sent) {
            // for most other errors, retry only if request hasn't been fully
            // sent yet
            retry = true;
        } else {
            // resend all idempotent requests
            final HttpUriRequest currentReq = (HttpUriRequest) context
                    .getAttribute(ExecutionContext.HTTP_REQUEST);
            final String requestType = currentReq.getMethod();
            if (!requestType.equals("POST")) {
                retry = true;
            } else {
                retry = false;
            }
        }

        if (retry) {
            SystemClock.sleep(RequestRetryHandler.RETRY_SLEEP_TIME_MILLIS);
        } else {
            if (AppContext.DEBUG) {
                exception.printStackTrace();
            }
        }

        return retry;
    }
}

package com.sogou.map.kubbo.remote.session.inner;

import com.sogou.map.kubbo.common.Constants;
import com.sogou.map.kubbo.common.util.StringUtils;
import com.sogou.map.kubbo.remote.Channel;
import com.sogou.map.kubbo.remote.RemoteExecutionException;
import com.sogou.map.kubbo.remote.RemotingException;
import com.sogou.map.kubbo.remote.session.SessionChannel;
import com.sogou.map.kubbo.remote.session.SessionHandler;
import com.sogou.map.kubbo.remote.session.Request;
import com.sogou.map.kubbo.remote.session.Response;
import com.sogou.map.kubbo.remote.transport.handler.AbstractChannelHandlerDelegate;

/**
 * InnerSessionHandler
 * 
 * @author liufuliang
 */
public class InnerSessionHandler extends AbstractChannelHandlerDelegate {

    private final SessionHandler handler;

    public InnerSessionHandler(SessionHandler handler){
        super(handler);
        if (handler == null) {
            throw new IllegalArgumentException("handler == NULL");
        }
        this.handler = handler;
    }

    @Override
    public void onConnected(Channel channel) throws RemotingException {
        SessionChannel sessionChannel = InnerSessionChannel.getOrAddChannel(channel);
        try {
            handler.onConnected(sessionChannel);
        } finally {
            InnerSessionChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void onDisconnected(Channel channel) throws RemotingException {
        SessionChannel sessionChannel = InnerSessionChannel.getOrAddChannel(channel);
        try {
            handler.onDisconnected(sessionChannel);
        } finally {
            InnerSessionChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void onSent(Channel channel, Object message) throws RemotingException {
        Throwable exception = null;
        try {
            SessionChannel sessionChannel = InnerSessionChannel.getOrAddChannel(channel);
            try {
                handler.onSent(sessionChannel, message);
            } finally {
                InnerSessionChannel.removeChannelIfDisconnected(channel);
            }
        } catch (Throwable t) {
            exception = t;
        }
        
        // notify future sent
        if (message instanceof Request) {
            Request request = (Request) message;
            InternalResponseFuture.sent(channel, request);
        }
        
        // throw
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else if (exception instanceof RemotingException) {
                throw (RemotingException) exception;
            } else {
                throw new RemotingException(channel.getLocalAddress(), channel.getRemoteAddress(),
                                            exception.getMessage(), exception);
            }
        }
    }

    @Override
    public void onReceived(Channel channel, Object message) throws RemotingException {
        SessionChannel sessionChannel = InnerSessionChannel.getOrAddChannel(channel);
        try {
            if (message instanceof Request) {
                // handle request.
                Request request = (Request) message;
                if (request.isEvent()) {
                    handleEvent(channel, request);
                } else if (request.isTwoWay()) {
                    Response response = handleRequest(sessionChannel, request);
                    channel.send(response);
                } else {
                    handleRequest(sessionChannel, request);
                }
            } else if (message instanceof Response) {
                // handle response.
                handleResponse(channel, (Response) message);
            } else if (message instanceof String) {
                handler.onReceived(sessionChannel, message);
            } else {
                // handle none
                handler.onReceived(sessionChannel, message);
            }
        } finally {
            InnerSessionChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void onExceptonCaught(Channel channel, Throwable exception) throws RemotingException {
        if (exception instanceof RemoteExecutionException) {
            RemoteExecutionException e = (RemoteExecutionException) exception;
            Object msg = e.getRequest();
            if (msg instanceof Request) {
                Request req = (Request) msg;
                if (req.isTwoWay() && ! req.isHeartbeat()) {
                    Response res = new Response(req.getId(), req.getVersion());
                    res.setStatus(Response.SERVER_ERROR);
                    res.setErrorMessage(StringUtils.toString(e));
                    channel.send(res);
                    return;
                }
            }
        }
        SessionChannel sessionChannel = InnerSessionChannel.getOrAddChannel(channel);
        try {
            handler.onExceptonCaught(sessionChannel, exception);
        } finally {
            InnerSessionChannel.removeChannelIfDisconnected(channel);
        }
    }
    
    protected void handleEvent(Channel channel, Request req) throws RemotingException {
        if (req.getData() != null && req.getData().equals(Request.EVENT_READONLY)) {
            channel.setAttribute(Constants.CHANNEL_ATTRIBUTE_READONLY_KEY, Boolean.TRUE);
        }
    }

    protected Response handleRequest(SessionChannel channel, Request req) throws RemotingException {
        Response res = new Response(req.getId(), req.getVersion());
        // bad requests
        if (req.isBroken()) {
            Object data = req.getData();

            String msg;
            if (data == null) msg = null;
            else if (data instanceof Throwable) msg = StringUtils.toString((Throwable) data);
            else msg = data.toString();
            res.setErrorMessage("Fail to decode request due to: " + msg);
            res.setStatus(Response.BAD_REQUEST);

            return res;
        }
        // find handler by message class.
        Object msg = req.getData();
        try {
            // handle data.
            Object result = handler.reply(channel, msg);
            res.setStatus(Response.OK);
            res.setResult(result);
        } catch (Throwable e) {
            res.setStatus(Response.SERVICE_ERROR);
            res.setErrorMessage(StringUtils.toString(e));
        } 
        return res;
    }

    protected void handleResponse(Channel channel, Response response) throws RemotingException {
        if (response != null && !response.isHeartbeat()) {
            InternalResponseFuture.received(channel, response);
        }
    }
}
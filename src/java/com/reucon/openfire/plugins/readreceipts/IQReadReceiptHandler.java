package com.reucon.openfire.plugins.readreceipts;

import java.util.Collections;
import java.util.Iterator;
import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError.Condition;

public class IQReadReceiptHandler extends IQHandler implements ServerFeaturesProvider {

    private static final Logger Log = LoggerFactory.getLogger(ReadReceiptsPlugin.class);

    private static final String NAMESPACE = "jabber:iq:read-receipt";
    private final IQHandlerInfo info = new IQHandlerInfo("query", NAMESPACE);
    private PersistenceManager persistenceManager;

    public IQReadReceiptHandler() {
        super("XMPP Read Receipt Handler");
    }

    public IQ handleIQ(IQ packet) throws UnauthorizedException {

        Element queryElement = packet.getChildElement();
        String with = queryElement.attributeValue("with");
        String vv = queryElement.attributeValue("vv");
        Integer ts = null;

        Log.debug("Processing " + packet.getType() + (vv != null ? " vv" : "") + " request from " + packet.getFrom().toString() + " with " + with);

        IQ reply = IQ.createResultIQ(packet);
        Element query = reply.setChildElement("query", NAMESPACE);

        if (packet.getFrom() == null || packet.getType() == null || with == null) {
            reply.setError(Condition.forbidden);
        }
        else {
            String from = packet.getFrom().toBareJID();
            if (packet.getType().toString().equals("get")){
                if (vv != null){
                    ts = this.persistenceManager.getReceiptTimestamp(with, from);
                    query.addAttribute("vv", "true");
                }
                else {
                    ts = this.persistenceManager.getReceiptTimestamp(from, with);
                }
            }
            else if (packet.getType().toString().equals("set")){
                ts = this.persistenceManager.saveReceipt(from, with);
            }
            if (ts != null){
                query.addAttribute("ts", String.valueOf(ts));
            }
            query.addAttribute("with", with);
        }
        return reply;
    }

    public IQHandlerInfo getInfo() {
        return this.info;
    }

    public Iterator<String> getFeatures() {
        return Collections.singleton(NAMESPACE).iterator();
    }

    public void initialize(XMPPServer server, PersistenceManager persistenceManager) {
        super.initialize(server);
        this.persistenceManager = persistenceManager;
    }
}

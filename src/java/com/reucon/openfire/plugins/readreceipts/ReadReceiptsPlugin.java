package com.reucon.openfire.plugins.readreceipts;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.io.File;

/**
 * ReadReceipts plugin for Openfire.
 */
public class ReadReceiptsPlugin implements Plugin, PacketInterceptor
{
    private InterceptorManager interceptorManager;
    private PersistenceManager persistenceManager;
    private IQReadReceiptHandler iqReadReceiptHandler;
    private static final Logger Log = LoggerFactory.getLogger(ReadReceiptsPlugin.class);

    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        persistenceManager = new PersistenceManager();
        interceptorManager = InterceptorManager.getInstance();

        // register with interceptor manager
        interceptorManager.addInterceptor(this);

        iqReadReceiptHandler = new IQReadReceiptHandler();
        try {
            iqReadReceiptHandler.initialize(XMPPServer.getInstance(), persistenceManager);
            iqReadReceiptHandler.start();
            IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
            iqRouter.addHandler(iqReadReceiptHandler);
        } catch (Exception e) {
            Log.error("Unable to initialize and start " + iqReadReceiptHandler.getClass());
        }
    }

    public void destroyPlugin()
    {
        interceptorManager.removeInterceptor(this);
        try {
            IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
            iqRouter.removeHandler(iqReadReceiptHandler);
            iqReadReceiptHandler.stop();
            iqReadReceiptHandler.destroy();
        } catch (Exception e) {
            Log.warn("Unable to stop and destroy " + iqReadReceiptHandler.getClass());
        }
    }

    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {

        //Case user sends message, update timestamp of corresponding sender. That means all previous messages have been read.
        if (!processed
            && incoming
            && packet instanceof Message
            && packet.getTo() != null
            && packet.getTo().getResource() == null
            && packet.getFrom() != null) {

            Message msg = (Message) packet;

            if (msg.getBody() != null && (msg.getType() == Message.Type.chat || msg.getType() == Message.Type.groupchat)) {

                String userJID = packet.getFrom().toBareJID();
                String senderJID = packet.getTo().toBareJID();

                Log.debug("Updating timestamp for userJID {} and senderJID {}", new Object[]{userJID, senderJID});
                persistenceManager.saveReceipt(userJID, senderJID);
            }
        }
    }
}

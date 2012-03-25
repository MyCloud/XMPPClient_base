package org.apache.android.xmpp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;

public class XMPPClient extends Activity {

    private ArrayList<String> messages = new ArrayList();
    private Handler mHandler = new Handler();
    private SettingsDialog mDialog;
    private EditText mRecipient;
    private EditText mSendText;
    private ListView mList;
    private XMPPConnection connection;
    private XMPPPreferences cPreferences;
	private String username;
	private String password;
	private String service;
	private String host;
	private int port;


    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i("XMPPClient", "onCreate called");
        setContentView(R.layout.main);

        mRecipient = (EditText) this.findViewById(R.id.recipient);
        Log.i("XMPPClient", "mRecipient = " + mRecipient);
        mSendText = (EditText) this.findViewById(R.id.sendText);
        Log.i("XMPPClient", "mSendText = " + mSendText);
        mList = (ListView) this.findViewById(R.id.listMessages);
        Log.i("XMPPClient", "mList = " + mList);
        setListAdapter();

        // Dialog for getting the xmpp settings
        cPreferences = new XMPPPreferences(this);

        // Dialog for getting the xmpp settings
        mDialog = new SettingsDialog(this);
        //MDJ SharedPreferences preferences = getPreferences(MODE_PRIVATE);        
        
        // Set a listener to show the settings dialog
        Button setup = (Button) this.findViewById(R.id.setup);
        setup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mDialog.show();
                    }
                });
            }
        });

        // Set a listener to send a chat text message
        Button send = (Button) this.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String to = mRecipient.getText().toString();
                String text = mSendText.getText().toString();
                if (to.isEmpty()) {
                	to = "2menno@gmail.com";
                }
                Log.i("XMPPClient", "Sending text [" + text + "] to [" + to + "]");
                Message msg = new Message(to, Message.Type.chat);
                msg.setBody(text);
                if ( connection == null && host!=null && port != 0 && service!=null ) {
                	createConnection(host, port, service);
                }
                if ( connection != null ) {
                	if ( !connection.isConnected()) {
                        Toast.makeText( getApplicationContext(), "reconnect", Toast.LENGTH_SHORT).show();
                        connectConnection();
                	}
                	if (connection != null && !connection.isAuthenticated()) {
                        Toast.makeText( getApplicationContext(), "login connect", Toast.LENGTH_SHORT).show();
                		loginConnection( username, password);
                	}                		
            		if ( connection != null && connection.isAuthenticated()) {
            			connection.sendPacket(msg);
            			messages.add(connection.getUser() + ":");
            			messages.add(text);
            			setListAdapter();
            		}
                    else {
                    	Log.i("XMPPClient", "Connection lost");
                        Toast.makeText( getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
                    }                
                }                	
                else {
                	Log.i("XMPPClient", "Connection == null");
                    Toast.makeText( getApplicationContext(), "Connection == null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Called by Settings dialog when a try to connect with the XMPP server
     *
     * @param connection
     */
    public void createConnection( String host, int port, String service )
    {
    	this.host = host;
    	this.port = port;
    	this.service = service;
	    ConnectionConfiguration connConfig =
	            new ConnectionConfiguration(host, port, service);
	    this.connection = new XMPPConnection(connConfig);
    }
    /**
     * Called by Settings dialog when a try to connect with the XMPP server
     *
     * @param connection
     */
    public void connectConnection() {
    	if (connection != null) {
	        try {
	            connection.connect();            
	            Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
	        } catch (XMPPException ex) {
	            Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
	            Log.e("XMPPClient", ex.toString());
	            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
	            setConnection(null);
	        }
    	}
    	else {
            Toast.makeText(getApplicationContext(), "connection == null", Toast.LENGTH_SHORT).show();    		
    	}
    }    
    /**
     * Called by Settings dialog when a try to connect with the XMPP server
     *
     * @param connection
     */
    public void loginConnection
            (	String username,
            		String password) {
    	if (connection != null) {
            try {
            	if ( connection.isConnected()) {
		            connection.login(username, password);
		            Log.i("XMPPClient", "Logged in as " + connection.getUser());
		
		            // Set the status to available
		            Presence presence = new Presence(Presence.Type.available);
		            connection.sendPacket(presence);
		            setConnection(connection);
		            this.username = username;
		            this.password = password;		            
                }
            } catch (XMPPException ex) {
                Log.e("XMPPClient", "[SettingsDialog] Failed to log in as " + username);
                Log.e("XMPPClient", ex.toString());
                setConnection(null);
                Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            }
    	}
    	else {
            Toast.makeText(getApplicationContext(), "connection == null", Toast.LENGTH_SHORT).show();    		
    	}
    }
    /**
     * Called by Settings dialog when a connection is establised with the XMPP server
     *
     * @param connection
     */
    public void setConnection
            (XMPPConnection
                    connection) {
        this.connection = connection;
        if (connection != null) {
            // Add a packet listener to get messages sent to us
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(new PacketListener() {
                public void processPacket(Packet packet) {
                    Message message = (Message) packet;
                    if (message.getBody() != null) {
                        String fromName = StringUtils.parseBareAddress(message.getFrom());
                        Log.i("XMPPClient", "Got text [" + message.getBody() + "] from [" + fromName + "]");
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        // Add the incoming message to the list view
                        mHandler.post(new Runnable() {
                            public void run() {
                                setListAdapter();
                            }
                        });
                    }
                }
            }, filter);
        } else {
            Toast.makeText(getApplicationContext(), "Set connetion to null", Toast.LENGTH_SHORT).show();        	
        }
        
    }

    private void setListAdapter
            () {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.multi_line_list_item,
                messages);
        mList.setAdapter(adapter);
    }
}

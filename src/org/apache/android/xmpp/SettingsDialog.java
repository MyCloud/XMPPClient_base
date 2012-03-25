package org.apache.android.xmpp;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class SettingsDialog extends Dialog implements android.view.View.OnClickListener {
    private XMPPClient xmppClient;

    public SettingsDialog(XMPPClient xmppClient) {
        super(xmppClient);
        this.xmppClient = xmppClient;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.settings);
        getWindow().setFlags(4, 4);
        setTitle("XMPP Settings");
        //MDJ SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);        
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }

    public void onClick(View v) {
        String host = getText(R.id.host);
        String port = getText(R.id.port);
        String service = getText(R.id.service);
        String username = getText(R.id.userid);
        String password = getText(R.id.password);
        //CharSequence text = "Port empty";
        host = "gotomycloud.net";
        port = "5222";
        service = "gotomycloud.net";
        username = "menno";
        password = "Z0wyZ@ny";
        // validate input
        //Toast.makeText(this.xmppClient.getApplicationContext(), "Port wrong", Toast.LENGTH_SHORT).show();
        
        // Create a connection
        xmppClient.createConnection(host, Integer.parseInt(port), service);
        xmppClient.connectConnection();
        xmppClient.loginConnection(username, password);

        dismiss();
    }

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}

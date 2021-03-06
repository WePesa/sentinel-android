package com.samourai.sentinel.hd;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Base58;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class HD_Account {

    private DeterministicKey aKey = null;
    private String strLabel = null;
    private int	mAID;
    private boolean isArchived = false;

    private HD_Chain mReceive = null;
    private HD_Chain mChange = null;

    private String strXPUB = null;

    private NetworkParameters mParams = null;

    private HD_Account() { ; }

    public HD_Account(NetworkParameters params, String xpub, String label, int child) throws AddressFormatException {

        mParams = params;
        strLabel = label;
        mAID = child;

        // assign master key to account key
        aKey = createMasterPubKeyFromXPub(xpub);

        strXPUB = xpub;

        mReceive = new HD_Chain(mParams, aKey, true, 1);
        mChange = new HD_Chain(mParams, aKey, false, 1);

    }

    private DeterministicKey createMasterPubKeyFromXPub(String xpubstr) throws AddressFormatException {

        byte[] xpubBytes = Base58.decodeChecked(xpubstr);

        ByteBuffer bb = ByteBuffer.wrap(xpubBytes);
        if(bb.getInt() != 0x0488B21E)   {
            throw new AddressFormatException("invalid xpub version");
        }

        byte[] chain = new byte[32];
        byte[] pub = new byte[33];
        // depth:
        bb.get();
        // parent fingerprint:
        bb.getInt();
        // child no.
        bb.getInt();
        bb.get(chain);
        bb.get(pub);

        return HDKeyDerivation.createMasterPubKeyFromBytes(pub, chain);
    }

    public String xpubstr() {

        return strXPUB;

    }

    public int getId() {
        return mAID;
    }

    public HD_Chain getReceive() {
        return mReceive;
    }

    public HD_Chain getChange() {
        return mChange;
    }

    public HD_Chain getChain(int idx) {
        return (idx == 0) ? mReceive : mChange;
    }

    public int size() {
        return mReceive.length() + mChange.length();
    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("xpub", xpubstr());

            return obj;
        }
        catch(JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
}

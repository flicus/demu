/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 schors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.schors.demu.client.diameter;

import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Message;
import org.jdiameter.api.validation.AvpRepresentation;
import org.jdiameter.api.validation.Dictionary;
import org.jdiameter.common.impl.validation.DictionaryImpl;

public class Utils {

    public static Dictionary AVP_DICTIONARY = DictionaryImpl.INSTANCE;
    private static Logger logger = Logger.getLogger(Utils.class);

    public static void printMessage(Message message) {
        String reqFlag = message.isRequest() ? "R" : "A";
        String flags = reqFlag += message.isError() ? " | E" : "";

        if (logger.isInfoEnabled()) {
            logger.info("Message [" + flags + "] Command-Code: " + message.getCommandCode() + " / E2E("
                    + message.getEndToEndIdentifier() + ") / HbH(" + message.getHopByHopIdentifier() + ")");
            logger.info("- - - - - - - - - - - - - - - - AVPs - - - - - - - - - - - - - - - -");
            printAvps(message.getAvps());
        }
    }

    public static String requestType(int rtype) {
        switch (rtype) {
            case 1:
                return "INITIAL_REQUEST";
            case 2:
                return "UPDATE_REQUEST";
            case 3:
                return "TERMINATION_REQUEST";
            case 4:
                return "EVENT_REQUEST";
        }
        return "UNKNOWN";
    }


    public static JsonObject diameter2json(Message message) {
        JsonObject result = new JsonObject();
        result.put("session-id", message.getSessionId());
        result.put("command-code", message.getCommandCode());
        result.put("is-request", message.isRequest());
        if (!message.isRequest()) {
            try {
                result.put("is-ok", message.getAvps().getAvp(268).getInteger32() == 2001);
            } catch (AvpDataException e) {
                e.printStackTrace();
            }
        }
        result.put("avps", diameter2json(message.getAvps()));
        return result;
    }

    public static JsonObject diameter2json(AvpSet avps) {
        JsonObject result = new JsonObject();
        for (Avp avp : avps) {
            AvpRepresentation avpRep = AVP_DICTIONARY.getAvp(avp.getCode(), avp.getVendorId());
            String name = avpRep.getName();
            JsonObject obj = new JsonObject();
            try {
                switch (avpRep.getType()) {
                    case "Grouped":
                        obj.put("value", diameter2json(avp.getGrouped()));
                        break;
                    case "Integer32":
                        obj.put("value", avp.getInteger32());
                        break;
                    case "Unsigned32":
                        obj.put("value", avp.getUnsigned32());
                        break;
                    case "Float64":
                        obj.put("value", avp.getFloat64());
                        break;
                    case "Integer64":
                        obj.put("value", avp.getInteger64());
                        break;
                    case "Time":
                        obj.put("value", avp.getTime());
                        break;
                    case "Unsigned64":
                        obj.put("value", avp.getUnsigned64());
                        break;
                    default:
                        obj.put("value", avp.getUTF8String().replaceAll("\r", "").replaceAll("\n", ""));
                }
            }catch (Exception e) {
                logger.warn(e,e);
            }
            obj.put("type", avpRep.getType());
            result.put(name, obj);
        }
        return result;
    }

    public static JsonObject prepareForUI(Message message) {
        JsonObject result = new JsonObject()
                .put("rtype", message.isRequest() ? 1 : 2)
                .put("id", message.getSessionId());
        for (Avp avp : message.getAvps()) {
            try {
                if (avp.getCode() == 268) { //result code
                    long code = avp.getUnsigned32();
                    result.put("code", code);
                    result.put("success", code == 2001);
                }
                if (avp.getCode() == 416) { //request type
                    int code = avp.getInteger32();
                    result.put("mtype", requestType(code));
                }
            } catch (AvpDataException e) {
                logger.warn(e, e);
            }
        }
        return result;
    }

    public static void printAvps(AvpSet avps) {
        printAvps(avps, "");
    }

    public static void printAvps(AvpSet avps, String indentation) {
        for (Avp avp : avps) {
            AvpRepresentation avpRep = AVP_DICTIONARY.getAvp(avp.getCode(), avp.getVendorId());
            Object avpValue = null;
            boolean isGrouped = false;

            try {
                String avpType = AVP_DICTIONARY.getAvp(avp.getCode(), avp.getVendorId()).getType();

                if ("Integer32".equals(avpType) || "AppId".equals(avpType)) {
                    avpValue = avp.getInteger32();
                } else if ("Unsigned32".equals(avpType) || "VendorId".equals(avpType)) {
                    avpValue = avp.getUnsigned32();
                } else if ("Float64".equals(avpType)) {
                    avpValue = avp.getFloat64();
                } else if ("Integer64".equals(avpType)) {
                    avpValue = avp.getInteger64();
                } else if ("Time".equals(avpType)) {
                    avpValue = avp.getTime();
                } else if ("Unsigned64".equals(avpType)) {
                    avpValue = avp.getUnsigned64();
                } else if ("Grouped".equals(avpType)) {
                    avpValue = "<Grouped>";
                    isGrouped = true;
                } else {
                    avpValue = avp.getUTF8String().replaceAll("\r", "").replaceAll("\n", "");
                }
            } catch (Exception ignore) {
                try {
                    avpValue = avp.getUTF8String().replaceAll("\r", "").replaceAll("\n", "");
                } catch (AvpDataException e) {
                    avpValue = avp.toString();
                }
            }

            String avpLine = indentation + avp.getCode() + ": " + avpRep.getName();
            while (avpLine.length() < 50) {
                avpLine += avpLine.length() % 2 == 0 ? "." : " ";
            }
            avpLine += avpValue;

            logger.info(avpLine);

            if (isGrouped) {
                try {
                    printAvps(avp.getGrouped(), indentation + "  ");
                } catch (AvpDataException e) {
                    // Failed to ungroup... ignore then...
                }
            }
        }
    }
}

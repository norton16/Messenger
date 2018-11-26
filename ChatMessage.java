package chatapplication;
import java.io.Serializable;
/**
 * Project 04 -- Simple Chat Server
 *
 * This program allows for connection of a server and clients, and messaging.
 *
 * @authors Brian Norton, Briana Crowe lab sec 015
 *
 * @version November 26, 2018
 *
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private int messageType;
    private String message;
    private String recipient;

    public final static int broadcast = 0;
    public final static int logout = 1;
    public final static int dm = 2;
    public final static int list = 3;


    public ChatMessage(int messageType, String message)
    {
        this(messageType, message, null);
    }

    public ChatMessage(int messageType, String message, String recipient)
    {
        this.message = message;
        this.messageType = messageType;
        this.recipient = recipient;
    }

    public int getMessType()
    {
        return messageType;
    }
    public String getMessage()
    {
        return message;
    }
    public String getRecipient()
    {
        return recipient;
    }
}
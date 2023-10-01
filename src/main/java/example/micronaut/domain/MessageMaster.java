package example.micronaut.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class MessageMaster implements Serializable {

    public MessageMaster() {}

    public MessageMaster(String messageId, String title, String text) {
        super();
        this.messageId = messageId;
        this.title = title;
        this.text = text;
    }

    private static final long serialVersionUID = 1L;

    private String messageId;
    private String title;
    private String text;
}

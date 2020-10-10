package iot.ccnu.bean.eventModel;

public class DevicesChangedEvent {
    private String type;
    private String operation;
    private String message;

    public DevicesChangedEvent() {
    }

    public DevicesChangedEvent(String type, String operation, String message) {
        this.type = type;
        this.operation = operation;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

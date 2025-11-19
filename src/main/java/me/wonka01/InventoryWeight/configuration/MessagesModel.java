package me.wonka01.InventoryWeight.configuration;

public class MessagesModel {

    private String noPermission;
    private String invalidCommand;
    private String invalidMaterial;
    private String itemWeight;
    private String weight;
    private String speed;
    private String reloadCommand;
    private String helpMessage;
    private String cantMoveMessage;
    private String overLimitMessage;
    private String overWeightMessage;
    private String slowdownWarningMessage;
    private String preventJumpWarningMessage;

    // TODO - make this a map instead of an object
    public MessagesModel(String noPermission, String invalidCommand, String invalidMaterial, String itemWeight,
            String weight, String speed, String reloadCommand, String helpMessage, String cantMoveMessage,
            String overLimitMessage, String overWeightMessage, String slowdownWarningMessage,
            String preventJumpWarningMessage) {
        this.noPermission = noPermission;
        this.invalidCommand = invalidCommand;
        this.invalidMaterial = invalidMaterial;
        this.itemWeight = itemWeight;
        this.weight = weight;
        this.speed = speed;
        this.reloadCommand = reloadCommand;
        this.helpMessage = helpMessage;
        this.cantMoveMessage = cantMoveMessage;
        this.overLimitMessage = overLimitMessage;
        this.overWeightMessage = overWeightMessage;
        this.slowdownWarningMessage = slowdownWarningMessage;
        this.preventJumpWarningMessage = preventJumpWarningMessage;
    }

    public String getNoPermission() {
        return noPermission;
    }

    public String getInvalidCommand() {
        return invalidCommand;
    }

    public String getInvalidMaterial() {
        return invalidMaterial;
    }

    public String getItemWeight() {
        return itemWeight;
    }

    public String getWeight() {
        return weight;
    }

    public String getSpeed() {
        return speed;
    }

    public String getReloadCommand() {
        return reloadCommand;
    }

    public String getHelpMessage() {
        return helpMessage;
    }

    public String getCantMoveMessage() {
        return cantMoveMessage;
    }

    public String getOverLimitMessage() {
        return overLimitMessage;
    }

    public String getOverWeightMessage() {
        return overWeightMessage;
    }

    public String getSlowdownWarningMessage() {
        return slowdownWarningMessage;
    }

    public String getPreventJumpWarningMessage() {
        return preventJumpWarningMessage;
    }
}

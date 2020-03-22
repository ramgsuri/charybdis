package ma.markware.charybdis.model.option;

public enum ReplicationStrategyClassEnum {

  SIMPLESTRATEGY("SimpleStrategy"),

  NETWORKTOPOLOGYSTRATEGY("NetworkTopologyStrategy");

  private String value;

  ReplicationStrategyClassEnum(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

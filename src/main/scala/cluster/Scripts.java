package cluster;

public enum Scripts {
    INSTALL_MASTER("scripts/install_master.sh"),
    INSTALL_AGENT("scripts/install_agent.sh"),
    START_MULTIPLE_MASTER("scripts/scala_start_master.scala"),
    START_AGENT_MULTIPLE("scripts/scala_start_agent.scala"),
    START_MARATHON("scripts/scala_start_marathon.scala");
    private String path;

    Scripts(String path){
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }


}

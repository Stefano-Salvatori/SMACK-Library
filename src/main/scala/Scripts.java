public enum Scripts {
    INSTALL_MASTER("scripts/install_master.sh"),
    INSTALL_AGENT("scripts/install_agent.sh"),
    START_MASTER("scripts/start_master.sh"),
    START_AGENT("scripts/start_agent.sh"),
    TEST("scripts/test.sh");

    private String path;

    Scripts(String path){
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }


}

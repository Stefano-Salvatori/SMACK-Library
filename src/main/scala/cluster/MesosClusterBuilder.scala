package cluster


class MesosClusterBuilder() {
  private var name: String = ""
  private var agents: List[String] = List()
  private var masters: List[String] = List()
  private var user: String = ""
  private var sshKeyPath: String = ""
  private var sshKeyPassword: String = ""


  /**
    * Set the name of the cluster
    *
    * @param name the name to give to the cluster
    */
  def setClusterName(name: String): MesosClusterBuilder = {
    if (name.contains(" ")) throw new IllegalArgumentException("Invalid cluster name")
    else this.name = name
    this
  }

  /**
    * Set the list of machines that will become agents
    *
    * @param agents the list of ip addresses
    */
  def setAgents(agents: List[String]): MesosClusterBuilder = {
    this.agents = agents
    this
  }

  /**
    * Set the list of machines that will become masters.
    * (It's recommended to have an odd number of master nodes)
    *
    * @param masters the list of ip addresses
    */
  def setMasters(masters: List[String]): MesosClusterBuilder = {
    this.masters = masters
    this
  }

  /**
    * Set the ssh key that will be used to authenticate to every node in the cluster
    *
    * @param sshKeyPath     the path to the ssh key
    * @param sshKeyPassword the password for the given key
    */
  def setConnection(user: String, sshKeyPath: String, sshKeyPassword: String): MesosClusterBuilder = {
    this.user = user
    this.sshKeyPath = sshKeyPath
    this.sshKeyPassword = sshKeyPassword
    this
  }

  def build(): MesosCluster = new MesosCluster(this.name,
                                                this.masters.map(m => new Node(m, user, sshKeyPath,
                                                                                sshKeyPassword)),
                                                this.agents.map(a => new Node(a, user, sshKeyPath,
                                                                               sshKeyPassword)))


}

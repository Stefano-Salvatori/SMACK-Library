package cluster

case class ClusterConfigurations(clusterName: String,
                                 user: String,
                                 sshKeyPath: String,
                                 sshKeyPassword: String,
                                 masters: List[String],
                                 agents: List[String]) {
  if (clusterName.contains(" ")) throw new IllegalArgumentException("Invalid cluster name")
}
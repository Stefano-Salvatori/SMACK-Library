package task

import task.CassandraTask.CassandraVariable.CassandraVariable
import Container._

object CassandraTask {
  val CASSANDRA_CONTAINER =
    Container("DOCKER", DockerContainer("cassandra:latest", "HOST", true, true, Array(7199, 7000,
                                                                                       7001, 9160, 9042)))

  def apply(id: String, cpus: Double, mem: Double, disk: Double,
            cmd: Option[String], instances: Int = 1): CassandraTask =
    new CassandraTask(id, cpus, mem, disk, cmd, instances)

  object CassandraVariable extends Enumeration {
    type CassandraVariable = Value

    /**
      * This variable sets the name of the cluster and must be the same for all nodes in the
      * cluster. It will set the cluster_name option of cassandra.yaml.
      */
    val CASSANDRA_CLUSTER_NAME = Value("CASSANDRA_CLUSTER_NAME")

    /**
      * This variable is for controlling which IP address to listen for incoming connections on.
      * The default value is auto, which will set the listen_address option in cassandra.yaml to
      * the IP address of the container as it starts. This default should work in most use cases.
      */
    val CASSANDRA_LISTEN_ADDRESS = Value("CASSANDRA_LISTEN_ADDRESS")

    /**
      * This variable is for controlling which IP address to advertise to other nodes. The default
      * value is the value of CASSANDRA_LISTEN_ADDRESS. It will set the broadcast_address and
      * broadcast_rpc_address options in cassandra.yaml.
      */
    val CASSANDRA_BROADCAST_ADDRESS = Value("CASSANDRA_BROADCAST_ADDRESS")

    /**
      * This variable is for controlling which address to bind the thrift rpc server to. If you do
      * not specify an address, the wildcard address (0.0.0.0) will be used. It will set the
      * rpc_address option in cassandra.yaml.
      */
    val CASSANDRA_RPC_ADDRESS = Value("CASSANDRA_RPC_ADDRESS")

    /**
      * This variable is for controlling if the thrift rpc server is started. It will set the
      * start_rpc option in cassandra.yaml.
      */
    val CASSANDRA_START_RPC = Value("CASSANDRA_START_RPC")

    /**
      * This variable is the comma-separated list of IP addresses used by gossip for bootstrapping
      * new nodes joining a cluster. It will set the seeds value of the seed_provider option in
      * cassandra.yaml. The CASSANDRA_BROADCAST_ADDRESS will be added the the seeds passed in so
      * that the sever will talk to itself as well.
      */
    val CASSANDRA_SEEDS = Value("CASSANDRA_SEEDS")

    /**
      * This variable sets number of tokens for this node. It will set the num_tokens option of cassandra.yaml.
      */
    val CASSANDRA_NUM_TOKENS = Value("CASSANDRA_NUM_TOKENS")

    /**
      * This variable sets the datacenter name of this node. It will set the dc option of
      * cassandra-rackdc.properties. You must set CASSANDRA_ENDPOINT_SNITCH to use the
      * "GossipingPropertyFileSnitch" in order for Cassandra to apply cassandra-rackdc.properties,
      * otherwise this variable will have no effect.
      */
    val CASSANDRA_DC = Value("CASSANDRA_DC")

    /**
      * This variable sets the rack name of this node. It will set the rack option of
      * cassandra-rackdc.properties. You must set CASSANDRA_ENDPOINT_SNITCH to use the
      * "GossipingPropertyFileSnitch" in order for Cassandra to apply cassandra-rackdc.properties,
      * otherwise this variable will have no effect.
      */
    val CASSANDRA_RACK = Value("CASSANDRA_RACK")

    /**
      * This variable sets the snitch implementation this node will use. It will set the
      * endpoint_snitch option of cassandra.yml.
      */
    val CASSANDRA_ENDPOINT_SNITCH = Value("CASSANDRA_ENDPOINT_SNITCH")
  }

}

class CassandraTask(id: String, cpus: Double, mem: Double, disk: Double,
                    cmd: Option[String], instances: Int = 1)
  extends GenericTask(id, cpus, mem, disk, cmd, CassandraTask.CASSANDRA_CONTAINER, Map(),
                       instances) {

  def set(name: CassandraVariable, value: String) = {
    super.setEnvVariable(name.toString, value)
  }

}

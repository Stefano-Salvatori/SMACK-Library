package task

import task.KafkaTask.KafkaVariable.KafkaVariable

object KafkaTask {

  object KafkaVariable extends Enumeration {
    type KafkaVariable = Value

    val HOSTNAME_COMMAND = Value("HOSTNAME_COMMAND") //"ip route get 8.8.8.8 | awk '{print; exit}'",

    /**
      * Listeners to publish to ZooKeeper for clients to use, if different than the `listeners`
      * config property. In IaaS environments, this may need to be different from the interface
      * to which the broker binds. If this is not set, the value for `listeners` will be used.
      * Unlike `listeners` it is not valid to advertise the 0.0.0.0 meta-address.
      */
    val KAFKA_ADVERTISED_LISTENERS = Value("KAFKA_ADVERTISED_LISTENERS")

    /**
      * Listener List - Comma-separated list of URIs we will listen on and the listener names. If
      * the listener name is not a security protocol, listener.security.protocol.map must also
      * be set. Specify hostname as 0.0.0.0 to bind to all interfaces. Leave hostname empty to
      * bind to default interface. Examples of legal listener lists: PLAINTEXT://myhost:9092,
      * SSL://:9091 CLIENT://0.0.0.0:9092,REPLICATION://localhost:9093
      */
    val KAFKA_LISTENERS = Value("KAFKA_LISTENERS") //"1.2.3.4",

    /**
      * Zookeeper host string
      */
    val KAFKA_ZOOKEEPER_CONNECT = Value("KAFKA_ZOOKEEPER_CONNECT") //"1.2.3.4",

    /**
      * Map between listener names and security protocols. This must be defined for the same
      * security protocol to be usable in more than one port or IP. For example, internal and
      * external traffic can be separated even if SSL is required for both. Concretely, the user
      * could define listeners with names INTERNAL and EXTERNAL and this property as:
      * `INTERNAL:SSL,EXTERNAL:SSL`. As shown, key and value are separated by a colon and map
      * entries are separated by commas. Each listener name should only appear once in the map.
      * Different security (SSL and SASL) settings can be configured for each listener by adding
      * a normalised prefix (the listener name is lowercased) to the config name. For example,
      * to set a different keystore for the INTERNAL listener, a config with name `listener.name
      * .internal.ssl.keystore.location` would be set. If the config for the listener name is
      * not set, the config will fallback to the generic config (i.e. `ssl.keystore.location`).
      */
    val KAFKA_LISTENER_SECURITY_PROTOCOL_MAP = Value("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP")

    /**
      * Name of listener used for communication between brokers. If this is unset, the listener
      * name is defined by security.inter.broker.protocol. It is an error to set this and
      * security.inter.broker.protocol properties at the same time.
      */
    val KAFKA_INTER_BROKER_LISTENER_NAME = Value("KAFKA_INTER_BROKER_LISTENER_NAME")

  }

}

class KafkaTask(id: String,
                cpus: Double,
                mem: Double,
                disk: Double,
                cmd: Option[String],
                instances: Int = 1)
  extends GenericTask(id,cpus,mem,disk,cmd,Some(Container(docker = Some(DockerContainer("wurstmeister/kafka",
                                                                                          "HOST",
                                                                                          forcePullImage = true,
                                                                                          privileged = true,
                                                                                         Array
                                                                                         (9094,
                                                                                           9092,
                                                                                           2181)))
                                                         )),Map()) {


  def set(name: KafkaVariable, value: String) = {
    super.setEnvVariable(name.toString, value)
  }
}

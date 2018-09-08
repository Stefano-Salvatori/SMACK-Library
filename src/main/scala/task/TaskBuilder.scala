package task

import java.util.UUID

/**
  * Builder class for tasks
  */
class TaskBuilder {

  private var id: String = UUID.randomUUID().toString
  private var cpus: Double = 0.5
  private var mem: Double = 512.0
  private var disk: Double = 0.0
  private var cmd: Option[String] = None

  private var container: Option[Container] = None
  private var env: Map[String, String] = Map()
  private var instances: Int = 1


  def setId(name: String): TaskBuilder = {
    this.id = name
    this
  }

  def setCpus(cpus: Double): TaskBuilder = {
    this.cpus = cpus
    this
  }

  def setMemory(mem: Double): TaskBuilder = {
    this.mem = mem
    this
  }

  def setDisk(disk: Double): TaskBuilder = {
    this.disk = disk
    this
  }

  def setCmd(cmd: String): TaskBuilder = {
    this.cmd = Some(cmd)
    this
  }

  def SetContainer(container: Container): TaskBuilder = {
    this.container = Some(container)
    this
  }

  def setEnv(key: String, value: String): TaskBuilder = {
    this.env += key -> value
    this
  }

  def instances(instances: Int): TaskBuilder = {
    this.instances = instances
    this
  }

  def build(): GenericTask = {
    new GenericTask(this.id, this.cpus, this.mem, this.disk,
                     this.cmd, this.container, this.env, this.instances)
  }


}

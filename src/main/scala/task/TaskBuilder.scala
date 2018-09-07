package task

import java.util.UUID

/**
  * Builder class for tasks
  */
class TaskBuilder {

  private var _id: String = UUID.randomUUID().toString
  private var _cpus: Double = 1.0
  private var _mem: Double = 512.0
  private var _disk: Double = 0.0
  private var _cmd: Option[String] = None

  private var _container: Option[Container] = None

  private var _env: Map[String, String] = Map()

  private var _instances: Int = 1


  def id(name: String): TaskBuilder = {
    this._id = name
    this
  }

  def cpus(cpus: Double): TaskBuilder = {
    this._cpus = cpus
    this
  }

  def mem(mem: Double): TaskBuilder = {
    this._mem = mem
    this
  }

  def disk(disk: Double): TaskBuilder = {
    this._disk = disk
    this
  }

  def cmd(cmd: String): TaskBuilder = {
    this._cmd = Some(cmd)
    this
  }

  def container(container: Container): TaskBuilder = {
    this._container = Some(container)
    this
  }

  def setEnv(key: String, value: String): TaskBuilder = {
    this._env += key -> value
    this
  }

  def instances(instances: Int): TaskBuilder = {
    this._instances = instances
    this
  }

  def build(): GenericTask = {
    new GenericTask(this._id, this._cpus, this._mem, this._disk,
                     this._cmd, this._container, this._env, this._instances)
  }


}

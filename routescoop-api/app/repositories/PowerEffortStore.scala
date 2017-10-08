package repositories

import models.PowerEffort


trait PowerEffortStore {

  val PowerEffortsTable = "power_efforts"

  def destroy(): Unit

  def insert(effort: PowerEffort): Unit

  def insertBatch(efforts: Seq[PowerEffort]): Unit

  def findByActivityId(activityId: String): Seq[PowerEffort]

}

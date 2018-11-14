package utils

import anorm.{Column, MetaDataItem, TypeDoesNotMatch}

object AnormExtension {

  implicit val rowToBoolean: Column[Boolean] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case int: Int => Right(int != 0)
      case long: Long => Right(long != 0)
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Boolean for column $qualified"))
    }
  }

}

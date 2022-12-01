package org.mixql.core.context

import org.mixql.core.function.SqlLambda

package object gtype:
  import scala.language.implicitConversions

  /** implicit conversions from scala types to gtypes or gtypes to scala types
    */
  object implicitGtypeConversions:
    implicit def from_int(a: Int): int = int(a)

    implicit def to_int(a: int): Int = a.value

    implicit def from_double(a: Double): double = double(a)

    implicit def to_double(a: double): Double = a.value

    implicit def from_bool(a: Boolean): bool = bool(a)

    implicit def to_bool(a: bool): Boolean = a.value

    implicit def from_string(a: String): string = string(a)

    implicit def to_string(a: string): String = a.value

  /** conversions any gtype to gtype you need (if possible)
    */
  object typeConversion:
    import implicitGtypeConversions._

    def to_string(a: Type): string =
      string(a.toString)

    def to_bool(a: Type): bool =
      a match
        case value: bool   => value
        case double(value) => value != 0
        case int(value)    => value != 0
        case string(value, _) =>
          if value.toLowerCase == "true" then
            true
          else if value.toLowerCase == "false" then
            false
          else
            throw new ClassCastException(
              s"cannot convert string \"$value\" to bool"
            )
        case Null =>
          throw new NullPointerException("cannot convert null to bool")
        case value =>
          throw new ClassCastException(
            s"cannot convert ${value.getClass.getSimpleName} to bool"
          )

    def to_int(a: Type): int =
      a match
        case bool(value)      => if value then 1 else 0
        case double(value)    => int(value.toInt)
        case value: int       => value
        case string(value, _) => int(value.toInt)
        case Null =>
          throw new NullPointerException("cannot convert null to int")
        case value =>
          throw new ClassCastException(
            s"cannot convert ${value.getClass.getSimpleName} to int"
          )

    def to_double(a: Type): double =
      a match
        case bool(value)      => if value then 1 else 0
        case value: double    => value
        case int(value)       => value.toDouble
        case string(value, _) => value.toDouble
        case Null =>
          throw new NullPointerException("cannot convert null to double")
        case value =>
          throw new ClassCastException(
            s"cannot convert ${value.getClass.getSimpleName} to double"
          )

  def isNull(a: Type): Boolean =
    a match
      case Null => true
      case _    => false

  def pack(a: Any): Type =
    a match
      case null          => Null
      case p: String     => string(p)
      case p: Int        => int(p)
      case p: Double     => double(p)
      case p: Boolean    => bool(p)
      case p: Array[Any] => array(p.map(pack))
      case p: SqlLambda  => p
      case other         => string(other.toString)

  def unpack(a: Type): Any =
    a match
      case Null         => null
      case string(v, q) => v
      case int(v)       => v
      case double(v)    => v
      case bool(v)      => v
      case array(v)     => v.map(unpack)
      case v: SqlLambda => v

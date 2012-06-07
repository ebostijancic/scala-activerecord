package com.github.aselab.activerecord.experimental

import org.specs2.mutable._
import org.specs2.specification._

import com.github.aselab.activerecord._
import models._
import java.util.{Date, UUID}
import java.sql.Timestamp

object SerializationsSpec extends ActiveRecordSpecification {

  case class ListModel(l1: List[String], l2: List[Int]) extends ActiveRecord {
    def this() = this(List(""), List(0))
  }
  object ListModel extends ActiveRecordCompanion[ListModel]

  case class FormSupportModel(
    string: String,
    boolean: Boolean,
    int: Int,
    long: Long,
    float: Float,
    double: Double,
    bigDecimal: BigDecimal,
    timestamp: Timestamp,
    date: Date,
    uuid: UUID,
    ostring: Option[String],
    oboolean: Option[Boolean],
    oint: Option[Int],
    olong: Option[Long],
    ofloat: Option[Float],
    odouble: Option[Double],
    obigDecimal: Option[BigDecimal],
    otimestamp: Option[Timestamp],
    odate: Option[Date],
    ouuid: Option[UUID]
  ) extends ActiveRecord {
    def this() = this("", false, 0, 0, 0.toFloat, 0.0, BigDecimal(0),
      new Timestamp(0), new Date(0), new UUID(0, 0),
      Some(""), Some(false), Some(0), Some(0L), Some(0.toFloat), Some(0.0),
      Some(BigDecimal(0)), Some(new Timestamp(0)), Some(new Date(0)), Some(new UUID(0, 0))
    )
  }

  object FormSupportModel extends ActiveRecordCompanion[FormSupportModel] with FormSupport[FormSupportModel]


  "IO" should {
    "assgin" >> {
      val m = DummyModel.newModel(0)
      m.assign(Map(
        "boolean" -> true,
        "oboolean" -> true,
        "timestamp" -> new Timestamp(5L),
        "otimestamp" -> new Timestamp(5L),
        "float" -> 5.toFloat,
        "ofloat" -> 5.toFloat,
        "long" -> 5L,
        "olong" -> 5L,
        "string" -> "string5",
        "ostring" -> "string5",
        "bigDecimal" -> BigDecimal(5),
        "obigDecimal" -> BigDecimal(5),
        "double" -> 5.0,
        "odouble" -> 5.0,
        "date" -> new Date(5L * 1000 * 60 * 60 * 24),
        "odate" -> new Date(5L * 1000 * 60 * 60 * 24),
        "int" -> 5,
        "oint" -> 5,
        "uuid" -> new UUID(5L, 5L),
        "ouuid" -> new UUID(5L, 5L)
      ))
      m must equalTo(DummyModel.newModel(5))
    }

    "assignFormValues" >> {
      val m = DummyModel.newModel(0)
      m.assignFormValues(Map(
        "boolean" -> "true",
        "oboolean" -> "true",
        "timestamp" -> "1970-01-01T09:00:00.005+09:00",
        "otimestamp" -> "1970-01-01T09:00:00.005+09:00",
        "float" -> "5.0",
        "ofloat" -> "5.0",
        "long" -> "5",
        "olong" -> "5",
        "string" -> "string5",
        "ostring" -> "string5",
        "bigDecimal" -> "5",
        "obigDecimal" -> "5",
        "double" -> "5.0",
        "odouble" -> "5.0",
        "date" -> "1970-01-06T09:00:00.000+09:00",
        "odate" -> "1970-01-06T09:00:00.000+09:00",
        "int" -> "5",
        "oint" -> "5",
        "uuid" -> "00000000-0000-0005-0000-000000000005",
        "ouuid" -> "00000000-0000-0005-0000-000000000005"
      ))
      m must equalTo(DummyModel.newModel(5))
    }

    "assignFormValues(list)" >> {
      val m = ListModel.newInstance
      m.assignFormValues(Map(
        "l1[0]" -> "aa",
        "l1[1]" -> "bb",
        "l1[2]" -> "cc",
        "l2[0]" -> "11",
        "l2[1]" -> "22",
        "l2[2]" -> "33"
      ))
      m must equalTo(ListModel(List("aa", "bb", "cc"), List(11, 22, 33)))
    }

    "toMap" >> {
      val m = DummyModel.newModel(5)
      m.ofloat = None
      m.otimestamp = None
      m.odate = None
      m.ouuid = None

      m.toMap must equalTo(Map(
        "boolean" -> true,
        "oboolean" -> true,
        "timestamp" -> new Timestamp(5L),
        "float" -> 5.0,
        "long" -> 5L,
        "olong" -> 5L,
        "string" -> "string5",
        "ostring" -> "string5",
        "bigDecimal" -> 5,
        "obigDecimal" -> 5,
        "double" -> 5.0,
        "odouble" -> 5.0,
        "date" -> new Date(5L * 1000 * 60 * 60 * 24),
        "int" -> 5,
        "oint" -> 5,
        "uuid" -> new UUID(5L, 5L)
      ))
    }
 
    "toMap (relation)" >> {
      val g = Group("group1")
      val p = Project("project1")
      g.save
      p.save
      val id = g.id
      val u1 = User("user1")
      val u2 = User("user2")
      g.users.associate(u1)
      g.users.associate(u2)
      p.users.associate(u1)
      p.users.associate(u2)
      g.toMap must equalTo(Map(
        "name" -> "group1",
        "users" -> List(
          Map("name" -> "user1", "groupId" -> id),
          Map("name" -> "user2", "groupId" -> id)
        )
      ))
      g.users.map(_.toMap) must containAllOf(List(
        Map("name" -> "user1", "groupId" -> id, "group" -> Map("name" -> "group1"), "projects" -> List(Map("name" -> "project1"))),
        Map("name" -> "user2", "groupId" -> id, "group" -> Map("name" -> "group1"), "projects" -> List(Map("name" -> "project1")))
      ))

    }.pendingUntilFixed
  }

  "FormSupport" should {
    "bind" >> {
      FormSupportModel.bind(Map("string" -> "string", "ostring" -> "", "int" -> "100")) mustEqual
        new FormSupportModel().copy(string = "string", ostring = Some(""), int = 100)
    }
  }
}

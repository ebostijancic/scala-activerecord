package com.github.aselab.activerecord.scalatra

import com.github.aselab.activerecord.generator._

import sbt._
import sbt.complete.DefaultParsers._

object ControllerGenerator extends Generator[(String, Seq[Seq[String]])] {
  val name = "controller"

  def generate(args: (String, Seq[Seq[String]])) {
    val (name, actions) = args
    val controllerName = name.capitalize
    val target = sourceDir / "controllers" / (controllerName + ".scala")

    template(target, "controller/template.ssp", Map(
      ("packageName", "controllers"),
      ("controllerName", controllerName),
      ("actions", actions)
    ))
  }

  val help = "[controllerName] [action]*"

  val argumentsParser = (token(NotSpace, "controllerName") ~ actions)

  lazy val actions = (token(Space) ~> (path ~ action).map{
    case (x ~ y) => List(x, y)
  }).* <~ SpaceClass.*

  lazy val path = token(Field <~ token(':'), "path:action   e.g.) /index:get")
  lazy val action = token(Field).examples("get", "post", "update", "delete")
}


package eu.inn.hyperbus.rest.annotations

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

@compileTimeOnly("enable macro paradise to expand macro annotations")
class response(status: Int) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ResponseMacro.response
}

private[annotations] object ResponseMacro {
  def response(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val c0: c.type = c
    val bundle = new {
      val c: c0.type = c0
    } with ResponseAnnotationMacroImpl
    bundle.run(annottees)
  }
}

// todo: status annotation?
private[annotations] trait ResponseAnnotationMacroImpl extends AnnotationMacroImplBase {
  import c.universe._

  def updateClass(annotationArgument: Tree, existingClass: ClassDef, clzCompanion: Option[ModuleDef] = None): c.Expr[Any] = {

    val q"case class $className[..$typeArgs](..$fields) extends ..$bases { ..$body }" = existingClass

    val fieldsExcept = fields.filterNot { f ⇒
      f.name.toString == "correlationId" || f.name.toString == "messageId"
    }

    // eliminate contravariance
    val methodTypeArgs = typeArgs.map { t: TypeDef ⇒
      TypeDef(Modifiers(), t.name, t.tparams, t.rhs)
    }
    val classTypeNames = typeArgs.map { t: TypeDef ⇒
      t.name
    }

    val newClass = q"""
        case class $className[..$typeArgs](..$fieldsExcept,
          messageId: String,
          correlationId: Option[String]) extends ..$bases {
          ..$body
          def status: Int = $annotationArgument
        }
      """

    val companionExtra = q"""
        def apply[..$methodTypeArgs](..$fieldsExcept)
                 (implicit context: eu.inn.hyperbus.rest.MessagingContext): $className[..$classTypeNames] =
                 ${className.toTermName}[..$classTypeNames](
                    ..${fieldsExcept.map(_.name)},
                    messageId = eu.inn.hyperbus.utils.IdUtils.createId,
                    correlationId = context.correlationId
                 )

        def apply[..$methodTypeArgs](..$fieldsExcept, messageId: String)
                 (implicit context: eu.inn.hyperbus.rest.MessagingContext): $className[..$classTypeNames] =
                 ${className.toTermName}[..$classTypeNames](..${fieldsExcept.map(_.name)}, messageId = messageId, correlationId = context.correlationId)
        def status: Int = $annotationArgument
    """

    val newCompanion = clzCompanion map { existingCompanion =>
      val q"object $companion extends ..$bases { ..$body }" = existingCompanion
      q"""
          object $companion extends ..$bases {
            ..$body
            ..$companionExtra
          }
        """
    } getOrElse {
      q"""
        object ${className.toTermName} {
          ..$companionExtra
        }
      """
    }

    c.Expr(q"""
        $newClass
        $newCompanion
      """
    )
  }
}
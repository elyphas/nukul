package spatutorial.client

import cats.effect.IO
import monix.execution.Scheduler
import outwatch.dom
import outwatch.dom._
import outwatch.dom.dsl._
import outwatch.router._
import outwatch.router.dsl.C
import monix.reactive.Observable
import spatutorial.client.FrmDatosGrales
import monix.execution.Ack.Continue

sealed trait Page
final case object DatosGrales extends Page
final case object NotFound extends Page
final case object DetallesRequisicion extends Page
final case object Home extends Page

import spatutorial.client.{FrmDatosGrales}
import spatutorial.client.modules.FrmDetallesRequisiciones

object Menu {

  def router: AppRouter[Page] = AppRouter.create[Page](NotFound){
    case Root / "datosgenerales" => DatosGrales
    case Root / "detalles" => DetallesRequisicion
    case Root / "home" => Home
    case _ => NotFound
  }

  def pageContainer()(implicit S: Scheduler, router: RouterStore[Page]): IO[Observable[VDomModifier]] =
    IO(
      AppRouter.render[Page]{
        case DatosGrales => FrmDatosGrales.render
        case DetallesRequisicion =>  FrmDetallesRequisiciones.render
        case Home =>
         div( clear.both,
           h3( "Inicio" ),
           p( "mmmmmmmmmmmmmmmmm" )
         )
        case NotFound => div( )
      }
    )

  val colorMenu = "#f7927c"
  val azulito = "#94f4e8"

  val styleMenu: List[VDomModifier] = List( display.inline, display.inline, float.left, padding := "5px",
    backgroundColor := colorMenu, border := "1px solid", borderColor := "#919393" )

  val mouseOver = Handler.create[String]("" ).unsafeRunSync( )

  def itemMenu(title: String, path: String)(implicit routerStore: RouterStore[Page]) = {
    li( styleMenu,
      onMouseOver.map( r => title ) --> mouseOver,
      onMouseOut.map(r => "") --> mouseOver,
      backgroundColor <-- mouseOver.map ( r => if ( r == title ) azulito else colorMenu ),
      C.a[Page]( "/" + path )( title )
    )
  }

  def render()(implicit scheduler: Scheduler, router: RouterStore[Page]): VDomModifier =
    pageContainer().map { pc =>
      div( cls := "ui two column grid",
        div( width := "1000px", //cls := "four wide column",
          ul(
            itemMenu("Home","home"),
            itemMenu("Datos Generales","datosgenerales"),
            itemMenu("Detalles","detalles")
          ),
        ),
        div( cls := "twelve wide fluid column",
          pc
        )
      )
    }
}

object HelloWoutWatch {
  import monix.execution.Scheduler.Implicits.global
  def main(args: Array[String]): Unit = {

    val program = for {
      implicit0(exRouterStore: RouterStore[Page]) <- Menu.router.store
      program <- OutWatch.renderInto("#root", div( Menu.render ) )

    } yield program

    program.unsafeRunSync()

  }

}

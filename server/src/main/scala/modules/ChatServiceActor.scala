package modules

import akka.actor.{Actor, ActorRef, PoisonPill, Props}

import spatutorial.shared.CRUDViewRenglonRequisicion._
import spatutorial.shared.CRUDDatosGralesRequisicion._

import spatutorial.shared.RenglonRequisicion

import scala.concurrent.ExecutionContext
import postg.{ CRenglonRequisicion, CDatosGralesRequisicion }

object ChatServiceActor {
  def props(out: ActorRef) = Props( new ChatServiceActor( out ) )
}

class ChatServiceActor (out: ActorRef) extends Actor {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def receive = {
    case msg: CRUDViewRenglonRequisicion =>
      val renglonRequisicion: CRenglonRequisicion = new CRenglonRequisicion()
      msg match {
        case save: SaveViewRenglonRequisicion =>

          val renglon = RenglonRequisicion( renglon = save.item.renglon,
                                            clave = save.item.clave,
                                            cantidad = save.item.cantidad.getOrElse(0),
                                            precio = save.item.precio
                                          )
          renglonRequisicion.insertWS( renglon )
      }

    case msg: CRUDDatosGralesRequisicion =>
      val datosGralesRequisicion: CDatosGralesRequisicion = new CDatosGralesRequisicion()
      msg match {
        case save: SaveDatosGralesRequisicion =>
          datosGralesRequisicion.insert( save.item )
      }
  }

  override def postStop() {
    println( "Closing the websocket connection changos quien sabe por que!!!!!!!!!!" )
  }
}
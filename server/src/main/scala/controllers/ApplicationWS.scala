package controllers

import com.google.inject.Inject
import play.api.{Configuration, Environment}
import play.api.mvc._
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import modules._
import boopickle.Default._
import akka.stream.scaladsl.Flow

import scala.util.{Failure, Success}
import akka.util.ByteString
import spatutorial.shared.CRUDDatosGralesRequisicion.CRUDDatosGralesRequisicion

//import spatutorial.shared.WebSocketMessages.{ WebSocketMsgIn, WebSocketMsgOut }
import spatutorial.shared.CRUDViewRenglonRequisicion._
import spatutorial.shared.CRUDDatosGralesRequisicion._

import play.api.http.websocket.{BinaryMessage, CloseCodes, CloseMessage, Message}
import play.api.libs.streams.{ActorFlow, AkkaStreams}
import play.api.mvc.WebSocket.MessageFlowTransformer

class ApplicationWS @Inject() ( implicit val config: Configuration, val env: Environment,
                                implicit val system: ActorSystem,materializer: ActorMaterializer) {

  implicit val webSocketTransformer = new MessageFlowTransformer[CRUDViewRenglonRequisicion, CRUDViewRenglonRequisicion] {

    override def transform(flow: Flow[CRUDViewRenglonRequisicion, CRUDViewRenglonRequisicion, _]): Flow[Message, Message, _] = {

      AkkaStreams.bypassWith[ Message, CRUDViewRenglonRequisicion, Message ]( Flow[ Message ] collect {
        case BinaryMessage( data ) =>
          Unpickle[ CRUDViewRenglonRequisicion ].tryFromBytes( data.asByteBuffer ) match {
              case Success( msg ) =>
                Left( msg )
              case Failure( err ) =>
                Right( CloseMessage( CloseCodes.Unacceptable, s"Error with transfer: $err" ) )
        }
        case _ =>
          Right( CloseMessage( CloseCodes.Unacceptable, "This WebSocket only accepts binary." ) )
      })( flow.map { msg =>
        val bytes = ByteString.fromByteBuffer( Pickle.intoBytes( msg ) )
        BinaryMessage( bytes )
      })
    }
  }


  implicit val webSocketTransformerDatosGralesRequisicion = new MessageFlowTransformer[CRUDDatosGralesRequisicion, CRUDDatosGralesRequisicion] {

    override def transform(flow: Flow[CRUDDatosGralesRequisicion, CRUDDatosGralesRequisicion, _]): Flow[Message, Message, _] = {

      AkkaStreams.bypassWith[ Message, CRUDDatosGralesRequisicion, Message ]( Flow[ Message ] collect {
        case BinaryMessage( data ) =>
          Unpickle[ CRUDDatosGralesRequisicion].tryFromBytes( data.asByteBuffer ) match {
            case Success( msg ) =>
              Left( msg )
            case Failure( err ) =>
              Right( CloseMessage( CloseCodes.Unacceptable, s"Error with transfer: $err" ) )
          }
        case _ =>
          Right( CloseMessage( CloseCodes.Unacceptable, "This WebSocket only accepts binary." ) )
      })( flow.map { msg =>
        val bytes = ByteString.fromByteBuffer( Pickle.intoBytes( msg ) )
        BinaryMessage( bytes )
      })
    }
  }


  def socket = WebSocket.accept[ CRUDViewRenglonRequisicion, CRUDViewRenglonRequisicion ] { request =>
    ActorFlow.actorRef { out =>
        ChatServiceActor.props( out )
    }
  }

  def socketDatosGralesRequisicion = WebSocket.accept[CRUDDatosGralesRequisicion, CRUDDatosGralesRequisicion] { request =>
      ActorFlow.actorRef { out =>
        ChatServiceActor.props( out )
      }
  }

}
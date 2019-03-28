package spatutorial.services

import monix.eval.Task
import monix.execution.Ack
import monix.execution.Scheduler.Implicits.global
import outwatch.dom._
import outwatch.dom.dsl._
import spatutorial.client.modules.StoreApp._
import spatutorial.client.components.GridEditable._
import outwatch.util.{Store, WebSocket}
import spatutorial.client.modules._
import monix.execution.Ack._
import org.scalajs.dom
import outwatch.util.WebSocket
import monix.reactive.subjects.PublishSubject
import monix.reactive.Observer
import org.scalajs.dom.{CloseEvent, Event, MessageEvent}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future
import boopickle.Default._

//import spatutorial.shared.WebSocketMessages._
import spatutorial.shared.CRUDViewRenglonRequisicion._
import spatutorial.shared.{ ViewRenglonRequisicion, IdRequisicion }

import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import org.scalajs.dom
import org.scalajs.dom._
import java.nio._

import scala.util.{Failure, Success}

object WSService {

  lazy val chat = WebSocket("ws://localhost:9000/wsdatosgralesrequisicion")

  val blobReader: FileReader = {

    val reader = new FileReader( )
    reader.onerror = (e: Event) => {
      dom.console.log(s"Error in blobReader: ${reader.error}")
    }

    reader.onload = (e: UIEvent) => {
      reader.result match {
        case buf: ArrayBuffer =>
            val intP = dom.document.getElementById("int-value")
            /*Unpickle[WebSocketMsgOut].fromBytes(TypedArrayBuffer.wrap(buf)) match {
                case IntMsgOut(int) =>  intP.innerHTML = int.toString
                case ErrorMsgOut(msg) =>  intP.innerHTML = s"ERROR: $msg"
            }*/
        case _ => // ignored
      }
    }

    reader

  }

  val closeWS = Sink.create[ String ] { msg =>
    chat.ws.close()
    Continue
  }

  val sendMsgtoWS = Sink.create[ String ] { msg =>

    chat.ws.binaryType = "arraybuffer"

    val bytes = Pickle.intoBytes[ CRUDViewRenglonRequisicion ](
      SaveViewRenglonRequisicion(
        item = ViewRenglonRequisicion(
          renglon = 5,
          clave = "033.604.2762",
          descripcion = "probar las conversiones y el crud a traves websocket",
          cantidad = Some(555),
          precio = 122.5
        ),
        id =  IdRequisicion( )
      )
    ).arrayBuffer( )

    chat.ws.send( bytes )

    chat.ws.close() //Esto se va ha manejar en el regreso, por que se debe cerrar cuando se tenga la respuesta si se guardo bien o no.

    Continue

  }

  val catchEvents = new Observer[MessageEvent] {
    def onNext( elem: MessageEvent ): Future[Ack] = {
      println( s"O-->$elem" )
      elem.data match {
        case buf: ArrayBuffer =>
          Unpickle[ CRUDViewRenglonRequisicion ].fromBytes( TypedArrayBuffer.wrap( buf ) ) match {
            //case IntMsgOut( int ) => println( "Convirtiendo bien chingon esto" + int.toString )
            //case ErrorMsgOut( msg ) => println( "Hubo un error al convertirse : " + msg )
            case ver => println( ver )
          }
        case blob: Blob =>  blobReader.readAsArrayBuffer( blob )
        case _ => dom.console.log("Error on receive, should be a blob." )
      }
      Continue
    }

    def onError(ex: Throwable): Unit = ex.printStackTrace( )
    def onComplete(): Unit = println( "O completed" )
  }

  chat.observable.subscribe(catchEvents)

  chat.observable.map { v =>
    println(v.data)
  }

}
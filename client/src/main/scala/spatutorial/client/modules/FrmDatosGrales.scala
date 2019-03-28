package spatutorial.client

import monix.eval.Task
import monix.execution.Ack
import monix.execution.Scheduler.Implicits.global
import cats.data.ValidatedNel
import outwatch.dom._
import outwatch.dom.dsl._
import spatutorial.client.modules.StoreApp._
import spatutorial.shared.{Api, Father}
//import spatutorial.client.components.GridEditable._
import outwatch.util.{Store, WebSocket}
import spatutorial.client.modules._
import monix.execution.Ack._
import org.scalajs.dom
import monix.reactive.subjects.PublishSubject
import monix.reactive.Observer
import org.scalajs.dom.{ CloseEvent, Event, MessageEvent }
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Future
import boopickle.Default._

import spatutorial.shared.CRUDDatosGralesRequisicion._
import spatutorial.shared.{ Fuente, Programa }

import spatutorial.shared.{ ViewRenglonRequisicion, IdRequisicion }
import scala.scalajs.js.typedarray.{ ArrayBuffer, TypedArrayBuffer }

import scala.scalajs.js.typedarray.TypedArrayBufferOps._

import scala.util.{Failure, Success}

import spatutorial.services.WSService._
import org.scalajs.dom.ext.KeyCode
import spatutorial.shared.DatosGralesRequisicion
import spatutorial.shared.Fechas

import monix.reactive.Observable
import autowire._
import spatutorial.client.services.AjaxClient
import spatutorial.client.components.Components._

import spatutorial.client.Page

import spatutorial.client.validates.DatosGralesCreationError

object FrmDatosGrales {

  def render = {

    val datosGralesRequi = DatosGralesRequisicion (
          cve_oficina = "1221",
          fecha = Fechas("01/01/2019"),
          ejercicio = 2019,
          ejercicio_presupuestal = 2019
      )

    val handlerDatosGrales = Handler.create[DatosGralesRequisicion](datosGralesRequi).unsafeRunSync() //PublishSubject[DatosGralesRequisicion]()
    handlerDatosGrales.onNext ( datosGralesRequi )

    val txtCveOficina = handlerDatosGrales.lens[String]( datosGralesRequi )( _.cve_oficina )((datosGrales, cve_oficina) => datosGrales.copy(cve_oficina = cve_oficina))
    val cancelableCveOficina = txtCveOficina.connect() // need to subscribe to this handler, because it internally needs to track the current state.

    val txtFolio = handlerDatosGrales.lens[Int]( datosGralesRequi )( _.folio )((datosGrales, folio) => datosGrales.copy(folio = folio))
    val cancelableFolio = txtFolio.connect()

    val txtEjercicio = handlerDatosGrales.lens[Int]( datosGralesRequi )( _.ejercicio )((datosGrales, ejercicio) => datosGrales.copy(ejercicio = ejercicio))
    val cancelableEjercicio = txtEjercicio.connect()

    val txtFecha = handlerDatosGrales.lens[Fechas]( datosGralesRequi )( _.fecha )((datosGrales, fecha) => datosGrales.copy(fecha = fecha))
    val cancelableFecha = txtFecha.connect()

    val txtEjercicioPresup = handlerDatosGrales.lens[Int]( datosGralesRequi )( _.ejercicio_presupuestal )((datosGrales, ejercicio_presupuestal) => datosGrales.copy(ejercicio_presupuestal = ejercicio_presupuestal))
    val cancelableEjercicioPresup = txtEjercicioPresup.connect()

    val txtIDFuente = handlerDatosGrales.lens[String]( datosGralesRequi )( _.idfuente )((datosGrales, idfuente) => datosGrales.copy(idfuente = idfuente))
    val cancelableIDFuente = txtIDFuente.connect()

    val txtFuente = handlerDatosGrales.lens[String]( datosGralesRequi )( _.fuente_financiamiento )((datosGrales, fuente_financiamiento) => datosGrales.copy(fuente_financiamiento = fuente_financiamiento))
    val cancelableFuente = txtFuente.connect()

    val txtPrograma = handlerDatosGrales.lens[String](datosGralesRequi)(_.programa )((datosGrales, programa) => datosGrales.copy(programa = programa))
    val cancelableprograma = txtPrograma.connect()

    val txtDescripPrograma = handlerDatosGrales.lens[String](datosGralesRequi)(_.descripcionprograma )((datosGrales, descripcionprograma) => datosGrales.copy(descripcionprograma = descripcionprograma))
    val cancelabledescripcionprograma = txtDescripPrograma.connect()

    val txtDestino = handlerDatosGrales.lens[String](datosGralesRequi)(_.destino )((datosGrales, destino) => datosGrales.copy(destino = destino))
    val cancelableDestino = txtDestino.connect()

    val txtObservacion = handlerDatosGrales.lens[String](datosGralesRequi)(_.observaciones )((datosGrales, observaciones) => datosGrales.copy(observaciones = observaciones))
    val cancelableobservaciones = txtObservacion.connect()

    //val sendProveedortoWS = Sink.create[DatosGralesRequisicion] { datos =>
    val sendProveedortoWS = Sink.create[ValidatedNel[DatosGralesCreationError, DatosGralesRequisicion]] { datos =>
      datos match {
        case x: DatosGralesCreationError =>
          println("Hubo errores")
        case y: DatosGralesRequisicion =>
          //val bytes = Pickle.intoBytes[CRUDDatosGralesRequisicion](SaveDatosGralesRequisicion(datos)).arrayBuffer()
          val bytes = Pickle.intoBytes[CRUDDatosGralesRequisicion](SaveDatosGralesRequisicion(y)).arrayBuffer()
          chat.ws.send( bytes )
      }
      Continue
    }

    val onSearchFuente = PublishSubject[String]()

    val currentFuentesFinanc = onSearchFuente.switchMap { descripcion =>
      if (descripcion.length > 4 )  Observable.fromFuture(AjaxClient[Api].searchFuente(descripcion).call())
      else  Observable.empty
    }.share

    currentFuentesFinanc.map { items =>
      UpdateResultsCatalog( items )
    }.subscribe(store)

    val onSearchPrograma = PublishSubject[String]()

    val currentPrograma = onSearchPrograma.switchMap { descripcion =>
      if (descripcion.length > 4 )  Observable.fromFuture( AjaxClient[Api].searchPrograma(descripcion).call() )
      else  Observable.empty
    }.share

    currentPrograma.map { items => UpdateResultsCatalog(items) }.subscribe( store )

    val onEnter = onKeyUp.filter{ k => k.keyCode == KeyCode.Enter }

    def whatCatalog(i: Father) = i match {
      case p: Programa => Some("Programa")
      case f: Fuente =>  Some("Fuente")
      case _ => None
    }

    val onClickItem = Sink.create[ Father ]{ i =>
      val descrip = i.descripcion.getOrElse("")
      val id = i.id

      whatCatalog(i).getOrElse("") match {
        case "Programa" =>
          txtPrograma.onNext(id)
          txtDescripPrograma.onNext(descrip)
        case "Fuente" =>
          txtIDFuente.onNext(id)
          txtFuente.onNext(descrip)
      }
      store.onNext(CleanResultsCatalog)
      Continue
    }

    //def gridSearchResultCatalog(s: NukulState, intTop: Int): Option[VNode] =
    def gridSearchResultCatalog(s: Seq[Father], intTop: Int): Option[VNode] =
        if (s.nonEmpty) Some (
          div( left:="10px", top := intTop.toString + "px",
            position.absolute, zIndex := 1000, backgroundColor := "#f99d89",
            border := "1px solid", width := "500px",
            s.map { i =>
              div( span(i.id, fontWeight.bold), " : " + i.descripcion.getOrElse(""),  border := "1px solid",
                onClick( s.filter(p => p.id == i.id).map( r => r ).head ) --> onClickItem
              )
            }
          )
        ) else None

    for {
      s <- store
    } yield {

        val catalog = if (s._2.lstResultsCatalog.nonEmpty) whatCatalog(s._2.lstResultsCatalog.head) else None

        div ( clear.both,
          div (
            div( width :="3000px",
              cmpInput("Cve. Oficina", txtCveOficina, 30),
              cmpInputInt("Folio", txtFolio, 30),
              cmpInputFechas("Fecha", txtFecha, 60),
            ),
            cmpInputInt("Ejercicio", txtEjercicio, 30),
            cmpInputInt("Ejercicio Pres.", txtEjercicioPresup, 30),
            cmpInput("Id Fuente", txtIDFuente, 60),
            div(
              cmpInputSearch("Fuente Fin.", txtFuente, 60 ),
              catalog.filter( c => c == "Fuente" ).map( r => gridSearchResultCatalog( s._2.lstResultsCatalog, 170))
            ),
            div( width:="350px",
              label( "Fuente Financiamiento" ),
              input (
                onInput.target.value --> onSearchFuente,
                onInput.target.value --> txtFuente,
                value <-- txtFuente.map( r => r ),
              ),
              catalog.filter( c => c == "Fuente" ).map( r => gridSearchResultCatalog( s._2.lstResultsCatalog, 170))
            ),
            cmpInput("Programa", txtPrograma, 350),

            div( width := "350px",
              label( "Descripcion Programa" ),
              input(
                onInput.target.value --> onSearchPrograma,
                onInput.target.value --> txtDescripPrograma,
                value <-- txtDescripPrograma.map( r => r ),
              ),
              catalog.filter( c => c == "Programa" ).map( r => gridSearchResultCatalog( s._2.lstResultsCatalog, 260))
            ),
            cmpInput("Destino", txtDestino, 350),
            cmpInput("Observación", txtObservacion, 350),
          ),
          div( padding:="10px",
            button( "Guardar", onClick(
              handlerDatosGrales.map { r =>
                //First we need to validate
                DatosGralesCreationError.createDatosGrales(
                  r.cve_oficina,
                  r.folio,
                  r.ejercicio,
                  r.fecha.fecha,
                  r.ejercicio_presupuestal,
                  r.idfuente,
                  r.fuente_financiamiento,
                  r.programa,
                  r.descripcionprograma,
                  r.destino
                )
            } ) --> sendProveedortoWS ),
            button( "Cerrar", onClick( "close" ) --> closeWS )
          )
      )
    }
  }
}


/*div( width:="250px",
  label( "Ejercicio" ),
  input(
    width := "55px",
    value <-- txtEjercicio.map( r => r.toString ),
    onChange.target.value.map( r => r.toInt ) --> txtEjercicio
  ),
),
div( width:="250px",
  label("Ejercicio Presup." ),
  input(
    width := "55px",
    value <-- txtEjercicioPresup.map( r => r.toString ),
    onChange.target.value.map( r => r.toInt ) --> txtEjercicioPresup ),
),*/
/*div( width:="350px",
  label( "ID Fuente F." ),
  input(
    onChange.target.value.map( r => r ) --> txtIDFuente,
    value <-- txtIDFuente.map( r => r )
  ),
),*/


/*div( width := "350px",
              label("Programa" ),
              input(
                  onChange.target.value.map( r => r ) --> txtPrograma,
                  value <-- txtPrograma.map( r => r )
              ),
            ),

div( width:="350px",
              label("Destino" ),
              input(
                onChange.target.value.map( r => r ) --> txtDestino
              ),
            ),

div( width:="350px",
              label( "Observación" ),
              input(
                onChange.target.value.map( r => r ) --> txtObservacion
              )
            )
*/
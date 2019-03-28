package services

import akka.actor
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}

import scala.language.postfixOps
import spatutorial.shared._

import postg.{CUsuarios, CViewRenglonRequisicion, CRenglonRequisicion}
import javax.inject.Inject
import play.api.libs.ws._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import services.JsonEitherSpec.Data.FailureJson
import spray.json._
import services.JsonSupport

import scala.concurrent.ExecutionContext.Implicits.global

///Para el web service
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import HttpMethods._

import scala.concurrent.Future

class ApiService @Inject()( protected val ws: WSClient) extends Api with JsonSupport {

  import services.jsontypes.JsonTypes._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  //Request-level
  import MediaTypes._
  import spray.json._

  private val url = "http://localhost:8080/"

  /*************************  Oficinas    **********************************************************/
  override def getAllOficinas(): Future[Seq[Oficina]] = ws.url(url + "oficinas/getAll").get()
    .map {response => Json.parse(response.body).as[Seq[Oficina]]}

  override def getOficina(id: String): Future[Oficina] = ws.url(url + "oficinas/byID")
    .post( Json.obj("id" -> id))
    .map{ response => Json.parse(response.body).as[Oficina] }
  /**################################################################################################*/

  /************************* Usuarios   **********************************************/
  override def getUsuario(user: String): Future[Seq[Usuarios]] = {
    val usuario = new CUsuarios()
    usuario.ById(user)
  }
  override def logear(item: Usuarios): Future[(Usuarios, String)] = {
    val usuario = new CUsuarios()
    usuario.logear(item)
  }
  /**################################################################################################*/

  /**************************  Proveedores   *****************************************************/
  /*override def proveedorSearchID(str: String): Future[Proveedor] = {
    ws.url(url + "proveedores/searchID")
      .post(Json.obj("search" -> str ))
      .map { response =>
        Json.parse(response.body).as[Proveedor]
      }
  }*/

  import services.JsonEitherSpec.Data._

  override def searchIDProveedor(id: String): Future[Either[String,Proveedor]] = {
    val rfc = JsObject("search" -> JsString(id))
    val responseFuture: Future [ HttpResponse ] =
        Http().singleRequest (
          HttpRequest ( POST, uri = url + "proveedores/searchID", entity = HttpEntity ( `application/json`, rfc.toString ) )
        )
        responseFuture.flatMap { response =>
          Unmarshal(response).to[Either[FailureJson, SuccessJson[Proveedor]]]
        }.map {
          case Right(SuccessJson(prov)) => Right(prov)
          case Left(FailureJson(error)) => Left(error)
      }
  }

  override def searchProveedor(descripcion: String): Future[Seq[Proveedor]] = {
    val razonSocial = JsObject( "search" -> JsString( descripcion ))
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(
        HttpRequest(POST, uri = url + "proveedores/searchDescripcion", entity = HttpEntity(`application/json`, razonSocial.toString))
      )
    responseFuture.flatMap { response => Unmarshal(response).to[ Seq[ Proveedor ]] }.map( r => r )
  }

  override def saveProveedor( item: Proveedor ): Future[Either[String, Proveedor]] = {
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(
        HttpRequest(POST, uri = url + "proveedores/save", entity = HttpEntity(`application/json`, item.toJson.toString()))
      )
    responseFuture.flatMap { response =>
      Unmarshal(response).to[Either[FailureJson, SuccessJson[Proveedor]]]
    }.map {
      case Right( SuccessJson( prov ) ) => Right( prov )
      case Left( FailureJson(error)) => Left( error )
    }
  }
  /**######################################################################################################################*/


  /**************************  Articulos   *****************************************************/
  override def getAllArticulos(): Future[Seq[Articulo]] = ws.url(url + "articulos/getAll").get()
    .map{response => Json.parse(response.body).as[Seq[Articulo]]}

  override def getArticulo(id: String): Future[Option[Articulo]] = ws.url(url + "articulos/byID")
    .post(Json.obj("id" -> id))
    .map { response =>
      //Json.parse(response.body).as[Articulo]
      if (response.body.isEmpty)
        None
      else
        Json.parse(response.body).asOpt[Articulo]
    }

  override def searchArticulo(str: String): Future[Option[Seq[Articulo]]] = ws.url(url + "articulos/searchDescripcion")
    .post(Json.obj("search" -> str))
    .map { response =>
      //Json.parse(response.body).as[Seq[Articulo] ]
      if (response.body.isEmpty) None
      else Json.parse(response.body).asOpt[Seq[Articulo]]
    }

  override def searchID(str: String): Future[Seq[Articulo]] = {
    ws.url(url + "articulos/searchID")
      .post(Json.obj("search" -> str ))
      .map { response => Json.parse(response.body).as[Seq[Articulo]] }
  }
  /**################################################################################################*/

  /*********    Programas     ******************************************************************/
  override def getPrograma(id: String): Future[Programa] = ws.url(url + "programas/byID")
    .post(Json.obj("id" -> id))
    .map { response => Json.parse(response.body).as[Programa] }

  override def getAllPrograma(): Future[Seq[Programa]] = ws.url(url + "programas/getAll").get()
    .map { response => Json.parse(response.body).as[Seq[Programa]] }

  override def searchPrograma( descripcion: String ): Future[Seq[Programa]] = {
    val descripJson = JsObject( "search" -> JsString( descripcion ))

    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(
        HttpRequest(POST, uri = url + "programas/searchDescripcion", entity = HttpEntity(`application/json`, descripJson.toString))
      )

    responseFuture.flatMap { response =>
      Unmarshal(response).to[Seq[Programa]]
    }.map( r => r )

  }

  /********************************************************************************************************************/

  /*********    Fuente     ********************************************************************************************/
  override def getFuente(id: String): Future[Fuente] = ws.url(url + "fuentes/byID")
    .post(Json.obj("id" -> id))
    .map { response =>
      Json.parse(response.body).as[Fuente]
    }

  override def getAllFuente(): Future[Seq[Fuente]] = ws.url( url + "fuentes/getAll").get()
    .map { response => Json.parse(response.body).as[Seq[Fuente]] }

  override def searchFuente( descripcion: String ): Future[Seq[Fuente]] = {
    val descripJson = JsObject( "search" -> JsString( descripcion ))

    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(
        HttpRequest(POST, uri = url + "fuentes/searchDescripcion", entity = HttpEntity(`application/json`, descripJson.toString))
      )
    responseFuture.flatMap { response =>
      Unmarshal(response).to[Seq[Fuente]]
    }.map( r => r )




  }

  /********************************************************************************************************************/


  ///*********    Partida     ********************************
  override def getPartidas(): Future[Seq[Partida]] = ws.url( url + "partida/getAll").get()
    .map { response => Json.parse(response.body).as[Seq[Partida]] }
  override def getPartida(id: String): Future[Partida] = ws.url( url + "partida/byID")
    .post(Json.obj("id" -> id)).map { response => Json.parse(response.body).as[Partida] }
  /*****************************************************************************************************/


  /*****************************************************    Presentaciones     ********************************/
  override def getAllPresentaciones(): Future[Seq[Presentacion]] = ws.url( url + "presentacion/getAll").get()
    .map { response => Json.parse(response.body).as[Seq[Presentacion]] }
  override def getPresentacion(id: String): Future[Presentacion] = ws.url( url + "presentacion/byID")
    .post(Json.obj("id" -> id)).map { response => Json.parse(response.body).as[Presentacion] }
  /*****************************************************************************************************/


  /*********************Requisiciones  ************************************************/
  protected val renglonRequisicion: CRenglonRequisicion = new CRenglonRequisicion()

  override def getRequisicion(id: IdRequisicion): Future[Requisicion] = {
    val viewRenglonRequisicion = new CViewRenglonRequisicion()
    viewRenglonRequisicion.getRequisicion(id)
  }

  override def insertRenglonRequisicion(item: RenglonRequisicion): Future[Either[String,ViewRenglonRequisicion]] = {
    val itemTmp = Json.obj("item" -> item )

    renglonRequisicion.insertWS(item).map {
        case Right(reng) =>  Right(reng)
        case Left(error) =>  Left(error)
    }

    /*renglonRequisicion.insert( item ).map {
        case Right(SuccessJson(reng)) =>  Right( reng )
        case Left(FailureJson(error)) =>  Left(error)
    }*/
  }

  /*override def saveRenglonRequisicion(item: RenglonRequisicion): Future[ Either[String, ViewRenglonRequisicion] ] =
    renglonRequisicion.save ( item ).map {
        case Right(SuccessJson(reng)) =>  Right( reng )
        case Left(FailureJson(error)) =>  Left(error)
  }*/

  override def deleteRenglonRequisicion(item: RenglonRequisicion): Future[Option[ViewRenglonRequisicion]] = renglonRequisicion.delete(item)
  /************************************************************************************/

}
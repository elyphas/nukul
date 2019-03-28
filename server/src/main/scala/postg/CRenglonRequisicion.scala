package postg

import spatutorial.shared._
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext
import org.postgresql.util.{PSQLException}
import play.api.mvc.Results._

class CRenglonRequisicion extends CMyDataBase {

	import jdbcProfile.api._
	private val query = TableQuery[tabla]

	private class tabla(tag: Tag) extends Table[RenglonRequisicion](tag, "tbldetrequisicion") {
		def cve_oficina	= column[String]("cve_oficina")
		def folio = column[Int]("folio")
		def ejercicio = column[Int]("ejercicio")
		def renglon = column[Int]("renglon")
		def clave = column[String]("cve_articulo")
		def cantidad = column[Int]("cantidad")
		def precio= column[Double]("precio")

		def * = (cve_oficina, folio, ejercicio, renglon, clave, cantidad, precio) <> (RenglonRequisicion.tupled, RenglonRequisicion.unapply)
	}

	def insertWS( item: RenglonRequisicion )( implicit ec: ExecutionContext ): Future[ Either[ String, ViewRenglonRequisicion ] ] = {

		println( "Cuando va ha insertar en la tabla de postgresql	***********************" )

		db.run( ( query += item ).asTry ).flatMap {
			case Success( value ) =>
				val idReq = IdRequisicion( cve_oficina = item.cve_oficina, folio = item.folio, ejercicio = item.ejercicio )
				//viewRenglonRequisicion.ById( idReq, item.renglon )
				Future(Right(ViewRenglonRequisicion()))
			case Failure( e: PSQLException ) if e.getSQLState == "23505" =>
				InternalServerError( "Some sort of unique key violation.." )
				Future(Left("Some sort of unique key violation.."))
			case Failure(e: PSQLException) =>
				InternalServerError( "Some sort of psql error.." )
				Future(Left("Some sort of psql error.."))
			case Failure(_) =>
				InternalServerError( "Something else happened.. it was bad.." )
				Future(Left("Something else happened.. it was bad.."))
			case _ =>
				InternalServerError( "Quien sabe que paso ******************************************" )
				Future(Left("Something else happened.. it was bad.."))
		}
	}



	def delete(item: RenglonRequisicion )(implicit ec: ExecutionContext) : Future[Option[ViewRenglonRequisicion]] = {
		val delete = query.filter( r => r.cve_oficina === item.cve_oficina && r.folio === item.folio && r.renglon === item.renglon )
		db.run(delete.delete).map { r =>
			println("El resultado de querer eliminar")
			None
		}
	}




	/*def save(item: RenglonRequisicion)(implicit ec: ExecutionContext): Future[Option[ViewRenglonRequisicion]] = {
		println ( "···································································································" )
		println ( "Postgresql" )
		println ( "Al tratar de modificar un registro ya existente" )
		println ( item )

		val idRequi = IdRequisicion ( cve_oficina = item.cve_oficina, folio = item.folio, ejercicio = item.ejercicio )

		val Save = query
						.filter { r => r.cve_oficina === item.cve_oficina && r.folio === item.folio && r.ejercicio === item.ejercicio && r.renglon === item.renglon }
						.map ( fields => ( fields.clave, fields.cantidad, fields.precio ) )
						.update ( ( item.clave, item.cantidad, item.precio ) )
		for {
			_ <- db.run(Save)
			renglon <- viewRenglonRequisicion.ById(id = idRequi, renglon = item.renglon)
		} yield renglon
	}*/




	/*def save(item: RenglonRequisicion)(implicit ec: ExecutionContext): Future[Either[FailureJson, SuccessJson[ViewRenglonRequisicion]]] = {
		println ( "···································································································" )
		println ( "Postgresql" )
		println ( "Al tratar de modificar un registro ya existente" )
		println ( item )

		val idRequi = IdRequisicion ( cve_oficina = item.cve_oficina, folio = item.folio, ejercicio = item.ejercicio )

		val Save = query
			.filter { r => r.cve_oficina === item.cve_oficina && r.folio === item.folio && r.ejercicio === item.ejercicio && r.renglon === item.renglon }
			.map ( fields => ( fields.clave, fields.cantidad, fields.precio ) )
			.update ( ( item.clave, item.cantidad, item.precio ) )
		for {
			_ <- db.run(Save)
			renglon <- viewRenglonRequisicion.ById(id = idRequi, renglon = item.renglon)
		} yield renglon
	}*/



}
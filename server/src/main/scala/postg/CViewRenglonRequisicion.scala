package postg

import spatutorial.shared.{ViewRenglonRequisicion, IdRequisicion, Requisicion}

import scala.concurrent.Future
import javax.inject.Inject
//import org.apache.commons.lang3.exception.ExceptionContext
import scala.concurrent.ExecutionContext
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
//import scala.concurrent.ExecutionContext
import play.db.NamedDatabase
//import slick.jdbc.JdbcProfile

import scala.util.{Failure, Success}

import org.postgresql.util.{PSQLException}
import services.JsonEitherSpec.Data._

class CViewRenglonRequisicion extends CMyDataBase //@Inject()( @NamedDatabase("sicap") protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
{

	import jdbcProfile.api._
	//import profile.api._ //Original

	private val query = TableQuery[tabla]

	private class tabla(tag: Tag) extends Table[ViewRenglonRequisicion](tag,Some("requerimientos"), "qry_detalles") {

		def cve_oficina	= column[String]("cve_oficina")
		def folio = column[Int]("folio")
		def ejercicio= column[Int]("ejercicio")
		def renglon= column[Int]("renglon")
		def clave= column[String]("cve_articulo")
		def descripcion= column[String]("descripcion_articulo")
		def cantidad = column[Option[Int]]("cantidad")
		def precio = column[Double]("precio")

		def * = (cve_oficina, folio, ejercicio, renglon, clave, descripcion, cantidad, precio) <> (ViewRenglonRequisicion.tupled, ViewRenglonRequisicion.unapply)
	}

	def getRequisicion(id: IdRequisicion)(implicit ec: ExecutionContext): Future[Requisicion] = {

		val requisicion = query filter(r => r.ejercicio === id.ejercicio && r.folio === id.folio && r.cve_oficina === id.cve_oficina)

		db.run(requisicion.result).map { res =>
			Requisicion(items = res)
		}

	}

	/*def ById(id: IdRequisicion, renglon: Int)(implicit ec: ExecutionContext): Future[Option[ViewRenglonRequisicion]] = {
			val qry = query.filter { r => r.cve_oficina === id.cve_oficina && r.folio === id.folio && r.ejercicio === id.ejercicio }
			db.run(qry.result.headOption)
		}*/

	def ById( id: IdRequisicion, renglon: Int )(implicit ec: ExecutionContext): Future[Either[FailureJson, SuccessJson[ViewRenglonRequisicion]]] = {
		db.run ( query.filter { r => r.cve_oficina === id.cve_oficina && r.folio === id.folio && r.ejercicio === id.ejercicio }
			.result.asTry
		).map { /*result => result match {*/
				case Success( value ) =>
					if( value.isEmpty )
						Left( FailureJson( "No se encontro el Renglon" ) )
					else
						Right( SuccessJson( value.head ) )
				case Failure( e: PSQLException ) if e.getSQLState == "23505" =>
					Left( FailureJson( "Some sort of unique key violation.." ) )
				case Failure( e: PSQLException ) =>
					Left( FailureJson( "Some sort of psql error.." ) )
				case Failure( _ ) =>
					Left( FailureJson( "Something else happened.. it was bad.." ) )
			//}
		}
	}

}
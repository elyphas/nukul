package postg

import java.text.SimpleDateFormat

import akka.actor.FSM
import akka.japi.Option.Some
import spatutorial.shared._

import scala.concurrent.Future
/*import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider*/
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import org.postgresql.util.PSQLException
import play.api.mvc.Results._

import scala.util.Success
import scala.util.Failure
import cats.data.OptionT
import cats._
import cats.data._
import cats.implicits._


class CDatosGralesRequisicion extends CMyDataBase {

	import jdbcProfile.api._
	private val query = TableQuery[tabla]

	implicit val fechasColumnType = MappedColumnType.base[Fechas, java.sql.Date](
		{fecha =>
			val format = new SimpleDateFormat("dd/MM/yyyy")
			val parsed = format.parse(fecha.fecha)
			new java.sql.Date(parsed.getTime());
		},
		{sql =>
			val df = new SimpleDateFormat("dd/MM/yyyy")
			val text = df.format(sql)
			Fechas(fecha = text)
		}
	)

	private class tabla(tag: Tag) extends Table[DatosGralesRequisicion](tag, "tblrequisicion") {
		def cve_oficina	= column[String]("cve_oficina")
		def folio = column[Int]("folio")
		def ejercicio = column[Int]("ejercicio")

		def fecha	= column[Fechas]("fecha")
		def idfuente = column[String]("idfuente")
		def ejercicio_presupuestal = column[Int]("ejercicio_presupuestal")
		def fuente_financiamiento = column[String]("fuente_financiamiento")
		def programa = column[String]("programa")
		def descripcionprograma = column[String]("programa")
		def destino = column[String]("destino")
		def observaciones	= column[String]("observaciones")

		def * = (cve_oficina, folio, ejercicio, fecha, ejercicio_presupuestal, idfuente, fuente_financiamiento, programa, descripcionprograma, destino, observaciones) <> (DatosGralesRequisicion.tupled, DatosGralesRequisicion.unapply)
	}

	def insert(item: DatosGralesRequisicion)(implicit ec: ExecutionContext): Future[Option[DatosGralesRequisicion]] =
		db.run( ( query += item ).asTry ).map { result =>
			result match {
				case Success(value) =>
					Some(item)
				case Failure(e: PSQLException) if e.getSQLState == "23505" =>
					InternalServerError("Some sort of unique key violation..")
					none[DatosGralesRequisicion]//.pure[Future]
				case Failure(e: PSQLException) =>
					InternalServerError("Some sort of psql error..")
					none[DatosGralesRequisicion]//.pure[Future]
				case Failure(_) =>
					InternalServerError("Something else happened.. it was bad..")
					none[DatosGralesRequisicion]//.pure[Future]
			}
		}

/*
	def delete(item: RenglonRequisicion )(implicit ec: ExecutionContext) : Future[Option[ViewRenglonRequisicion]] = {
		val delete = query.filter( r => r.cve_oficina === item.cve_oficina && r.folio === item.folio && r.renglon === item.renglon )
		db.run(delete.delete).map { r =>
			println("El resultado de querer eliminar")
			None
		}
	}

	def save(item: RenglonRequisicion)(implicit ec: ExecutionContext): Future[Option[ViewRenglonRequisicion]] = {
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
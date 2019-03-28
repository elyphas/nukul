package spatutorial.shared

import boopickle.Default._
import com.sun.xml.internal.messaging.saaj.soap.FastInfosetDataContentHandler

object CRUDViewRenglonRequisicion {
		sealed trait CRUDViewRenglonRequisicion
		final case class SaveViewRenglonRequisicion( id: IdRequisicion, item: ViewRenglonRequisicion ) extends CRUDViewRenglonRequisicion
		final case class GetViewRenglonRequisicion( id: IdRequisicion, item: ViewRenglonRequisicion ) extends CRUDViewRenglonRequisicion
}

object CRUDDatosGralesRequisicion {
	sealed trait CRUDDatosGralesRequisicion
	final case class SaveDatosGralesRequisicion( item: DatosGralesRequisicion ) extends CRUDDatosGralesRequisicion
	final case class GetDatosGralesRequisicion( item: DatosGralesRequisicion ) extends CRUDDatosGralesRequisicion
}


sealed trait Father {
  val id: String
  val descripcion: Option[String]
}

object Event extends Enumeration {
	val Found, Saved, Inserted, Deleted, NotFound = Value
}

case class Deleted(num: Int)

case class Programa( id: String = "",
                     descripcion: Option[String] = None,
                     destino: String = "",
                     depto: String = "1221",
                     mostrar: Boolean = true,
                     rfc_dependencia: String = "SES890417TX8",
                     nivel: String = "ESTATAL",
                     encargado: Option[String] = Some("PENDIENTE"),
                     activo: Boolean = true,
                     fuente_financiamiento: String = "") extends Father


case class Oficina(	 id: String = "",
										 descripcion: Option[String] = None,
										 firma: Option[String] = None,
										 cargo: Option[String] = None
                  ) extends Father

case class Fuente(	id: String = "",
										descripcion: Option[String] = None,
										observaciones: Option[String] = None,
										rfc_dependencia: String  = "SES890417TX8",
										nivel: Option[String] = None
								 ) extends Father

case class Articulo (
						id: String = "",
	          descripcion: Option[String] = None,
						unidad: String = "",
						presentacion: Option[Int] = None,
						unid_med_pres: Option[String] = None,
						partida: String = "",
						cabms: Option[String] = None,
						cb: Option[Boolean] = None,
						iva: Option[Double] = None,
						baja: Option[Boolean] = None	) extends Father

case class Partida (	id: String = "",
                      descripcion: Option[String] = None,
											observaciones: Option[String] = None,
	                    presupuesto: Option[Double] = None,
                      activo: Option[Boolean] = None) extends Father

case class Presentacion(	id: String = "",
													descripcion: Option[String] = None,	//unidad se repite con fines practicos
                       		presentacion: Option[Double] = None,
                       		unidad_present: Option[String] = None) extends Father

case class Usuarios(
							usuario: String = "",
							contraseña: String = "",
							tipo: String = "",
							nombre: String = "",
							area: String = "",
							activo: Boolean = true,
							nivel: Int = 0)

case class Proveedor(
					id: String = "",
					descripcion: Option[String] = None,
					propietario: Option[String] = None,
					calle: Option[String] = None,
					colonia: Option[String] = None,
					delegacion: Option[String] = None,
					cp: Option[String] = None,
					ciudad: Option[String] = None,
					telefonos: Option[String] = None,
					fax: Option[String] = None,
					observaciones: Option[String] = None,
					activo: Boolean = true,
					elaboro: Option[String] = None,
					giro: Option[String] = None,
					descuento: Option[String] = None) extends Father

/********************Types for Requisiciones**********************************************/
case class RenglonRequisicion (
																cve_oficina: String = "1221",
																folio: Int = 1,
																ejercicio: Int = 2018,
																renglon: Int = 0,
																clave: String = "",
																cantidad: Int = 0,
																precio: Double = 0.0
															)

case class ViewRenglonRequisicion (
																		cve_oficina: String = "",
																		folio: Int = 0,
																		ejercicio: Int = 0,
																		renglon: Int = 0,
																		clave: String = "",
																		descripcion: String = "",
																		cantidad: Option[Int] = None,
																		precio: Double = 0.0
																	)


case class DatosGralesRequisicion( 	cve_oficina: String = "",
																		folio: Int = 0,
																		ejercicio: Int = 2018,
																		fecha: Fechas = Fechas(fecha = "01/01/2018"),
																		ejercicio_presupuestal: Int = 2018,
																		idfuente: String = "",
																		fuente_financiamiento: String = "",
																		programa: String = "",
																		descripcionprograma: String = "",
																		destino: String = "Falta",
																		observaciones: String = "")


case class Fechas ( fecha: String )

case class Requisicion ( items: Seq[ ViewRenglonRequisicion ] = Seq.empty[ ViewRenglonRequisicion ] ) {

	def update( newItem: ViewRenglonRequisicion ) = {
		val isNew = ( i: ViewRenglonRequisicion ) => {
			i.cve_oficina == newItem.cve_oficina && i.folio == newItem.folio &&
				i.ejercicio == newItem.ejercicio && i.renglon == newItem.renglon
		}

		items.indexWhere( isNew ) match {
			case -1 =>	Requisicion ( items :+ newItem ) 								// add new
			case idx =>	 Requisicion ( items.updated ( idx, newItem ) )	// replace old
		}

	}

	def remove(item: ViewRenglonRequisicion) = Requisicion(items.filterNot(_ == item))

	def insert(item: ViewRenglonRequisicion) = Requisicion(items :+ item)

}

case class IdRequisicion(cve_oficina: String = "", folio: Int = 0, ejercicio: Int = 0)


package spatutorial.client.validates


import cats._
import cats.data._
import cats.implicits._
import spatutorial.shared.Programa
import spatutorial.shared.Fechas

//import cats.data.Validated._
import cats.data.Validated.{ valid, invalidNel }
import cats.data.ValidatedNel

import spatutorial.shared.DatosGralesRequisicion

sealed trait DatosGralesCreationError

object DatosGralesCreationError {

  case object CveOficinaError extends DatosGralesCreationError
  case object FolioError  extends DatosGralesCreationError
  case object EjercicioError  extends DatosGralesCreationError
  case object FechaError extends DatosGralesCreationError
  case object FuenteError extends DatosGralesCreationError
  case object DescFuenteError extends DatosGralesCreationError
  case object ProgramaError extends DatosGralesCreationError
  case object DescProgramaError extends DatosGralesCreationError
  case object DestinoError extends DatosGralesCreationError

  def checkCveOficina(id: String): ValidatedNel[DatosGralesCreationError, String] = {
    if (id.length > 3) valid(id)
    else invalidNel(CveOficinaError)
  }

  def checkFolio(folio: Int): ValidatedNel [DatosGralesCreationError, Int] = {
    if (folio > 0) valid(folio)
    else invalidNel (FolioError)
  }

  def checkEjercicio(ejercicio: Int): ValidatedNel [DatosGralesCreationError, Int] = {
    if (ejercicio > 0) valid(ejercicio)
    else invalidNel (EjercicioError)
  }

  def checkFecha(fecha: String): ValidatedNel [DatosGralesCreationError, String] = {
    if (fecha != "") valid(fecha)
    else invalidNel (EjercicioError)
  }

  def checkEjercicioPresupuestal(ejercicio: Int): ValidatedNel [DatosGralesCreationError, Int] = {
    if (ejercicio > 0) valid(ejercicio)
    else invalidNel (EjercicioError)
  }

  def checkFuente(fuente: String): ValidatedNel [DatosGralesCreationError, String] = {
    if (fuente.size > 0) valid(fuente)
    else invalidNel (FuenteError)
  }

  def checkDescFuente(fuente: String): ValidatedNel [DatosGralesCreationError, String] = {
    if (fuente.size > 0) valid(fuente)
    else invalidNel (FuenteError)
  }

  def checkPrograma(programa: String): ValidatedNel [DatosGralesCreationError, String] = {
    if (programa.size > 0) valid(programa)
    else invalidNel (ProgramaError)
  }

  def checkDescPrograma(programa: String): ValidatedNel [DatosGralesCreationError, String] = {
    if (programa.size > 0) valid(programa)
    else invalidNel (ProgramaError)
  }

  def checkDestino(destino: String): ValidatedNel [DatosGralesCreationError, String] = {
    if (destino.size > 0) valid(destino)
    else invalidNel (ProgramaError)
  }

  /*case class DatosGralesRequisicion(
        cve_oficina: String = "",
        folio: Int = 0,
        ejercicio: Int = 2018,
        fecha: Fechas = Fechas(fecha = "01/01/2018"),
        ejercicio_presupuestal: Int = 2018,
        idfuente: String = "",
        fuente_financiamiento: String = "",
        programa: String = "",
        descripcionprograma: String = "",
        destino: String = "Falta",
  )*/

  def createDatosGrales(id: String,
                        folio: Int,
                        ejercicio: Int,
                        fecha: String,
                        ejercicioPres: Int,
                        fuente: String,
                        descFuente: String,
                        programa: String,
                        descPrograma: String,
                        destino: String
                       ): ValidatedNel[DatosGralesCreationError, DatosGralesRequisicion] = {
    (
          checkCveOficina(id),
          checkFolio(folio),
          checkEjercicio(ejercicio),
          checkFecha(fecha),
          checkEjercicioPresupuestal(ejercicioPres),
          checkFuente(fuente),
          checkDescFuente(descFuente),
          checkPrograma(programa),
          checkDestino(destino)
    ) mapN ( (cveOfix, fol, ejercic, fech, ejercicioPre, fuent, descFuent, program, desti ) =>
          DatosGralesRequisicion (
              cve_oficina = cveOfix,
              folio = folio,
              ejercicio = ejercic,
              fecha = Fechas(fech),
              ejercicio_presupuestal = ejercicioPres,
              destino = desti
        )
    )

  }
}
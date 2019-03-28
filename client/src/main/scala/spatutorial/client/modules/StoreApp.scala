package spatutorial.client.modules

import outwatch.util.Store
import spatutorial.shared.{DatosGralesRequisicion, Father, Fuente, ViewRenglonRequisicion}
import monix.execution.Scheduler.Implicits.global

object StoreApp {

  sealed trait ActionViewRenglonRequisicion
  case object Search extends ActionViewRenglonRequisicion
  case class RenglonActive(reng: ViewRenglonRequisicion) extends ActionViewRenglonRequisicion
  case object Clean extends ActionViewRenglonRequisicion
  case class Clean(s:String) extends ActionViewRenglonRequisicion
  case class UpdateIdComparativo( id: String) extends ActionViewRenglonRequisicion
  case class UpdateRengActiv( ren: ViewRenglonRequisicion ) extends ActionViewRenglonRequisicion
  case class UpdateResultsCatalog(lst: Seq[Father]) extends ActionViewRenglonRequisicion
  case class UpdateDatosGrales(item: DatosGralesRequisicion) extends ActionViewRenglonRequisicion
  case object CleanResultsCatalog extends ActionViewRenglonRequisicion

  case class NukulState(
                         listado: Seq[ViewRenglonRequisicion],
                         renglonActive: ViewRenglonRequisicion,
                         idComparativo: String = "",
                         lstResultsCatalog: Seq[Father] = Seq.empty[Father],
                         datosGralesRequisicion: DatosGralesRequisicion = DatosGralesRequisicion()
                       ) {

    def updated(newItem: ViewRenglonRequisicion) = {
      listado.indexWhere(_.renglon == newItem.renglon) match {
        case -1 => NukulState(listado :+ newItem, renglonActive = renglonActive)
        case idx => NukulState(listado.updated(idx, newItem), renglonActive = renglonActive)
      }
    }
    def remove(item: ViewRenglonRequisicion) = NukulState(listado.filterNot(_ == item), renglonActive = renglonActive)
  }

  val reduce: (NukulState, ActionViewRenglonRequisicion) => NukulState = (s, a) => a match {
      case RenglonActive(reng) => s.copy(renglonActive = reng)
      case UpdateRengActiv(reng) => s.copy(renglonActive = reng)
      case Clean => NukulState(listado = Seq.empty[ViewRenglonRequisicion], renglonActive = ViewRenglonRequisicion())
      case Clean(s) =>  NukulState(listado = Seq.empty[ViewRenglonRequisicion], renglonActive = ViewRenglonRequisicion())
      case UpdateIdComparativo(id) => s.copy( idComparativo = id )
      case UpdateResultsCatalog(lst) => s.copy( lstResultsCatalog = lst )
      case UpdateDatosGrales(item) => s.copy( datosGralesRequisicion = item )
      //case CleanResultsCatalogFuente => s.copy( lstResultsCatalog = Seq.empty[Father])
      case CleanResultsCatalog => s.copy( lstResultsCatalog = Seq.empty[Father])
      case _ => NukulState(listado = Seq.empty[ViewRenglonRequisicion], renglonActive = ViewRenglonRequisicion())
    }

  //val initialState = NukulState(listado = Seq.empty[ViewRenglonRequisicion], renglonActive = ViewRenglonRequisicion())
  val renglonInitial = ViewRenglonRequisicion(renglon = 1)

  val initialState = NukulState(listado = Seq(renglonInitial), renglonActive = renglonInitial)

  //val store = Store.create[NukulState, ActionViewRenglonRequisicion]( Clean("Limpiar"), initialState, reduce (_,_) ).unsafeRunSync()
  val store = Store.create[ActionViewRenglonRequisicion, NukulState]( Clean("Limpiar"), initialState, reduce ).unsafeRunSync()

}

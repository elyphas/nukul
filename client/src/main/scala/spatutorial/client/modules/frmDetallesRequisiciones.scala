package spatutorial.client.modules

import monix.reactive.{Observable, Observer}

import org.scalajs.dom.ext.KeyCode
import outwatch.dom._
import outwatch.dom.dsl._
import spatutorial.client.modules.StoreApp.{ActionViewRenglonRequisicion, NukulState, UpdateIdComparativo}
import spatutorial.client.components.GridEditable._

import spatutorial.client.modules.StoreApp._

object FrmDetallesRequisiciones {

  val sizeFuente: VDomModifier = fontSize := "12px"

  val onKeyEnterIdComparativo = onKeyUp
                                .filter { k => k.keyCode == KeyCode.Enter }
                                .map ( v => UpdateIdComparativo ( id = v.toString ) )

  /*def render( state: Observable[(ActionViewRenglonRequisicion, NukulState)],
              store: Observer[ActionViewRenglonRequisicion]) = */

  def render = div( clear.both, paddingTop := "50px",
      store.map { case ( action, s ) =>
        div ( clear.both, marginTop := "30px",
          label ( "Id Comparativo", sizeFuente ),
          input ( sizeFuente,
            onKeyEnterIdComparativo --> store,
            s.idComparativo
          ),
          gridEditable ( store, store )
        )
      }
    )
}

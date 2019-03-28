package spatutorial.client.components

import outwatch.dom.Handler
import outwatch.dom.dsl._
import spatutorial.shared.Fechas
//import monix.reactive.Observer
//import monix.reactive.Observable

object Components {

  def cmpInput( lbl: String, hdl: Handler[String], w: Int ) = {
    div( width := (w + 200).toString  + "px", label(lbl),
      input (
        value <-- hdl.map( r => r ),
        onChange.target.value --> hdl,
        width := w.toString + "px"
      )
    )
  }

  def cmpInputInt( lbl: String, hdl: Handler[Int], w: Int ) = {
    div( width := (w + 200).toString + "px", label(lbl),
      input (
        value <-- hdl.map( r => r.toString ),
        onChange.target.value.map(r=> r.toInt) --> hdl,
        width := w.toString + "px"
      )
    )
  }

  def cmpInputFechas( lbl: String, hdl: Handler[Fechas], w: Int ) = {
    div( width := (w + 200).toString + "px", label(lbl),
      input (
        value <-- hdl.map( r => r.fecha ),
        onChange.target.value.map(r=> Fechas(r)) --> hdl,
        width := w.toString + "px"
      )
    )
  }

  def cmpInputSearch( lbl: String, hdl: Handler[String], w: Int ) = {
    div( width := w.toString + "px", label(lbl),
      input (
        value <-- hdl.map( r => r ),
        onChange.target.value --> hdl,
        width := "60px"
      )
    )
  }


}

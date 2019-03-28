package spatutorial.client.components

import monix.execution.Ack.Continue
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html
import outwatch.dom.{Sink, VDomModifier}
import outwatch.dom.dsl._
import spatutorial.client.services.AjaxClient
import spatutorial.shared.{Api, Articulo, RenglonRequisicion, ViewRenglonRequisicion}
import boopickle.Default._
import autowire._
import spatutorial.shared.{Api, Articulo, ViewRenglonRequisicion}
import spatutorial.client.services.AjaxClient
import monix.reactive._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.subjects.PublishSubject
import monix.execution.Ack.Continue
import cats.implicits._
import monix.execution.Ack
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html
import org.scalajs.dom
import outwatch.dom._
import outwatch.dom.dsl._
import spatutorial.client.modules.StoreApp._

object GridEditable {

  def cellValue ( col: Int, txt: String, edit: Boolean )( implicit row: html.TableRow ): html.TableCell = {
    val cell = row.insertCell( -1 ).asInstanceOf[ html.TableCell ]
    cell.innerHTML = txt
    cell.style.border = "1px solid"
    cell.contentEditable = if(edit) "true" else "false"
    cell
  }

  def focusCellBgColor(cell: html.TableCell, cellnext: html.TableCell) = {
    //cell.style.backgroundColor = "white"
    //cellnext.style.backgroundColor = "Aquamarine"
    //cellnext.focus()
  }

  def MoveRowDonw(cell: html.TableCell, numbCol: Int) = {
    val tbl = dom.document.getElementById("tblRenglones").asInstanceOf[html.Table]
    val currRow = cell.parentNode.asInstanceOf[ html.TableRow ]
    val maxRows = tbl.rows.length
    if ( (currRow.rowIndex + 1) < maxRows ) {
      val nextRow = cell.parentNode.nextSibling.asInstanceOf[html.TableRow] //MoveRowDonw(cell)
      val nextCell = nextRow.cells(numbCol).asInstanceOf[html.TableCell]
      nextCell
    } else cell
  }

  sealed trait MoveCursor
  final case object CursLeft extends MoveCursor
  final case object CursRight extends MoveCursor
  final case object CursDown extends MoveCursor
  final case object CursUp extends MoveCursor

  def changePosCell(cellAct: html.TableCell, mov: MoveCursor) = {
    val tbl = dom.document.getElementById("tblRenglones").asInstanceOf[html.Table]
    val currRow = cellAct.parentNode.asInstanceOf[ html.TableRow ]
    val maxRows = tbl.rows.length

    mov match {
      case CursDown =>
        val nextCell = MoveRowDonw(cellAct, cellAct.cellIndex)
        nextCell.focus()
        //focusCellBgColor(cellAct, nextCell)
      case CursUp =>
        if ( currRow.rowIndex > 1) {
          val nextRow = cellAct.parentNode.previousSibling.asInstanceOf[html.TableRow]
          val nextCell = nextRow.cells(cellAct.cellIndex).asInstanceOf[html.TableCell]
          nextCell.focus()
          //focusCellBgColor(cellAct, nextCell)
        }
      case CursLeft =>
        if (cellAct.cellIndex >= 1 ) {
          val nextCell = cellAct.previousSibling.asInstanceOf[html.TableCell]
          nextCell.focus()
          //focusCellBgColor(cellAct, nextCell)
        }
      case CursRight =>                   //El indice de las columnas empieza en 0
        val nextCell = cellAct.cellIndex match {
          case col if col >= 2 => MoveRowDonw(cellAct, 0)
          case _ => cellAct.nextSibling.asInstanceOf[ html.TableCell ]
        }
        nextCell.focus()
        //focusCellBgColor(cellAct, nextCell )
    }
  }

  val onKeyDownTable = Sink.create [ dom.KeyboardEvent ] { key: dom.KeyboardEvent =>
    val cellActive = key.target.value.asInstanceOf[html.TableCell]
    val t1 = cellActive.parentNode.asInstanceOf[html.TableRow]
    val t2 = t1.parentNode.asInstanceOf[html.Table]

    if ( key.keyCode == KeyCode.Insert) {
      val tbl = dom.document.getElementById("tblRenglones").asInstanceOf[html.Table]
      implicit val newRow = t2.insertRow( -1 ).asInstanceOf[ html.TableRow ]

      val cellRenglon = cellValue(1,   "rengl", false)
      val cellClave = cellValue(2,   "clav", true)
      val cellDescripcion = cellValue( 3,   "descrip", true)

      val rows = tbl.rows.length - 1
      cellRenglon.textContent = rows.toString
      cellClave.focus()
      cellActive.focus()
      //focusCellBgColor(cellActive, cellRenglon)

    } else if (key.keyCode == KeyCode.Enter) {
      if ( cellActive.cellIndex == 0 ) {
        for {
          result <- AjaxClient[Api].getArticulo( cellActive.textContent ).call()
        } yield {
          val artic = result.getOrElse(Articulo())
          val cellDescripcion = t1.cells( 2 ).asInstanceOf[ html.TableCell ]
          cellDescripcion.textContent = artic.descripcion.getOrElse ( "" )
          cellActive.focus()
          //focusCellBgColor ( cellActive, cellDescripcion )
          cellDescripcion.focus()
        }
      }
    }
    else if ( key.keyCode == 38 ) changePosCell( cellActive, CursUp )
    else if ( key.keyCode == 40 ) changePosCell( cellActive, CursDown )
    else if ( key.keyCode == 37 ) changePosCell( cellActive, CursLeft )
    else if ( key.keyCode == KeyCode.Tab ) changePosCell( cellActive, CursRight )
    Continue
  }

  val changeCell = Sink.create[String] { ch: String =>
    println(ch)
    Continue
  }

  val keyEnterColClave = onKeyUp.filter { k =>
    val cellActive = k.target.value.asInstanceOf[html.TableCell]
    (k.keyCode == KeyCode.Enter) && (cellActive.cellIndex == 2)
  }.map { k =>
    val cellActive = k.target.value.asInstanceOf[html.TableCell]
    val art = getArticulo(cellActive)
    //Add( getArticulo( cellActive ) )
    UpdateRengActiv(ren = ViewRenglonRequisicion(
      renglon = art.renglon,
      clave = art.clave,
      descripcion = art.descripcion,
      cantidad = art.cantidad
    ))
  }

  case class StyleCell(edit: Boolean, align: String)
  case class CellVal(txt: String, style: StyleCell)

  def styleCell(styleCell: StyleCell): List[ VDomModifier ] =
                List( contentEditable := styleCell.edit, textAlign := styleCell.align, styles.border := "1px solid" )

  def newCell(cellVal: CellVal)  = td(cellVal.txt, styleCell(cellVal.style) )

  //def newRow(values: Seq[(String, Boolean, String)]) = values.map { case (txt, edit) => newCell(txt, edit) }
  def newRow(value: Seq[CellVal]) = value.map { cellVal => newCell(cellVal) }

  def title(txt: String, edit: Boolean, width: Int) =
    td (
        txt, styles.border:="1px solid",
        styles.backgroundColor := "lightGray",
        styles.width := width.toString + "px"
    )

  val keyEnterClave = onKeyUp.filter { k =>
      val cellActive = k.target.value.asInstanceOf[html.TableCell]
      (k.keyCode == KeyCode.Enter) && (cellActive.cellIndex == 1)
    }.map { k =>
      val cellActive = k.target.value.asInstanceOf[html.TableCell]
      val currRow = cellActive.parentNode.asInstanceOf[ html.TableRow ]
      val row = cellActive.parentNode.asInstanceOf[html.TableRow]
      val cantidadCell = row.cells(3).asInstanceOf[html.TableCell]
      cantidadCell.focus()
      //focusCellBgColor(cellActive, cantidadCell)
      val cve = cellActive.textContent
      cve
  }

  //Esto no deberia estar aqui el renglon activo esta en el 'Store'
  def getArticulo(cellActive: html.TableCell) = {
    val row = cellActive.parentNode.asInstanceOf[html.TableRow]
    val numRow = row.rowIndex
    val cellRenglon = row.cells(0).asInstanceOf[html.TableCell]
    val cellClave = row.cells(1).asInstanceOf[html.TableCell]
    val cellDescripcion = row.cells(2).asInstanceOf[html.TableCell]
    ViewRenglonRequisicion (
        renglon = 0,
        clave  = cellClave.textContent,
        descripcion = cellDescripcion.textContent
    )
  }

  val onSearchArticulo = PublishSubject[String]()

  val currentProvider = onSearchArticulo.switchMap { cve =>
      Observable.fromFuture(AjaxClient[Api].getArticulo(cve).call().map(a => a.getOrElse(Articulo())))
  }.share

  val results = currentProvider.map { provider =>
      UpdateRengActiv( ViewRenglonRequisicion(clave = provider.id, descripcion = provider.descripcion.getOrElse("") ))
  }.subscribe( store )

  val onSaveRenglon = PublishSubject[ViewRenglonRequisicion]()

  val savedRenglon = onSaveRenglon.switchMap { reng =>
    val renglon = RenglonRequisicion( renglon = reng.renglon, clave = reng.clave, cantidad = reng.cantidad.getOrElse( 0 ) )
    Observable.fromFuture(AjaxClient[Api].insertRenglonRequisicion(renglon).call().map( a => a.getOrElse( ViewRenglonRequisicion() ) ) )
  }.share

  val resultSave = savedRenglon.map { provider => UpdateRengActiv( provider ) }.subscribe( store )

  def keyEnterColCantidad = onKeyUp.filter { k =>
    val cellActive = k.target.value.asInstanceOf[html.TableCell]
    cellActive.cellIndex == 3
  }.filter { k1 => k1.keyCode == KeyCode.Enter }

  def gridEditable( state: Observable[(ActionViewRenglonRequisicion, NukulState)],
                    store: Observer[ActionViewRenglonRequisicion]): VDomModifier =

    state.map { current =>

     table(id := "tblRenglones", caption("Pa captura"),
        thead (
          tr (
            title("Renglon", false, 50),
            title("Clave", false, 120),title("Descripcion", false, 300),
            title("Cantidad", false, 70)
          )
        ),
        tbody (
          keyEnterColCantidad.map { k =>
            val cellActive = k.target.value.asInstanceOf[ html.TableCell ]
            current._2.renglonActive.copy( cantidad = Some( cellActive.textContent.toInt ) )
          } --> onSaveRenglon,
          keyEnterClave --> onSearchArticulo,
          keyEnterColClave --> store,
          onKeyDown.map { input =>
            val cellActive = input.target.value.asInstanceOf[html.TableCell]
            if (input.keyCode == KeyCode.Enter /*&& cellActive.cellIndex == 1*/) input.preventDefault()
            else if ( input.keyCode == KeyCode.Tab) input.preventDefault()
            input
          } --> onKeyDownTable,
          current._2.listado.map { v =>
            tr( id :=  "row1",
              newRow(
                Seq(  CellVal("1", StyleCell(false, "center")),
                      CellVal(v.clave, StyleCell(true,"center")),
                      CellVal(current._2.renglonActive.descripcion, StyleCell(false, "left")),
                      CellVal(v.cantidad.getOrElse("").toString, StyleCell(true, "right"))
                )
              )
            )
          }
        )
      )
    }
}
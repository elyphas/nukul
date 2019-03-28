package spatutorial.shared

import scala.concurrent.Future

trait Api {

  /*************************  Oficinas    ********************************************************/
  //def Oficinas(): Future[Seq[Oficina]]
  def getOficina(id: String): Future[Oficina]
  def getAllOficinas(): Future[Seq[Oficina]]
  /***********************************************************************************************/

  /*************************  Proveedores    *****************************************************/
  def saveProveedor(item: Proveedor): Future[Either[String, Proveedor]]
  def searchIDProveedor(str: String): Future[Either[String, Proveedor]]
  def searchProveedor(descripcion: String): Future[Seq[Proveedor]]
  /***********************************************************************************************/

  /*************************  Partidas    ********************************************************/
  def getPartidas(): Future[Seq[Partida]]
  def getPartida(id: String): Future[Partida]
  /***********************************************************************************************/

  /*************************   Usuarios       ****************************************************/
  def getUsuario(user: String): Future[Seq[Usuarios]]
  def logear(item: Usuarios): Future[(Usuarios, String)]
  /***********************************************************************************************/

  /********************** Articulos **************************************************************/
  def getArticulo(cve: String): Future[Option[Articulo]]
  def getAllArticulos(): Future[Seq[Articulo]]
  def searchID(str: String): Future[Seq[Articulo]]
  def searchArticulo(str: String): Future[Option[Seq[Articulo]]]
  /***********************************************************************************************/

  /*********    Programas     ********************************************************************/
  def getPrograma(id: String): Future[Programa]
  def getAllPrograma(): Future[Seq[Programa]]
  def searchPrograma(descripcion: String): Future[Seq[Programa]]
  /***********************************************************************************************/

  /*********    Fuentes     **********************************************************************/
  def getFuente(id: String): Future[Fuente]
  def getAllFuente(): Future[Seq[Fuente]]
  def searchFuente( descripcion: String ): Future[Seq[Fuente]]
  /***********************************************************************************************/

  /*********    Presentaciones     ***************************************************************/
  def getPresentacion(id: String): Future[Presentacion]
  def getAllPresentaciones(): Future[Seq[Presentacion]]
  /***********************************************************************************************/

  /*********************Requisiciones  ************************************************/
  def getRequisicion(id: IdRequisicion): Future[Requisicion]
  //def insertRenglonRequisicion(item: RenglonRequisicion): Future[Option[ViewRenglonRequisicion]]
  def insertRenglonRequisicion(item: RenglonRequisicion): Future[Either[String,ViewRenglonRequisicion]]

  //def saveRenglonRequisicion(item: RenglonRequisicion): Future[Either[String,ViewRenglonRequisicion]]
  def deleteRenglonRequisicion(item: RenglonRequisicion): Future[Option[ViewRenglonRequisicion]]
  /************************************************************************************/

}
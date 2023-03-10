package controlador;
import dao.HistoricoDao;
import dao.PronosticoDao;
import modelo.VentaPeriodo;
import vista.VistaPronostico;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/*
 * @author Nicolas Herrera <herrera.nicolas@correounivalle.edu.co>
 * @author Samuel Galindo Cuevas <samuel.galindo@correounivalle.edu.co>
 */

public class ControladorPronostico
{
    private final HistoricoDao historicoDao;
    private final VistaPronostico vistaPronostico;
    private final PronosticoDao pronosticoDao;

    public ControladorPronostico(HistoricoDao auxHistoricoDao, VistaPronostico auxVistaPronostico, PronosticoDao auxPronosticoDao)
    {
        this.vistaPronostico = auxVistaPronostico;
        this.historicoDao = auxHistoricoDao;
        this.pronosticoDao = auxPronosticoDao;

        vistaPronostico.pantallaCompleta();

        PronosticoListener pronosticoListener = new PronosticoListener();
        vistaPronostico.addBtnAgregarAñoListener(pronosticoListener);
        vistaPronostico.addBtnModificarAñoListener(pronosticoListener);
        vistaPronostico.addBtnBorrarAñoListener(pronosticoListener);
        vistaPronostico.addBtnNuevoPronosticoListener(pronosticoListener);
    }

    private void agregarVentaHistorica()
    {
        VentaPeriodo auxVentaPeriodo;
        int auxId;
        int auxPeriodo;
        double auxCantidadVentas;

        auxId = Integer.parseInt(vistaPronostico.getAño());
        if(auxId == 0)
        {
            if(comprobarCampoCantidadVenta())
            {
                try
                {
                    auxCantidadVentas = Double.parseDouble(vistaPronostico.getCantidadVentas());


                    if(auxCantidadVentas >= 0)
                    {
                        if(historicoDao.getHistorico().isEmpty())
                        {
                            auxVentaPeriodo = new VentaPeriodo(auxCantidadVentas, 0 ,0);
                            if(historicoDao.agregarHistorico(auxVentaPeriodo))
                            {
                                vistaPronostico.setCantidadVentas("");
                                listarAgregarVenta(auxVentaPeriodo);
                            }
                        }
                        else
                        {
                            auxVentaPeriodo = new VentaPeriodo(auxCantidadVentas, 0 ,0);
                            if(historicoDao.agregarHistorico(auxVentaPeriodo))
                            {
                                calcularVariacionAgregar(auxVentaPeriodo);
                                vistaPronostico.setCantidadVentas("");
                                vistaPronostico.setTotalVariacion("" + calcularTotalPorcentajeVariacion());
                                listarAgregarVenta(auxVentaPeriodo);
                            }
                        }
                    }
                    else
                    {
                        vistaPronostico.mostrarMensajeError("No se puede agregar ventas negativas");
                        vistaPronostico.setCantidadVentas("");
                    }

                }
                catch (NumberFormatException ex)
                {
                    vistaPronostico.mostrarMensajeError("ingrese la cantidad de ventas correctamente");
                    vistaPronostico.setCantidadVentas("");
                }
            }
        }
        else
        {
            vistaPronostico.mostrarMensajeError("Deseleccione la fila");
        }
    }

    private void modificarVentaHistorica()
    {
        VentaPeriodo auxVentaPeriodo;
        int auxAño;
        double auxCantidadVentas;

        auxAño = Integer.parseInt(vistaPronostico.getAño());
        auxVentaPeriodo = historicoDao.getVentaHistorica(auxAño);

        if(auxVentaPeriodo != null)
        {
            if(comprobarCampoCantidadVenta())
            {
                try
                {
                    auxCantidadVentas = Double.parseDouble(vistaPronostico.getCantidadVentas());

                    if(auxCantidadVentas >= 0)
                    {
                        if(historicoDao.getHistorico().size() == 1)
                        {
                            auxVentaPeriodo.setCantidadVentas(auxCantidadVentas);
                            if(historicoDao.editarHistorico(auxVentaPeriodo))
                            {
                                listarModificarVenta(auxVentaPeriodo, vistaPronostico.getFilaSeleccionadaVentas());
                                vistaPronostico.setCantidadVentas("");
                                vistaPronostico.setTotalVariacion("" + calcularTotalPorcentajeVariacion());
                                vistaPronostico.deseleccionarFilaTablaVentas();
                                vistaPronostico.setAño("0");
                            }
                        }
                        else
                        {
                            auxVentaPeriodo.setCantidadVentas(auxCantidadVentas);
                            if(historicoDao.editarHistorico(auxVentaPeriodo))
                            {
                                calcularVariacionModificar(auxVentaPeriodo);
                                vistaPronostico.setCantidadVentas("");
                                vistaPronostico.setTotalVariacion("" + calcularTotalPorcentajeVariacion());
                                vistaPronostico.deseleccionarFilaTablaVentas();
                                vistaPronostico.setAño("0");
                            }
                        }
                    }
                    else
                    {
                        vistaPronostico.mostrarMensajeError("No se puede agregar ventas negativas");
                        vistaPronostico.setCantidadVentas("");
                    }
                }
                catch (NumberFormatException ex)
                {
                    vistaPronostico.mostrarMensajeError("ingrese la cantidad de ventas correctamente");
                    vistaPronostico.setCantidadVentas("");
                }
            }
        }
        else
        {
            vistaPronostico.mostrarMensajeError("Seleccione una fila");
        }
    }

    private void borrarVentaHistorica()
    {
        VentaPeriodo auxVentaPeriodo;
        int auxAño;

        auxAño = Integer.parseInt(vistaPronostico.getAño());
        auxVentaPeriodo = historicoDao.getVentaHistorica(auxAño);

        if(auxVentaPeriodo != null)
        {
            if(historicoDao.getHistorico().size() == 1)
            {
                if(historicoDao.eliminarHistorico(auxVentaPeriodo))
                {
                    recalcularAños();
                    listarBorrarVenta();
                    vistaPronostico.setCantidadVentas("");
                    vistaPronostico.setTotalVariacion("0");
                    vistaPronostico.deseleccionarFilaTablaVentas();
                    vistaPronostico.setAño("0");
                }
            }
            else
            {
                if(historicoDao.eliminarHistorico(auxVentaPeriodo))
                {
                    listarBorrarVenta();
                    recalcularAños();
                    calcularVariacionBorrar(auxVentaPeriodo);
                    vistaPronostico.setCantidadVentas("");
                    vistaPronostico.setTotalVariacion("" + calcularTotalPorcentajeVariacion());
                    vistaPronostico.deseleccionarFilaTablaVentas();
                    vistaPronostico.setAño("0");

                }
            }
        }
        else
        {
            vistaPronostico.mostrarMensajeError("Seleccione una fila");
        }
    }

    private boolean comprobarCampoCantidadVenta()
    {
        boolean campoValido;
        campoValido = !vistaPronostico.getCantidadVentas().equals("");
        return  campoValido;
    }

    private void calcularVariacionAgregar(VentaPeriodo auxVenta)
    {
        double variacionPorcentajeVenta;
        double auxVariacion;

        VentaPeriodo auxVentaAnterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo()-1);
        auxVariacion = auxVenta.getCantidadVentas() - auxVentaAnterior.getCantidadVentas();
        if(auxVentaAnterior.getCantidadVentas()!=0)
        {
            variacionPorcentajeVenta = auxVariacion / auxVentaAnterior.getCantidadVentas();
        }
        else
        {
            variacionPorcentajeVenta = auxVariacion;
        }

        auxVenta.setVariacionVentas(auxVariacion);
        auxVenta.setPorcentajeVariacionVenta(variacionPorcentajeVenta);
    }

    private void calcularVariacionModificar(VentaPeriodo auxVenta)
    {
        double auxVariacion = 0;
        double auxVariacionPorcentaje = 0;

        if(historicoDao.getHistorico().size() != auxVenta.getPeriodo() && auxVenta.getPeriodo() != 1)
        {
            VentaPeriodo auxVentaAnterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo() - 1);
            VentaPeriodo auxVentaPosterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo() + 1);

            auxVariacion = auxVenta.getCantidadVentas() - auxVentaAnterior.getCantidadVentas();
            auxVenta.setVariacionVentas(auxVariacion);

            if(auxVentaAnterior.getCantidadVentas() != 0)
            {
                auxVariacionPorcentaje = auxVariacion/auxVentaAnterior.getCantidadVentas();
                auxVenta.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }
            else
            {
                auxVariacionPorcentaje = auxVariacion;
                auxVenta.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }

            listarModificarVenta(auxVenta, vistaPronostico.getFilaSeleccionadaVentas());

            auxVariacion = auxVentaPosterior.getCantidadVentas() - auxVenta.getCantidadVentas();
            auxVentaPosterior.setVariacionVentas(auxVariacion);

            if(auxVenta.getCantidadVentas() != 0)
            {
                auxVariacionPorcentaje = auxVariacion/auxVenta.getCantidadVentas();
                auxVentaPosterior.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }
            else
            {
                auxVariacionPorcentaje = auxVariacion;
                auxVentaPosterior.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }

            listarModificarVenta(auxVentaPosterior, vistaPronostico.getFilaSeleccionadaVentas() + 1);
        }
        else if(historicoDao.getHistorico().size() == auxVenta.getPeriodo())
        {
            VentaPeriodo auxVentaAnterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo() - 1);
            auxVariacion = auxVenta.getCantidadVentas() - auxVentaAnterior.getCantidadVentas();
            auxVenta.setVariacionVentas(auxVariacion);

            if(auxVentaAnterior.getCantidadVentas() != 0)
            {
                auxVariacionPorcentaje = auxVariacion/auxVentaAnterior.getCantidadVentas();
                auxVenta.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }
            else
            {
                auxVariacionPorcentaje = auxVariacion;
                auxVenta.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }

            listarModificarVenta(auxVenta, vistaPronostico.getFilaSeleccionadaVentas());
        }
        else if(auxVenta.getPeriodo() == 1)
        {
            VentaPeriodo auxVentaPosterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo() + 1);

            auxVariacion = auxVentaPosterior.getCantidadVentas() - auxVenta.getCantidadVentas();
            auxVentaPosterior.setVariacionVentas(auxVariacion);

            if(auxVenta.getCantidadVentas() != 0)
            {
                auxVariacionPorcentaje = auxVariacion/auxVenta.getCantidadVentas();
                auxVentaPosterior.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }
            else
            {
                auxVariacionPorcentaje = auxVariacion;
                auxVentaPosterior.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }

            listarModificarVenta(auxVenta, vistaPronostico.getFilaSeleccionadaVentas());
            listarModificarVenta(auxVentaPosterior, vistaPronostico.getFilaSeleccionadaVentas() + 1);
        }
    }

    private void calcularVariacionBorrar(VentaPeriodo auxVenta)
    {
        double auxVariacion = 0;
        double auxVariacionPorcentaje = 0;

        if(historicoDao.getHistorico().size() + 1 != auxVenta.getPeriodo() && auxVenta.getPeriodo() != 1)
        {
            System.out.println("a");
            VentaPeriodo auxVentaAnterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo() - 1);
            VentaPeriodo auxVentaPosterior = historicoDao.getVentaHistorica(auxVenta.getPeriodo());

            System.out.println(auxVentaPosterior.getCantidadVentas());
            System.out.println(auxVentaAnterior.getCantidadVentas());
            auxVariacion = auxVentaPosterior.getCantidadVentas() - auxVentaAnterior.getCantidadVentas();
            auxVentaPosterior.setVariacionVentas(auxVariacion);

            if(auxVentaAnterior.getCantidadVentas() != 0)
            {
                auxVariacionPorcentaje = auxVariacion/auxVentaAnterior.getCantidadVentas();
                auxVentaPosterior.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }
            else
            {
                auxVariacionPorcentaje = auxVariacion;
                auxVentaPosterior.setPorcentajeVariacionVenta(auxVariacionPorcentaje);
            }

            listarModificarVenta(auxVentaPosterior, auxVentaPosterior.getPeriodo()-1);
        }
        else if(auxVenta.getPeriodo() == 1)
        {
            VentaPeriodo auxVentaPosterior = historicoDao.getVentaHistorica(1);
            auxVentaPosterior.setVariacionVentas(0);
            auxVentaPosterior.setPorcentajeVariacionVenta(0);
            listarModificarVenta(auxVentaPosterior, 0);
        }
    }

    private void recalcularAños()
    {
        ArrayList<VentaPeriodo> auxHistorico;
        auxHistorico = historicoDao.getHistorico();
        for(VentaPeriodo ventaPeriodo : auxHistorico)
        {
            ventaPeriodo.setPeriodo(auxHistorico.indexOf(ventaPeriodo) + 1);
            listarModificarVenta(ventaPeriodo, auxHistorico.indexOf(ventaPeriodo));
        }
    }

    private double calcularTotalPorcentajeVariacion()
    {
        ArrayList<VentaPeriodo> ventasHistoricas = historicoDao.getHistorico();
        double totalPorcentajeVariacion = 0;
        for(VentaPeriodo ventaPeriodo : ventasHistoricas)
        {
            totalPorcentajeVariacion = totalPorcentajeVariacion + ventaPeriodo.getPorcentajeVariacionVenta();
        }

        return totalPorcentajeVariacion;
    }

    private void listarAgregarVenta(VentaPeriodo auxVenta)
    {
        DefaultTableModel modelo = (DefaultTableModel) vistaPronostico.getTableModelHistoricoVentas();
        modelo.addRow(new Object[]{auxVenta.getPeriodo(),auxVenta.getCantidadVentas(), auxVenta.getVariacionVentas(), auxVenta.getPorcentajeVariacionVenta()});
    }

    private void listarModificarVenta(VentaPeriodo auxVenta, int auxFila)
    {
        int auxAno = auxVenta.getPeriodo();
        double auxCantidadVentas = auxVenta.getCantidadVentas();
        double variacionVenta = auxVenta.getVariacionVentas();
        double porcentajeVariacion = auxVenta.getPorcentajeVariacionVenta();

        DefaultTableModel modelo = (DefaultTableModel) vistaPronostico.getTableModelHistoricoVentas();
        modelo.setValueAt(auxAno, auxFila, 0);
        modelo.setValueAt(auxCantidadVentas, auxFila, 1);
        modelo.setValueAt(variacionVenta, auxFila, 2);
        modelo.setValueAt(porcentajeVariacion, auxFila, 3);
    }

    private void listarBorrarVenta()
    {
        DefaultTableModel modelo = (DefaultTableModel) vistaPronostico.getTableModelHistoricoVentas();
        int auxFila = vistaPronostico.getFilaSeleccionadaVentas();
        modelo.removeRow(auxFila);
    }

    private void generarPronostico()
    {
        int auxCantidadAnhos;
        if(comprobarCampoNumeroPronostico())
        {
            try
            {
                auxCantidadAnhos = Integer.parseInt(vistaPronostico.getCantidadAños());
                if(auxCantidadAnhos>1)
                {
                    if(historicoDao.getHistorico().size() >2)
                    {
                        pronosticoEliminarAll();
                        calcularPronostico(auxCantidadAnhos);
                        listarPronostico();
                        pronosticoDao.limpiarPronostico();
                    }
                    else
                    {
                        vistaPronostico.mostrarMensajeError("Necesita minimo 3 datos de Ventas historicas para generar un pronostico");
                    }
                }
                else
                {
                    vistaPronostico.mostrarMensajeError("Ingrese un valor de años a pronosticar mayor o igual a 2");
                }

            }
            catch (NumberFormatException ex)
            {
                vistaPronostico.mostrarMensajeError("Digite correctamente los años a pronosticar");
            }

        }
        else
        {
            vistaPronostico.mostrarMensajeError("Rellene el campo de años a pronosticar");
        }
    }

    private void calcularPronostico(int cantidadAnhos)
    {
        ArrayList<VentaPeriodo> historial = historicoDao.getHistorico();
        double promedioTotal = Double.parseDouble(vistaPronostico.getPorcentajeVarTotal())/(historial.size()-1);
        vistaPronostico.setPromedioVariacion("" + promedioTotal);
        VentaPeriodo auxVentaPronostico;
        double auxCantidadVentas = historicoDao.getVentaHistorica(historial.size()).getCantidadVentas();
        for(int i = historial.size()+1; i <= historial.size() + cantidadAnhos; i++)
        {
            auxCantidadVentas = auxCantidadVentas + (auxCantidadVentas * promedioTotal);
            auxVentaPronostico = new VentaPeriodo(auxCantidadVentas);
            auxVentaPronostico.setPeriodo(i);
            pronosticoDao.agregarPronostico(auxVentaPronostico);
        }
    }

    private void listarPronostico()
    {
        DefaultTableModel modelo = (DefaultTableModel) vistaPronostico.getTableModelPronostico();
        VentaPeriodo auxVentaPeriodo;
        for(int i = 0; i < pronosticoDao.getPronostico().size(); i++)
        {
            auxVentaPeriodo = pronosticoDao.getPronostico().get(i);
            modelo.addRow(new Object[]{auxVentaPeriodo.getPeriodo(), auxVentaPeriodo.getCantidadVentas()});
        }
    }

    private boolean comprobarCampoNumeroPronostico()
    {
        boolean campoValido;
        campoValido = !vistaPronostico.getCantidadAños().equals("");
        return  campoValido;
    }

    private void pronosticoEliminarAll()
    {
        DefaultTableModel auxModeloTabla = (DefaultTableModel) vistaPronostico.getTableModelPronostico();
        auxModeloTabla.setRowCount(0);
    }

    class PronosticoListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equalsIgnoreCase("AGREGAR AÑO"))
            {
                agregarVentaHistorica();
            }
            if (e.getActionCommand().equalsIgnoreCase("MODIFICAR AÑO"))
            {
                modificarVentaHistorica();
            }
            if (e.getActionCommand().equalsIgnoreCase("BORRAR AÑO"))
            {
               borrarVentaHistorica();
            }
            if(e.getActionCommand().equalsIgnoreCase("NUEVO PRONOSTICO"))
            {
                generarPronostico();
            }
        }
    }
}

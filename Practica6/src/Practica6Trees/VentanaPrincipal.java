package Practica6Trees;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
    public ProgressBarRenderer() {
        super(JProgressBar.HORIZONTAL, 0, 5000000);
        setStringPainted(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int habitantes = (int) value;

        if (column == 2) {
            double porcentaje = (double) (habitantes - 50000) / (5000000 - 50000);
            int red = (int) (255 * porcentaje);
            int green = (int) (255 * (1 - porcentaje));

            setValue(habitantes);
            setBackground(new Color(red, green, 0));
            setString(String.valueOf(habitantes));

            return this;
        } else {
            return null; // Devolvemos null para las otras columnas
        }
    }
}

public class VentanaPrincipal extends JFrame{
	
	private String provinciaSeleccionada = null;
	private int ordenado;
	private static final int COL_AUTONOMIA = 4;
	private static VentanaPrincipal vent;
	private JPanel pnlDerecha;
	private static MiJTree tree;
	private static DefaultTreeModel modeloArbol;
	private static DefaultMutableTreeNode raizMuni;
	private static DatasetParaJTable dataTabla;
	private static DataSetMunicipios dataset;
	private static VentanaTablaDatos ventanaDatos;
	private static JTable tablaDatos;
	private static MiTableModel modeloDatos;
	private String autonomiaSeleccionada = "";
	
	private class PanelDerecho extends JPanel {
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);

	        // Dibuja el gráfico de barras
	        if (provinciaSeleccionada != null) {
	            Municipio provincia = (Municipio) dataset.getListaMunicipiosProv(provinciaSeleccionada);
	            int totalProvincia = provincia.getHabitantes();
	            int totalEstado = dataset.getTotalHabitantes();

	            // Dibuja la barra de la provincia
	            int barraProvinciaHeight = getHeight() * 2 / 3;
	            int barraProvinciaWidth = getWidth() / 4;
	            int barraProvinciaX = getWidth() / 4;
	            int barraProvinciaY = getHeight() / 3;
	            int incrementoY = barraProvinciaHeight / dataset.getListaMunicipios().size();

	            g.setColor(Color.BLUE);
	            g.fillRect(barraProvinciaX, barraProvinciaY, barraProvinciaWidth, barraProvinciaHeight);
	            g.setColor(Color.BLACK);

	            // Dibuja las líneas horizontales para separar los municipios
	            for (int i = 1; i < dataset.getListaMunicipios().size(); i++) {
	                int lineaY = barraProvinciaY + i * incrementoY;
	                g.drawLine(barraProvinciaX, lineaY, barraProvinciaX + barraProvinciaWidth, lineaY);
	            }

	            // Dibuja el texto de la población en los municipios
	            g.setColor(Color.WHITE);
	            for (int i = 0; i < dataset.getListaMunicipios().size(); i++) {
	                Municipio municipio = dataset.getListaMunicipios().get(i);
	                int textoX = barraProvinciaX + 10;
	                int textoY = barraProvinciaY + i * incrementoY + 20;
	                g.drawString(String.valueOf(municipio.getHabitantes()), textoX, textoY);
	            }

	            // Dibuja la barra del estado
	            int barraEstadoHeight = getHeight() * 2 / 3;
	            int barraEstadoWidth = getWidth() / 4;
	            int barraEstadoX = 2 * getWidth() / 4;
	            int barraEstadoY = getHeight() / 3;

	            g.setColor(Color.GREEN);
	            g.fillRect(barraEstadoX, barraEstadoY, barraEstadoWidth, barraEstadoHeight);
	            g.setColor(Color.BLACK);

	            // Dibuja el texto de la población del estado
	            g.setColor(Color.BLACK);
	            g.drawString("Total Estado: " + totalEstado, barraEstadoX, barraEstadoY - 20);
	        }
	    }
	}
	
	private PanelDerecho panelDerecho;
	
	public static void main(String[] args) {
		vent = new VentanaPrincipal();
		vent.prueba();
	}

	public VentanaPrincipal() {
		setTitle( "Árbol de funciones" );
		setSize( 640, 480 );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setLocationRelativeTo( null );
		
		//Paso 3
		tablaDatos = new JTable();
		JScrollPane spCentro = new JScrollPane( tablaDatos );
		getContentPane().add( spCentro, BorderLayout.CENTER );
			
		tree = new MiJTree();
		JScrollPane spPrincipal = new JScrollPane( tree );
		getContentPane().add( spPrincipal, BorderLayout.WEST );
		
		panelDerecho = new PanelDerecho();
	    getContentPane().add(panelDerecho, BorderLayout.EAST);
	    panelDerecho.setPreferredSize(new Dimension(200, 200));
		
		JPanel pBotonera = new JPanel();
		getContentPane().add( pBotonera, BorderLayout.SOUTH );
		
		JButton bAnyadirMuni = new JButton( "Añadir municipio" );
		pBotonera.add( bAnyadirMuni );
		JButton bBorrar = new JButton( "Borrar municipio" );
		pBotonera.add( bBorrar );
		JButton bOrdenar = new JButton("Ordenar");
		pBotonera.add( bOrdenar );
		
		tree.setEditable( false );
		
		//Tarea 3
		tree.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
					TreePath tp = tree.getPathForLocation( e.getX(), e.getY() );
					DefaultMutableTreeNode nodoSel = (DefaultMutableTreeNode) tp.getLastPathComponent();
					if(nodoSel.isLeaf() && nodoSel!=null) {
						
					}
			}
		});
		
		bOrdenar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			String provincia = tree.getSelectionPath().getLastPathComponent().toString();
			//System.out.println(provincia);
			List<Municipio> municipiosEnProvincia = dataset.getListaMunicipiosProv(provincia);

			if(ordenado == 1) {
			municipiosEnProvincia.sort(Comparator.comparing(Municipio::getNombre));
			ordenado = 0;

			}else {
			municipiosEnProvincia.sort(Comparator.comparingInt(Municipio::getHabitantes).reversed());
			ordenado = 1;
			}
			((MiTableModel) tablaDatos.getModel()).setListaMunicipios(municipiosEnProvincia);
			tablaDatos.repaint();

			}
			});
		
		bAnyadirMuni.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int filaSel = tablaDatos.getSelectedRow();
				int tam = tablaDatos.getRowCount();
				dataset.anyadir( new Municipio( tam+1, "Nombre", 50000, dataset.getListaMunicipios().get(filaSel).getProvincia(), dataset.getListaMunicipios().get(filaSel).getAutonomia() ), tam );
				modeloDatos.anyadeFila( tam );
				tablaDatos.repaint();
			}
		});
		
		bBorrar.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showConfirmDialog(null, "¿Estás seguro de que deseas borrar este municipio?", "No hay vuelta atrás", JOptionPane.YES_NO_OPTION);
				int filaSel = tablaDatos.getSelectedRow();
				if (filaSel >= 0) {
					dataset.quitar( dataset.getListaMunicipios().get(filaSel).getCodigo() );
					modeloDatos.borraFila(filaSel);
				}
			}
		});
		
	}
	
	//Traea 3
	
	
	private void prueba() {
		
		try {
	        dataset = new DataSetMunicipios( "municipios200k.txt" );
	        System.out.println( "Cargados municipios:" );
	        for (Municipio m : dataset.getListaMunicipios() ) {
	            System.out.println( "\t" + m );
	        }
	    } catch (IOException e) {
	        System.err.println( "Error en carga de municipios" );
	    }
	    tablaDatos.setModel(dataset);
	    
	    raizMuni = new DefaultMutableTreeNode( "Municipios" );
	    modeloArbol = new DefaultTreeModel( raizMuni );
	    tree.setModel( modeloArbol );

	    for(int i = 0 ; i<dataset.getListaAutonomias().size() ; i++) {
	        String autonomia = dataset.getListaAutonomias().get(i);
	        DefaultMutableTreeNode aut = crearNodo("" + autonomia, raizMuni, i);
	        for(int j = 0 ; j<dataset.getListaProvinciasDeAutonomia(dataset.getListaAutonomias().get(i)).size() ; j++) {
	            crearNodo("" + dataset.getListaProvinciasDeAutonomia(autonomia).get(j), aut, j);
	        }
	    }

	    tree.addTreeSelectionListener(new TreeSelectionListener() {
	        @Override
	        public void valueChanged(TreeSelectionEvent e) {
	            TreePath tp = e.getPath();
	            if(tp != null) {
	                DefaultMutableTreeNode nodoSel = (DefaultMutableTreeNode) tp.getLastPathComponent();
	                if(nodoSel != null && nodoSel.isLeaf()) {
	                    String provinciaSel = (String) nodoSel.getUserObject();
	                    cargarDatosProvincia(provinciaSel);
	                }
	            }
	        }
	    });

	    vent.setVisible( true );
	}
	
	private void cargarDatosProvincia(String provinciaSel) {
	    List<Municipio> municipiosEnProvincia = dataset.getListaMunicipiosProv(provinciaSel);
	    municipiosEnProvincia.sort(Comparator.comparing(Municipio::getNombre));

	    modeloDatos = new MiTableModel();
	    tablaDatos.setModel(modeloDatos);

	    for (Municipio municipio : municipiosEnProvincia) {
	        modeloDatos.setListaMunicipios(municipiosEnProvincia);
	    }
	    tablaDatos.repaint();

	    // Agregar una barra de progreso al nombre de la provincia en el árbol
	    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    if (selectedNode != null && selectedNode.getUserObject() instanceof Municipio) {
	        Municipio provincia = (Municipio) selectedNode.getUserObject();
	        int totalHabitantes = provincia.getHabitantes();
	        JProgressBar progressBar = new JProgressBar(0, totalHabitantes);
	        progressBar.setStringPainted(true);
	        progressBar.setValue(totalHabitantes);

	        // Agregar la barra de progreso a la descripción del nodo
	        selectedNode.setUserObject(provincia.getNombre() + " (" + totalHabitantes + " habitantes)");
	        tree.repaint();
	        provinciaSeleccionada = provinciaSel; // Actualiza el estado seleccionado
	        panelDerecho.repaint(); // Vuelve a dibujar el panel derecho para actualizar el gráfico
	    }
	}
	
	
	private DefaultMutableTreeNode crearNodo( Object dato, DefaultMutableTreeNode nodoPadre, int posi ) {
		DefaultMutableTreeNode nodo1 = new DefaultMutableTreeNode( dato );
		modeloArbol.insertNodeInto( nodo1, nodoPadre, posi ); // Este método hace las dos cosas: inserta y notifica a los escuchadores del modelo
		tree.expandir( new TreePath(nodo1.getPath()), true ); // --> lo que hacemos es expandir el tree hasta ese nodo (incluido)
		return nodo1;
	}

	@SuppressWarnings("serial")
	public static class MiJTree extends JTree {
		public void expandir( TreePath path, boolean estado ) {
			setExpandedState( path, estado );
		}
	}
	
	private void rendererProvincia(JTree treeDatos, DataSetMunicipios datosMunis) {
		   treeDatos.setCellRenderer(new DefaultTreeCellRenderer() {
		       @Override
		       public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		                                                     boolean leaf, int row, boolean hasFocus) {
		           Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		           if (value instanceof DefaultMutableTreeNode) {
		               DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		               if (dataset.getProvincias().contains(node.getUserObject()) && node.isLeaf()) {
		                   String provincia = (String) node.getUserObject();
		                   
		                   int habitantes = 0;
		                   for(int i=0; i<datosMunis.getListaMunicipiosProv(provincia).size(); i++) {
		                       habitantes += datosMunis.getListaMunicipiosProv(provincia).get(i).getHabitantes();
		                   }
		                   JProgressBar progressBar = new JProgressBar();
		                   progressBar.setMaximum(5000000);
		                   progressBar.setValue(habitantes);

		                   JPanel panel = new JPanel(new BorderLayout());
		                   panel.add(new JLabel(provincia), BorderLayout.WEST);
		                   panel.add(progressBar, BorderLayout.EAST);

		                   return panel;
		               }
		           }
		           return c;
		       }
		   });
		}
	
	private class MiTableModel extends AbstractTableModel implements TableModel {

		// Paso 7
		private final Class<?>[] CLASES_COLS = { Integer.class, String.class, Integer.class, String.class, String.class };
		private List<Municipio> listaMunicipios;
		@Override
		public Class<?> getColumnClass(int columnIndex) {
		    if (columnIndex == 2) {
		        return ProgressBarRenderer.class;
		    } else {
		        return CLASES_COLS[columnIndex]; // o super.getColumnClass(columnIndex) si prefieres el comportamiento predeterminado
		    }
		}

		public void setListaMunicipios(List<Municipio> municipiosEnProvincia) {
			this.listaMunicipios = municipiosEnProvincia;
			fireTableDataChanged();
		}

		@Override
		public int getColumnCount() {
			// System.out.println( "getColumnCount" );
			return 5;
		}

		@Override
		public int getRowCount() {
			 if (listaMunicipios != null) { // Verifica que la lista no sea null antes de acceder a su tamaño
		            return listaMunicipios.size();
		        } else {
		            return 0; // Si la lista es null, devuelve 0
		        }
		    }

		private final String[] cabeceras = { "Código", "Nombre", "Habitantes", "Provincia", "Autonomía" };
		@Override
		public String getColumnName(int columnIndex) {
			// System.out.println( "getColumnName " + columnIndex );
			return cabeceras[columnIndex];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// System.out.println( "getValueAt " + rowIndex + "," + columnIndex );
			switch (columnIndex) {
			case 0:
				return dataset.getListaMunicipios().get(rowIndex).getCodigo();
			case 1:
				return dataset.getListaMunicipios().get(rowIndex).getNombre();
			case 2:
				return dataset.getListaMunicipios().get(rowIndex).getHabitantes();
			case 3:
				return dataset.getListaMunicipios().get(rowIndex).getProvincia();
			case 4:
				return dataset.getListaMunicipios().get(rowIndex).getAutonomia();
			default:
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			System.out.println( "isCellEditable" );
			if (columnIndex == 0) {
				return false;
			}
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			System.out.println( "setValue " + aValue + "[" + aValue.getClass().getName() + "] " + rowIndex + "," + columnIndex );
			switch (columnIndex) {
			case 0:
				dataset.getListaMunicipios().get(rowIndex).setCodigo( (Integer) aValue );
				break;
			case 1:
				dataset.getListaMunicipios().get(rowIndex).setNombre( (String) aValue );
				break;
			case 2:
				try {
					// Cuando no estaba especializada la columna había que tratarla como Object
					// datosMunis.getListaMunicipios().get(rowIndex).setHabitantes( Integer.parseInt((String)aValue) );
					// Pero ahora puede tratarse como Integer:
					dataset.getListaMunicipios().get(rowIndex).setHabitantes( (Integer) aValue );
				} catch (NumberFormatException e) {
					JOptionPane.showInputDialog("Nº de habitantes erróneo" );
				}
				break;
			case 3:
				dataset.getListaMunicipios().get(rowIndex).setProvincia( (String) aValue );
				break;
			case 4:
				dataset.getListaMunicipios().get(rowIndex).setAutonomia( (String) aValue );
				break;
			}
		}

		ArrayList<TableModelListener> listaEsc = new ArrayList<>();
		@Override
		public void addTableModelListener(TableModelListener l) {
			System.out.println( "addTableModelListener" );
			listaEsc.add( l );
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			listaEsc.remove( l );
		}
		
		// DefaultTableModel lo hace así
		public void fireTableChanged( TableModelEvent e ) {
			for (TableModelListener l : listaEsc) {
				l.tableChanged( e );
			}
		}
		
	    // Paso 5
		public void borraFila( int fila ) {
			fireTableChanged( new TableModelEvent( modeloDatos, fila, dataset.getListaMunicipios().size() ));
		}
		
	    // Paso 6
	    public void anyadeFila( int fila ) {
	    	fireTableChanged( new TableModelEvent( modeloDatos, fila, dataset.getListaMunicipios().size() ) );  // Para que detecte el cambio en todas
	    }
	    
	}
	
	private void CargarDatosTabla(String provinciaSel) {
		List<Municipio> municipiosEnProvincia = dataset.getListaMunicipiosProv(provinciaSel);
		municipiosEnProvincia.sort(Comparator.comparing(Municipio::getNombre));
		modeloDatos = new MiTableModel();
		tablaDatos.setModel(modeloDatos);
		
		tablaDatos.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
		private JProgressBar pbPoblacion = new JProgressBar(50000, 5000000);
		@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
			if(column == 3) {
				int poblacion = (Integer) value;
				double porcentaje = (double) (poblacion - 50000) / (5000000 - 50000);
	
				int red = (int) (255 * porcentaje);
				int green = (int) (255 * (1 - porcentaje));
	
				pbPoblacion.setValue(poblacion);
				pbPoblacion.setForeground(new Color(red, green, 0));
	
				return pbPoblacion;
				} else {
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}
		});
	
	}
}



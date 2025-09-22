
import java.text.DecimalFormat;
import java.util.function.DoubleUnaryOperator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author chant
 */
public class holaa extends javax.swing.JFrame {

    /**
     * Creates new form holaa
     */
     private static final String[] NOMBRES = {
        "y = 2x^2 − 4x + 1",
        "y = 5 * (1.5)^x",
        "y = log10(x)",
        "y = −7x^5 + 4x^3 − 1.5x + 10",
        "y = x^4 − 2x^3 + 5x^2 − 9"
    };

    private static final DoubleUnaryOperator[] FUNCIONES = {
        x -> 2*Math.pow(x,2) - 4*x + 1,
        x -> 5*Math.pow(1.5,x),
        x -> Math.log10(x),
        x -> -7*Math.pow(x,5) + 4*Math.pow(x,3) - 1.5*x + 10,
        x -> Math.pow(x,4) - 2*Math.pow(x,3) + 5*Math.pow(x,2) - 9
    };

    private static final DoubleUnaryOperator[] DERIVADAS = {
        x -> 4*x - 4,
        x -> 5*Math.pow(1.5,x) * Math.log(1.5),
        x -> 1/(x * Math.log(10)),
        x -> -35*Math.pow(x,4) + 12*Math.pow(x,2) - 1.5,
        x -> 4*Math.pow(x,3) - 6*Math.pow(x,2) + 10*x
    };

    private DecimalFormat df = new DecimalFormat("#.####");
    public holaa() {
        initComponents();
        cbxFuncion.setModel(new DefaultComboBoxModel<>(NOMBRES));
        inicializarTabla();

        // Botones
        btnBiseccion.addActionListener(e -> btnBiseccionActionPerformed(null));
        btnRegla.addActionListener(e -> btnReglaActionPerformed(null));
        btnNewton.addActionListener(e -> btnNewtonActionPerformed(null));
        btnSecante.addActionListener(e -> btnSecanteActionPerformed(null));
        btnComparar.addActionListener(e -> btnCompararActionPerformed(null));
    }
private void inicializarTabla() {
        String[] columnas = {"Iteración", "Xi", "Xs", "Xr", "f(Xr)", "Ea %"};
        DefaultTableModel model = new DefaultTableModel(columnas,0);
        tblRaices.setModel(model);
        tblRaices.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblRaices.getTableHeader().setReorderingAllowed(false);
        tblRaices.setFillsViewportHeight(true);

        int[] anchos = {80,100,100,100,100,80};
        for(int i=0;i<tblRaices.getColumnCount();i++){
            tblRaices.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

   private double[] calcularMetodo(DoubleUnaryOperator f, DoubleUnaryOperator dF,
                                double xi, double xf, double tol, String metodo) {

    // Reiniciar tabla con cabeceras uniformes
    String[] columnas = {"Iteración", "Xi", "Xs", "Xr", "f(Xr)", "Ea %"};
    DefaultTableModel model = new DefaultTableModel(columnas, 0);
    tblRaices.setModel(model);

    double xr = 0.0, ea = 100.0;
    int i = 0;

    switch (metodo) {
        case "Biseccion": {
            double l = xi, u = xf; // lower/upper
            while (ea > tol) {
                double xrold = xr;
                xr = (l + u) / 2.0;
                if (i > 0) ea = Math.abs((xr - xrold) / xr) * 100.0;

                double fxr = f.applyAsDouble(xr);
                model.addRow(new Object[]{
                    i, df.format(l), df.format(u), df.format(xr), df.format(fxr), df.format(ea)
                });

                double fxl = f.applyAsDouble(l);
                if (fxl * fxr < 0) u = xr; else l = xr;
                i++;
            }
            break;
        }

        case "ReglaFalsa": {
            double l = xi, u = xf;
            while (ea > tol) {
                double xrold = xr;
                double fxl = f.applyAsDouble(l);
                double fxu = f.applyAsDouble(u);

                xr = u - fxu * (l - u) / (fxl - fxu);  // falsa posición
                if (i > 0) ea = Math.abs((xr - xrold) / xr) * 100.0;

                double fxr = f.applyAsDouble(xr);
                model.addRow(new Object[]{
                    i, df.format(l), df.format(u), df.format(xr), df.format(fxr), df.format(ea)
                });

                if (fxl * fxr < 0) u = xr; else l = xr;
                i++;
            }
            break;
        }

        case "Newton": {
            double x = xi;
            ea = 100.0; i = 0;
            while (ea > tol) {
                double xold = x;
                double dfx = dF.applyAsDouble(x);
                if (dfx == 0.0) {
                    JOptionPane.showMessageDialog(this, "f'(x) = 0. No se puede continuar con Newton.");
                    break;
                }
                x = x - f.applyAsDouble(x) / dfx;
                if (i > 0) ea = Math.abs((x - xold) / x) * 100.0;

                double fxr = f.applyAsDouble(x);
                // Para uniformidad, mostramos Xi = xold, Xs = xold, Xr = x
                model.addRow(new Object[]{
                    i, df.format(xold), df.format(xold), df.format(x), df.format(fxr), df.format(ea)
                });
                i++;
            }
            xr = x;
            break;
        }

        case "Secante": {
            double x0 = xi;
            double x1 = xf;
            double x2 = 0.0;      // <== DECLARADO
            ea = 100.0; i = 0;

            while (ea > tol) {
                double fx0 = f.applyAsDouble(x0);
                double fx1 = f.applyAsDouble(x1);
                double denom = (fx0 - fx1);
                if (denom == 0.0) {
                    JOptionPane.showMessageDialog(this, "Denominador 0 en Secante. No se puede continuar.");
                    break;
                }

                double xold = x2;
                x2 = x1 - fx1 * (x0 - x1) / denom;

                if (i > 0) ea = Math.abs((x2 - xold) / x2) * 100.0;

                double fxr = f.applyAsDouble(x2);
                model.addRow(new Object[]{
                    i, df.format(x0), df.format(x1), df.format(x2), df.format(fxr), df.format(ea)
                });

                x0 = x1;
                x1 = x2;
                i++;
            }
            xr = x2;
            break;
        }
    }

    return new double[]{xr, ea};
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtE = new javax.swing.JTextField();
        txtFinal = new javax.swing.JTextField();
        btnSecante = new javax.swing.JButton();
        btnBiseccion = new javax.swing.JButton();
        btnRegla = new javax.swing.JButton();
        btnNewton = new javax.swing.JButton();
        btnComparar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRaices = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtInicial = new javax.swing.JTextField();
        cbxFuncion = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEActionPerformed(evt);
            }
        });

        txtFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFinalActionPerformed(evt);
            }
        });

        btnSecante.setBackground(new java.awt.Color(255, 204, 204));
        btnSecante.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnSecante.setText("Secante");
        btnSecante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSecanteActionPerformed(evt);
            }
        });

        btnBiseccion.setBackground(new java.awt.Color(153, 255, 153));
        btnBiseccion.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnBiseccion.setText("Bisección");
        btnBiseccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBiseccionActionPerformed(evt);
            }
        });

        btnRegla.setBackground(new java.awt.Color(255, 255, 153));
        btnRegla.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnRegla.setText("Regla Falsa");
        btnRegla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReglaActionPerformed(evt);
            }
        });

        btnNewton.setBackground(new java.awt.Color(204, 153, 255));
        btnNewton.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnNewton.setText("Newton");
        btnNewton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewtonActionPerformed(evt);
            }
        });

        btnComparar.setBackground(new java.awt.Color(153, 204, 255));
        btnComparar.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnComparar.setText("Comparar");
        btnComparar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompararActionPerformed(evt);
            }
        });

        tblRaices.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tblRaices.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        tblRaices.setForeground(new java.awt.Color(0, 0, 0));
        tblRaices.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Iteración", "xi", "xf", "xr", "f(xi)", "f(xf)", "f(xr)", "Ea (%)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblRaices);

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 22)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Raíces");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        jLabel2.setText("Función");

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        jLabel3.setText("X Inicial:");

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        jLabel4.setText("X Final:");

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        jLabel5.setText("Ea Max:");

        txtInicial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInicialActionPerformed(evt);
            }
        });

        cbxFuncion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(68, 68, 68)
                        .addComponent(jLabel3)
                        .addGap(60, 60, 60)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbxFuncion, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(txtInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(txtFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(txtE, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnBiseccion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnSecante, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnNewton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnRegla, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnComparar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGap(17, 17, 17))
            .addGroup(layout.createSequentialGroup()
                .addGap(208, 208, 208)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxFuncion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(96, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBiseccion, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnRegla, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(btnSecante, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(btnNewton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btnComparar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEActionPerformed

    private void txtFinalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFinalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFinalActionPerformed

    private void btnSecanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSecanteActionPerformed
        double xi = Double.parseDouble(txtInicial.getText());
        double xf = Double.parseDouble(txtFinal.getText());
        double tol = Double.parseDouble(txtE.getText());
        calcularMetodo(FUNCIONES[cbxFuncion.getSelectedIndex()], DERIVADAS[cbxFuncion.getSelectedIndex()], xi, xf, tol, "Secante");
    }//GEN-LAST:event_btnSecanteActionPerformed

    private void btnBiseccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBiseccionActionPerformed
         double xi = Double.parseDouble(txtInicial.getText());
        double xf = Double.parseDouble(txtFinal.getText());
        double tol = Double.parseDouble(txtE.getText());
        calcularMetodo(FUNCIONES[cbxFuncion.getSelectedIndex()], DERIVADAS[cbxFuncion.getSelectedIndex()], xi, xf, tol, "Biseccion");
    }//GEN-LAST:event_btnBiseccionActionPerformed

    private void btnReglaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReglaActionPerformed
         double xi = Double.parseDouble(txtInicial.getText());
        double xf = Double.parseDouble(txtFinal.getText());
        double tol = Double.parseDouble(txtE.getText());
        calcularMetodo(FUNCIONES[cbxFuncion.getSelectedIndex()], DERIVADAS[cbxFuncion.getSelectedIndex()], xi, xf, tol, "ReglaFalsa");
    }//GEN-LAST:event_btnReglaActionPerformed

    private void btnNewtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewtonActionPerformed
         double xi = Double.parseDouble(txtInicial.getText());
        double tol = Double.parseDouble(txtE.getText());
        calcularMetodo(FUNCIONES[cbxFuncion.getSelectedIndex()], DERIVADAS[cbxFuncion.getSelectedIndex()], xi, 0, tol, "Newton");
    
    }//GEN-LAST:event_btnNewtonActionPerformed

private ResultadoMetodo resumenMetodo(
        DoubleUnaryOperator f,
        DoubleUnaryOperator dF,
        double xi, double xf,
        double tol,
        String metodo) {

    double xr = 0.0;
    double ea = 100.0;
    int iter = 0;

    switch (metodo) {
        case "Biseccion": {
            double l = xi, u = xf;
            while (ea > tol) {
                double xrOld = xr;
                xr = (l + u) / 2.0;
                if (iter > 0) ea = Math.abs((xr - xrOld) / xr) * 100.0;
                if (f.applyAsDouble(l) * f.applyAsDouble(xr) < 0)
                    u = xr;
                else
                    l = xr;
                iter++;
            }
            break;
        }
        case "ReglaFalsa": {
            double l = xi, u = xf;
            while (ea > tol) {
                double xrOld = xr;
                double fl = f.applyAsDouble(l);
                double fu = f.applyAsDouble(u);
                xr = u - fu * (l - u) / (fl - fu);
                if (iter > 0) ea = Math.abs((xr - xrOld) / xr) * 100.0;
                if (fl * f.applyAsDouble(xr) < 0)
                    u = xr;
                else
                    l = xr;
                iter++;
            }
            break;
        }
        case "Newton": {
            double x = xi;
            while (ea > tol) {
                double xOld = x;
                x = x - f.applyAsDouble(x) / dF.applyAsDouble(x);
                if (iter > 0) ea = Math.abs((x - xOld) / x) * 100.0;
                iter++;
            }
            xr = x;
            break;
        }
        case "Secante": {
            double x0 = xi, x1 = xf, x2;
            while (ea > tol) {
                double xOld = x1;
                x2 = x1 - f.applyAsDouble(x1) * (x0 - x1)
                        / (f.applyAsDouble(x0) - f.applyAsDouble(x1));
                if (iter > 0) ea = Math.abs((x2 - x1) / x2) * 100.0;
                x0 = x1;
                x1 = x2;
                iter++;
            }
            xr = x1;
            break;
        }
    }

    // Aquí sí formateamos solo para mostrar
    return new ResultadoMetodo(
        metodo,
        df.format(xr),   // texto bonito
        iter,
        df.format(ea),
        ea               // valor numérico real para comparar
    );
}
private static class ResultadoMetodo {
    String metodo;
    String raizTxt;
    int iter;
    String errorTxt;
    double errorNum;
    ResultadoMetodo(String m, String rtxt, int i, String etxt, double en) {
        metodo = m;
        raizTxt = rtxt;
        iter = i;
        errorTxt = etxt;
        errorNum = en;
    }
}
    private void btnCompararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompararActionPerformed
double xi  = Double.parseDouble(txtInicial.getText());
    double xf  = Double.parseDouble(txtFinal.getText());
    double tol = Double.parseDouble(txtE.getText());

    String[] columnas = {"Método", "Raíz", "Iteraciones", "Error %", "Mejor"};
    DefaultTableModel model = new DefaultTableModel(columnas, 0);
    tblRaices.setModel(model);

    String[] metodos = {"Biseccion", "ReglaFalsa", "Newton", "Secante"};
    java.util.List<ResultadoMetodo> resultados = new java.util.ArrayList<>();

    for (String m : metodos) {
        resultados.add(resumenMetodo(
            FUNCIONES[cbxFuncion.getSelectedIndex()],
            DERIVADAS[cbxFuncion.getSelectedIndex()],
            xi, xf, tol, m
        ));
    }

    // Encontrar el mejor
    int mejorIdx = 0;
    int minIter = Integer.MAX_VALUE;
    double minErr = Double.MAX_VALUE;
    for (int i = 0; i < resultados.size(); i++) {
        ResultadoMetodo r = resultados.get(i);
        if (r.iter < minIter || (r.iter == minIter && r.errorNum < minErr)) {
            minIter = r.iter;
            minErr  = r.errorNum;
            mejorIdx = i;
        }
    }

    // Llenar la tabla
    for (int i = 0; i < resultados.size(); i++) {
        ResultadoMetodo r = resultados.get(i);
        String marca = (i == mejorIdx) ? "★" : "";
        model.addRow(new Object[]{r.metodo, r.raizTxt, r.iter, r.errorTxt, marca});
    }

    JOptionPane.showMessageDialog(this,
        "El método más eficiente es: " + resultados.get(mejorIdx).metodo);
    }//GEN-LAST:event_btnCompararActionPerformed

    private void txtInicialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInicialActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtInicialActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(holaa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(holaa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(holaa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(holaa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new holaa().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBiseccion;
    private javax.swing.JButton btnComparar;
    private javax.swing.JButton btnNewton;
    private javax.swing.JButton btnRegla;
    private javax.swing.JButton btnSecante;
    private javax.swing.JComboBox<String> cbxFuncion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRaices;
    private javax.swing.JTextField txtE;
    private javax.swing.JTextField txtFinal;
    private javax.swing.JTextField txtInicial;
    // End of variables declaration//GEN-END:variables
}

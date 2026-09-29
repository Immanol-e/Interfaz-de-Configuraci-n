package configuracionpagina;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class ConfiguracionPagina extends JFrame {

    // medidas de una hoja tamaño carta en milimetros
    final float ANCHO_HOJA = 216;
    final float ALTO_HOJA = 279;

    JSpinner spInferior, spSuperior;
    JComboBox<String> cbOrientacion;
    JButton btnInicializar;
    GLJPanel panelVertical, panelHorizontal;

    public ConfiguracionPagina() {
        setTitle("Configuración de página");
        setSize(500, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        Font fuente = new Font("Tahoma", Font.BOLD, 11);

        GLProfile perfil = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(perfil);

        // ---------- hoja en vertical ----------
        JLabel lblHoja = new JLabel("Hoja");
        lblHoja.setFont(fuente);
        lblHoja.setBounds(40, 15, 60, 20);
        add(lblHoja);

        panelVertical = new GLJPanel(cap);
        panelVertical.addGLEventListener(new VistaHoja(false));
        panelVertical.setBounds(20, 40, 90, 125);
        panelVertical.setBorder(BorderFactory.createEtchedBorder());
        add(panelVertical);

        // ---------- margenes ----------
        JLabel lblInferior = new JLabel("Margen inferior");
        lblInferior.setFont(fuente);
        lblInferior.setBounds(142, 15, 120, 20);
        add(lblInferior);

        spInferior = new JSpinner(new SpinnerNumberModel(10, 0, 60, 1));
        spInferior.setBounds(142, 38, 50, 26);
        spInferior.addChangeListener(e -> redibujar());
        add(spInferior);

        JLabel lblSuperior = new JLabel("Margen superior");
        lblSuperior.setFont(fuente);
        lblSuperior.setBounds(142, 105, 120, 20);
        add(lblSuperior);

        spSuperior = new JSpinner(new SpinnerNumberModel(5, 0, 60, 1));
        spSuperior.setBounds(142, 128, 50, 26);
        spSuperior.addChangeListener(e -> redibujar());
        add(spSuperior);

        // orientacion
        JLabel lblOrientacion = new JLabel("Orientación de página");
        lblOrientacion.setFont(fuente);
        lblOrientacion.setBounds(280, 15, 190, 20);
        add(lblOrientacion);

        cbOrientacion = new JComboBox<>(new String[]{"Vertical", "Horizontal"});
        cbOrientacion.setBounds(280, 40, 175, 22);
        cbOrientacion.addActionListener(e -> redibujar());
        add(cbOrientacion);

        // hoja en horizontal 
        panelHorizontal = new GLJPanel(cap);
        panelHorizontal.addGLEventListener(new VistaHoja(true));
        panelHorizontal.setBounds(272, 72, 180, 100);
        panelHorizontal.setBorder(BorderFactory.createEtchedBorder());
        add(panelHorizontal);

        // ---------- boton de reset ----------
        btnInicializar = new JButton("Inicializar");
        btnInicializar.setBounds(40, 210, 145, 24);
        btnInicializar.addActionListener(e -> inicializar());
        add(btnInicializar);
    }

    private float valor(JSpinner sp) {
        return ((Number) sp.getValue()).floatValue();
    }

    private boolean horizontalSeleccionado() {
        return cbOrientacion.getSelectedIndex() == 1;
    }

    // margenes en cero y hoja en vertical
    private void inicializar() {
        spInferior.setValue(0);
        spSuperior.setValue(0);
        cbOrientacion.setSelectedIndex(0);
        redibujar();
    }

    private void redibujar() {
        panelVertical.repaint();
        panelHorizontal.repaint();
    }

    /**
     * Dibuja la hoja en una orientacion. Si esa orientacion es la que esta
     * seleccionada se dibuja con sus margenes, si no se ve apagada.
     */
    class VistaHoja implements GLEventListener {

        boolean esHorizontal;
        int ancho, alto;

        VistaHoja(boolean esHorizontal) {
            this.esHorizontal = esHorizontal;
        }

        private void rectangulo(GL2 gl, int modo, float x, float y, float w, float h) {
            gl.glBegin(modo);
            gl.glVertex2f(x, y);
            gl.glVertex2f(x + w, y);
            gl.glVertex2f(x + w, y + h);
            gl.glVertex2f(x, y + h);
            gl.glEnd();
        }

        private void linea(GL2 gl, float x1, float y1, float x2, float y2) {
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(x1, y1);
            gl.glVertex2f(x2, y2);
            gl.glEnd();
        }

        @Override
        public void init(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();
            gl.glClearColor(0.88f, 0.90f, 0.93f, 1.0f);
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrtho(0, ancho, 0, alto, -1, 1);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();

            // tamaño de la hoja segun la orientacion de este panel
            float pw = esHorizontal ? ALTO_HOJA : ANCHO_HOJA;
            float ph = esHorizontal ? ANCHO_HOJA : ALTO_HOJA;

            // escala para que la hoja quepa en el panel
            float esc = Math.min((ancho - 12) / pw, (alto - 12) / ph);
            float w = pw * esc;
            float h = ph * esc;
            float x0 = (ancho - w) / 2;
            float y0 = (alto - h) / 2;

            // si no es la orientacion elegida la hoja se ve apagada
            if (esHorizontal != horizontalSeleccionado()) {
                gl.glColor3f(0.82f, 0.83f, 0.86f);
                rectangulo(gl, GL2.GL_QUADS, x0, y0, w, h);
                gl.glLineWidth(1.0f);
                gl.glColor3f(0.68f, 0.69f, 0.72f);
                rectangulo(gl, GL.GL_LINE_LOOP, x0, y0, w, h);
                return;
            }

            // margenes pasados a pixeles
            float sup = valor(spSuperior) * esc;
            float inf = valor(spInferior) * esc;

            // sombra
            gl.glColor3f(0.60f, 0.62f, 0.66f);
            rectangulo(gl, GL2.GL_QUADS, x0 + 3, y0 - 3, w, h);

            // hoja
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            rectangulo(gl, GL2.GL_QUADS, x0, y0, w, h);

            // area donde se puede escribir
            float ay = y0 + inf;
            float ah = h - sup - inf;
            gl.glColor3f(0.95f, 0.97f, 0.90f);
            rectangulo(gl, GL2.GL_QUADS, x0, ay, w, ah);

            // renglones de "texto" para que se vea como documento
            gl.glLineWidth(1.0f);
            gl.glColor3f(0.78f, 0.78f, 0.80f);
            int n = 0;
            for (float y = ay + ah - 5; y > ay + 3; y -= 6) {
                float fin = (n % 5 == 4) ? x0 + w * 0.6f : x0 + w - 5;
                linea(gl, x0 + 5, y, fin, y);
                n++;
            }

            // lineas de los margenes punteadas
            gl.glLineWidth(1.5f);
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.glLineStipple(1, (short) 0x0F0F);
            gl.glColor3f(0.10f, 0.50f, 0.35f);
            linea(gl, x0, y0 + h - sup, x0 + w, y0 + h - sup); // superior
            linea(gl, x0, y0 + inf, x0 + w, y0 + inf);         // inferior
            gl.glDisable(GL2.GL_LINE_STIPPLE);

            // contorno de la hoja
            gl.glColor3f(0.25f, 0.25f, 0.28f);
            rectangulo(gl, GL.GL_LINE_LOOP, x0, y0, w, h);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            ancho = width;
            alto = height;
            drawable.getGL().glViewport(0, 0, width, height);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConfiguracionPagina().setVisible(true));
    }
}
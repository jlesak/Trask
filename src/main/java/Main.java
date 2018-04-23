
public class Main {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrameForm jFrameForm = new JFrameForm("trasktest1.json", "output.json");
                jFrameForm.setVisible(true);
            }
        });
    }
}
